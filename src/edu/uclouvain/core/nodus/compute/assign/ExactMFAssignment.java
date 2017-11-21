/**
 * Copyright (c) 1991-2018 Université catholique de Louvain
 *
 * <p>Center for Operations Research and Econometrics (CORE)
 *
 * <p>http://www.uclouvain.be
 *
 * <p>This file is part of Nodus.
 *
 * <p>Nodus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package edu.uclouvain.core.nodus.compute.assign;

import edu.uclouvain.core.nodus.NodusC;
import edu.uclouvain.core.nodus.NodusMapPanel;
import edu.uclouvain.core.nodus.compute.assign.workers.AssignmentWorker;
import edu.uclouvain.core.nodus.compute.assign.workers.AssignmentWorkerParameters;
import edu.uclouvain.core.nodus.compute.assign.workers.ExactMFAssignmentWorker;
import edu.uclouvain.core.nodus.compute.exclusions.ExclusionReader;
import edu.uclouvain.core.nodus.compute.od.ODReader;
import edu.uclouvain.core.nodus.compute.virtual.PathWriter;
import edu.uclouvain.core.nodus.compute.virtual.VirtualNetwork;
import edu.uclouvain.core.nodus.compute.virtual.VirtualNetworkWriter;
import edu.uclouvain.core.nodus.tools.console.NodusConsole;
import edu.uclouvain.core.nodus.utils.GarbageCollectionRunner;
import edu.uclouvain.core.nodus.utils.WorkQueue;

/**
 * The exact multi-flow assignment computes one or several alternative routes for each mode/means
 * combination. It is based on the AStar shortest path algorithm instead of the algorithm of
 * Dijkstra in order to prevent some side effects described in <i> Jourquin B., A multi-flow
 * multi-modal assignment procedure on large freight transportation networks, Studies in Regional
 * Science, Vol 35, n°4, pp. 929- 946, 2005.</i>.
 *
 * <p>Even if the AStar algorithm is faster than Dijkstra if the shortest path between a single
 * origin-destination pair must be computed, running AStar for each pair of a row in the OD matrix
 * is slower than running Dijkstra, which computes, in a single run, all the routes for the this row
 * of OD pairs. The exact multi-flow assignment is therefore (much) slower than the fast multi-fmow
 * assignment.
 *
 * @author Bart Jourquin
 */
public class ExactMFAssignment extends Assignment {

  /**
   * Initializes the assignment procedure.
   *
   * @param ap AssignmentParameters
   */
  public ExactMFAssignment(AssignmentParameters ap) {
    super(ap);
  }

  /** Thread that does the assignment work. */
  @Override
  public boolean assign() {

    // double meansModeRatio = assignmentParameters.getMeansModeRatio();

    // Test if scenario already exists
    if (!VirtualNetworkWriter.acceptScenario(
        nodusProject, assignmentParameters.getScenario(), assignmentParameters.isConfirmDelete())) {
      return false;
    }

    // Generate a virtual network
    virtualNet = new VirtualNetwork(assignmentParameters);

    if (!virtualNet.generate()) {
      return false;
    }

    // Read the exclusions
    ExclusionReader er = new ExclusionReader(virtualNet);

    if (er.hasExclusions()) {
      if (!er.loadExclusions()) {
        return false;
      }
    }

    // Read the O-D matrixes
    ODReader odr = new ODReader(assignmentParameters);

    if (!odr.loadDemand(virtualNet)) {
      return false;
    }

    // Get the structure of the virtual network
    // VirtualNodeList[] vnl = virtualNet.getVirtualNetworkStructure();

    // Create a path writer
    pathWriter = new PathWriter(assignmentParameters);

    // Display console if needed
    if (assignmentParameters.isLogLostPaths()) {
      new NodusConsole(nodusProject.getLocalProperty(NodusC.PROP_PROJECT_DOTPATH));
      System.out.println(i18n.get(Assignment.class, "Lost_paths", "Lost paths:"));
      System.out.println(
          NodusC.DBF_GROUP
              + ", "
              + NodusC.DBF_ORIGIN
              + ", "
              + NodusC.DBF_DESTINATION
              + ", "
              + NodusC.DBF_QUANTITY);
    }

    // long start = System.currentTimeMillis();

    // Force Garbage collector?
    NodusMapPanel nodusMapPanel = nodusProject.getNodusMapPanel();
    int gcInterval = nodusMapPanel.getGarbageCollectorInterval();
    GarbageCollectionRunner gcr = new GarbageCollectionRunner(gcInterval);

    /**
     * Here, the assignment must be performed cell per cell in the O-D matrix. For each cell,
     * NbIterartions alternative routes must be computed. After each iteration, the costs on the
     * used links are increased by a cost mark-up in order to make the next computed route
     * significantly different from the current route. This makes the available alternatives more
     * plausible.
     */

    // --- Assign flows
    // ---------------------------------------------------------------------
    for (byte odClass = 0; odClass < virtualNet.getNbODClasses(); odClass++) {

      if (!virtualNet.odClassHasDemand(odClass)) {
        continue;
      }

      // Get the number of threads
      int threads = assignmentParameters.getThreads();

      // The initial costs must be computed (these are the real costs)
      if (!virtualNet.computeCosts(0, odClass, assignmentParameters.getCostFunctions(), threads)) {
        nodusMapPanel.stopProgress();

        return false;
      }

      // Create the work queue
      WorkQueue queue = new WorkQueue();

      // Create a set of worker threads
      assignmentWorkers = new AssignmentWorker[threads];
      for (int i = 0; i < assignmentWorkers.length; i++) {
        assignmentWorkers[i] = new ExactMFAssignmentWorker(queue);
        assignmentWorkers[i].start();
      }

      // Add the jobs to the queue
      for (byte groupIndex = 0; groupIndex < (byte) virtualNet.getGroups().length; groupIndex++) {
        AssignmentWorkerParameters awp = new AssignmentWorkerParameters(this, groupIndex, odClass);
        queue.addWork(awp);
      }

      // Add special end-of-stream markers to terminate the workers
      for (int i = 0; i < assignmentWorkers.length; i++) {
        queue.addWork(WorkQueue.NO_MORE_WORK);
      }

      // Initialize a progress monitor with the number of OD matrix rows to assign
      int lengthOfTask = 0;

      for (byte groupIndex = 0; groupIndex < (byte) virtualNet.getGroups().length; groupIndex++) {
        for (int nodeIndex = 0; nodeIndex < virtualNet.getVirtualNodeLists().length; nodeIndex++) {
          // Get the demand associated to this node and group
          if (virtualNet.getVirtualNodeLists()[nodeIndex].hasDemandForGroup(
              virtualNet.getGroups()[groupIndex], odClass)) {
            lengthOfTask++;
          }
        }
      }
      nodusMapPanel.startProgress(lengthOfTask);

      // Wait until all the works are completed
      for (int i = 0; i < threads; i++) {
        try {
          assignmentWorkers[i].join();
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      }

      nodusMapPanel.stopProgress();

      // Test if everything was OK
      for (int i = 0; i < threads; i++) {
        if (assignmentWorkers[i].isCancelled()) {
          return false;
        }
      }

      nodusMapPanel.stopProgress();
    } // Next od class

    // Transform the flows in vehicles
    virtualNet.flowsToVehicles();

    gcr.stop();
    // long end = System.currentTimeMillis();
    // System.out.println("Duration : " + ((end - start) / 1000));

    // Close path writer
    pathWriter.close();

    // Create a Virtual Network writer
    VirtualNetworkWriter vnw = new VirtualNetworkWriter(assignmentParameters, virtualNet);
    return vnw.save();
  }
}

/**
 * Copyright (c) 1991-2019 Université catholique de Louvain
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

package edu.uclouvain.core.nodus.compute.virtual;

import com.bbn.openmap.Environment;
import com.bbn.openmap.util.I18n;

import edu.uclouvain.core.nodus.NodusC;
import edu.uclouvain.core.nodus.NodusMapPanel;
import edu.uclouvain.core.nodus.NodusProject;
import edu.uclouvain.core.nodus.compute.assign.AssignmentParameters;
import edu.uclouvain.core.nodus.database.JDBCField;
import edu.uclouvain.core.nodus.database.JDBCUtils;
import edu.uclouvain.core.nodus.swing.SingleInstanceMessagePane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;

import javax.swing.JOptionPane;

/**
 * Writes the result of an assignment in a database table.
 *
 * @author Bart Jourquin
 */
public class VirtualNetworkWriter {

  private static I18n i18n = Environment.getI18n();

  private NodusMapPanel nodusMapPanel;

  private static boolean saveCompleteVirtualNetwork;

  /**
   * Convenience method used to test the existence of a scenario. If it exists, the user is asked if
   * he wants to overwrite the existent tables in the database. Returns true if the scenario number
   * is accepted.
   *
   * @param nodusProject The nodus project
   * @param scenario The ID of the scenario to assign.
   * @param confirm True if a dialog that asks to confirm overwriting must be displayed.
   * @return True if the scenario is accepted.
   */
  public static boolean acceptScenario(NodusProject nodusProject, int scenario, boolean confirm) {

    /** Save the complete virtual network or not */
    saveCompleteVirtualNetwork =
        Boolean.parseBoolean(nodusProject.getLocalProperty(NodusC.PROP_SAVE_ALL_VN));

    JDBCUtils jdbcUtils = new JDBCUtils(nodusProject.getMainJDBCConnection());

    // Build table name
    String tableName =
        nodusProject.getLocalProperty(NodusC.PROP_PROJECT_DOTNAME) + NodusC.SUFFIX_VNET;
    tableName = nodusProject.getLocalProperty(NodusC.PROP_VNET_TABLE, tableName) + scenario;
    tableName = jdbcUtils.getCompliantIdentifier(tableName);

    // If table doesn't exit, no problem
    if (!jdbcUtils.tableExists(tableName)) {
      return true;
    }

    if (confirm) {
      int answer =
          JOptionPane.showConfirmDialog(
              nodusProject.getNodusMapPanel(),
              i18n.get(VirtualNetworkWriter.class, "Clear_existent_flows", "Clear existent flows?"),
              i18n.get(
                  VirtualNetworkWriter.class, "Scenario_already_exists", "Scenario already exists"),
              JOptionPane.YES_NO_OPTION);

      if (answer != JOptionPane.YES_OPTION) {
        return false;
      }
    }

    return true;
  }

  /**
   * Initializes the database table used to store the virtual network that contains an assignment.
   *
   * @param nodusProject The Nodus project.
   * @param scenario The ID of the scenario for which the table must be created.
   * @param groups An array that contains the numbers of the groups of commodities to assign.
   * @return Return true on success.
   */
  public static boolean initTable(NodusProject nodusProject, int scenario, byte[] groups) {

    Connection jdbcConnection = nodusProject.getMainJDBCConnection();
    JDBCUtils jdbcUtils = new JDBCUtils(jdbcConnection);

    vNetTableName = nodusProject.getLocalProperty(NodusC.PROP_PROJECT_DOTNAME) + NodusC.SUFFIX_VNET;
    vNetTableName = nodusProject.getLocalProperty(NodusC.PROP_VNET_TABLE, vNetTableName) + scenario;
    vNetTableName = jdbcUtils.getCompliantIdentifier(vNetTableName);

    // Virtual network table
    jdbcUtils.dropTable(vNetTableName);

    JDBCField[] field = null;

    field = new JDBCField[12 + 3 * (groups.length + 1)];

    int idx = 0;
    /*
     * With the virtual network 3, create a table with line origin and the line destination.
     * With the virtual network 2, don't make any change.
     */
    field[idx++] = new JDBCField(NodusC.DBF_NODE1, "NUMERIC(11)");
    field[idx++] = new JDBCField(NodusC.DBF_LINK1, "NUMERIC(10)");
    field[idx++] = new JDBCField(NodusC.DBF_MODE1, "NUMERIC(2)");
    field[idx++] = new JDBCField(NodusC.DBF_MEANS1, "NUMERIC(2)");
    field[idx++] = new JDBCField(NodusC.DBF_SERVICE1, "NUMERIC(4)");
    field[idx++] = new JDBCField(NodusC.DBF_NODE2, "NUMERIC(11)");
    field[idx++] = new JDBCField(NodusC.DBF_LINK2, "NUMERIC(10)");
    field[idx++] = new JDBCField(NodusC.DBF_MODE2, "NUMERIC(2)");
    field[idx++] = new JDBCField(NodusC.DBF_MEANS2, "NUMERIC(2)");
    field[idx++] = new JDBCField(NodusC.DBF_SERVICE2, "NUMERIC(4)");
    field[idx++] = new JDBCField(NodusC.DBF_TIME, "NUMERIC(5,0)");
    field[idx++] = new JDBCField(NodusC.DBF_LENGTH, "NUMERIC(8,3)");

    // Detailed data per group
    for (byte element : groups) {
      field[idx++] = new JDBCField(NodusC.DBF_UNITCOST + element, "NUMERIC(13,3)");
      field[idx++] = new JDBCField(NodusC.DBF_QUANTITY + element, "NUMERIC(13,3)");
      field[idx++] = new JDBCField(NodusC.DBF_VEHICLES + element, "NUMERIC(10)");
    }

    // Consolidated data
    field[idx++] = new JDBCField(NodusC.DBF_UNITCOST, "NUMERIC(13,3)");
    field[idx++] = new JDBCField(NodusC.DBF_QUANTITY, "NUMERIC(13,3)");
    field[idx++] = new JDBCField(NodusC.DBF_VEHICLES, "NUMERIC(10)");

    if (!jdbcUtils.createTable(vNetTableName, field)) {
      return false;
    }

    return true;
  }

  private NodusProject nodusProject;

  private int scenario;

  private boolean tablesAreReady = false;

  private VirtualNetwork virtualNet;

  private static String vNetTableName;

  /**
   * Initializes a writer for a given main frame, a set of assignment parameters and a virtual
   * network.
   *
   * @param ap AssignmentParameters
   * @param vnet VirtualNetwork
   */
  public VirtualNetworkWriter(AssignmentParameters ap, VirtualNetwork vnet) {
    virtualNet = vnet;
    nodusProject = ap.getNodusProject();
    nodusMapPanel = nodusProject.getNodusMapPanel();
    scenario = ap.getScenario();
    SingleInstanceMessagePane.reset();
  }

  /**
   * Creates a new empty table in the database.
   *
   * @return True on success.
   */
  private boolean initTable() {

    if (tablesAreReady) {
      return true;
    }

    boolean result = initTable(nodusProject, scenario, virtualNet.getGroups());

    if (result) {
      tablesAreReady = true;
    }
    return result;
  }

  /**
   * Saves the content of the assignment in a database table.
   *
   * @return True on success.
   */
  public boolean save() {

    // Create or clear table at first iteration
    if (!initTable()) {
      return false;
    }

    int nbTimeSlices = virtualNet.getNbTimeSlices();
    int timeSliceDuration = virtualNet.getTimeSliceDuration();
    int assignmentStarTime = virtualNet.getAssignmentStartTime();

    // long start = System.currentTimeMillis();

    try {
      // Fill it
      Connection jdbcConnection = nodusProject.getMainJDBCConnection();

      /* Prepared statement */
      String sqlStmt = "INSERT INTO " + vNetTableName + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,";

      byte[] groups = virtualNet.getGroups();
      for (byte k = 0; k < (byte) groups.length; k++) {
        sqlStmt += "?,?,?,";
      }
      sqlStmt += "?,?,?)";
      PreparedStatement prepStmt = jdbcConnection.prepareStatement(sqlStmt);

      nodusMapPanel.startProgress(virtualNet.getNbVirtualLinks());

      VirtualNodeList[] vnl = virtualNet.getVirtualNodeLists();
      for (VirtualNodeList element : vnl) {
        // Iterate through all the virtual nodes generated for this real
        // node
        Iterator<VirtualNode> nodeLit = element.getVirtualNodeList().iterator();

        while (nodeLit.hasNext()) {
          VirtualNode vn = nodeLit.next();

          // Iterate through all the virtual links that start from
          // this virtual node
          Iterator<VirtualLink> linkLit = vn.getVirtualLinkList().iterator();

          while (linkLit.hasNext()) {
            if (!nodusMapPanel.updateProgress(
                i18n.get(
                    VirtualNetworkWriter.class,
                    "Saving_virtual_network",
                    "Saving virtual network"))) {

              prepStmt.close();

              return false;
            }

            VirtualLink vl = linkLit.next();

            // Only saves virtual links on which a flow was assigned
            for (byte timeSlice = 0; timeSlice < nbTimeSlices; timeSlice++) {
              int currentTime = assignmentStarTime + timeSlice * timeSliceDuration;

              if (vl.hasFlow(timeSlice) || saveCompleteVirtualNetwork) {

                int idx = 1;
                /*
                 * With the virtual network 3, insert in the table the line origin and the line
                 * destination. With the virtual network 2, don't make any change.
                 */
                prepStmt.setInt(idx++, vl.getBeginVirtualNode().getRealNodeId(true));
                prepStmt.setInt(idx++, vl.getBeginVirtualNode().getRealLinkId());
                prepStmt.setInt(idx++, vl.getBeginVirtualNode().getMode());
                prepStmt.setInt(idx++, vl.getBeginVirtualNode().getMeans());
                prepStmt.setInt(idx++, vl.getBeginVirtualNode().getService());
                prepStmt.setInt(idx++, vl.getEndVirtualNode().getRealNodeId(true));
                prepStmt.setInt(idx++, vl.getEndVirtualNode().getRealLinkId());
                prepStmt.setInt(idx++, vl.getEndVirtualNode().getMode());
                prepStmt.setInt(idx++, vl.getEndVirtualNode().getMeans());
                prepStmt.setInt(idx++, vl.getEndVirtualNode().getService());
                prepStmt.setInt(idx++, currentTime);
                prepStmt.setDouble(idx++, vl.getLength());

                double totalQty = 0.0;
                double averageWeight = 0.0;
                int totalVehicles = 0;

                for (byte k = 0; k < (byte) groups.length; k++) {

                  totalQty += vl.getCurrentFlow(k, timeSlice);
                  averageWeight += vl.getCurrentFlow(k, timeSlice) * vl.getWeight(k);
                  totalVehicles += vl.getCurrentVehicles(k, timeSlice);

                  prepStmt.setDouble(idx++, vl.getWeight(k));
                  prepStmt.setDouble(idx++, vl.getCurrentFlow(k, timeSlice));
                  prepStmt.setInt(idx++, vl.getCurrentVehicles(k, timeSlice));
                }

                if (totalQty > 0) {
                  averageWeight /= totalQty;
                }

                prepStmt.setDouble(idx++, averageWeight);
                prepStmt.setDouble(idx++, totalQty);
                prepStmt.setInt(idx++, totalVehicles);

                prepStmt.executeUpdate();
              }
            }
          }
        }
      }

      prepStmt.close();
      if (!jdbcConnection.getAutoCommit()) {
        jdbcConnection.commit();
      }

    } catch (Exception e) {
      nodusMapPanel.stopProgress();
      SingleInstanceMessagePane.display(
          nodusProject.getNodusMapPanel(),
          i18n.get(
              VirtualNetworkWriter.class,
              "Invalid_value",
              "Invalid value in VNET fields. See Stack Trace."),
          JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
      return false;
    }

    // long end = System.currentTimeMillis();
    // System.out.println("Duration : " + ((end - start) / 1000) + " seconds
    // for " + nbRecords + " records");
    nodusMapPanel.stopProgress();

    return true;
  }

  /**
   * Returns the name of the virtual network table.
   *
   * @return The name of the table.
   */
  public static String getTableName() {
    return vNetTableName;
  }
}

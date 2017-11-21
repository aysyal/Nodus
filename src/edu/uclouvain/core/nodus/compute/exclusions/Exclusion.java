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

package edu.uclouvain.core.nodus.compute.exclusions;

/**
 * Simple class that maintains information related to an exclusion for a given node.
 *
 * @author Bart Jourquin
 */
public class Exclusion {

  /** Group to which this exclusion belongs. -1 if true for any group */
  private int group;

  /** Origin means of the movement to exclude. -1 if applicable to any means */
  private int means1;

  /** Destination means of the movement to exclude. -1 if applicable to any means */
  private int means2;

  /** Origin mode of the movement to exclude. -1 if applicable to any mode */
  private int mode1;

  /** Destination mode of the movement to exclude. -1 if applicable to any mode */
  private int mode2;

  /** Node number to which this exclusion belongs to. */
  private int nodeId;

  /**
   * Creates a new exclusion for a give group, node number and a pair of two mode/means
   * combinations.
   *
   * @param group Group of goods.
   * @param nodeId Real node num.
   * @param mode1 Mode at the origin.
   * @param means1 Means at the origin.
   * @param mode2 Mode at the destination.
   * @param means2 Means at the destination.
   */
  public Exclusion(int group, int nodeId, int mode1, int means1, int mode2, int means2) {
    this.group = group;
    this.nodeId = nodeId;
    this.mode1 = mode1;
    this.means1 = means1;
    this.mode2 = mode2;
    this.means2 = means2;
  }

  /**
   * Returns true if the operation relative to the given group and mode/means combinations is not
   * permitted at node num.
   *
   * @param group Group of goods.
   * @param nodeId Real node ID.
   * @param mode1 Mode at the origin.
   * @param means1 Means at the origin.
   * @param mode2 Mode at the destination.
   * @param means2 Means at the destination.
   * @return boolean True if excluded.
   */
  public boolean isExcluded(int group, int nodeId, int mode1, int means1, int mode2, int means2) {
    // Does the pattern concern the relevant node?
    if (nodeId != this.nodeId) {
      return false;
    }

    // Does the pattern concern the relevant group or is this exclusion
    // relative to all groups?
    if (group != this.group && this.group != -1) {
      return false;
    }

    // Test the exclusion
    int firstMode = this.mode1;

    if (firstMode == -1) {
      firstMode = mode1; // If M1 is relative to all modes
    }

    int firstMeans = this.means1;

    if (firstMeans == -1) {
      firstMeans = means1;
    }

    int secondMode = this.mode2;

    if (secondMode == -1) {
      secondMode = mode2;
    }

    int secondMeans = this.means2;

    if (secondMeans == -1) {
      secondMeans = means2;
    }

    if (firstMode == mode1
        && firstMeans == means1
        && secondMode == mode2
        && secondMeans == means2) {
      return true;
    }

    if (firstMode == mode2
        && firstMeans == means2
        && secondMode == mode1
        && secondMeans == means1) {
      return true;
    }

    return false;
  }
}

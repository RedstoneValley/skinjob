/*
 * Copyright (c) 1997, 1999, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.awt.geom;

import java.util.NoSuchElementException;

/**
 * A utility class to iterate over the path segments of a line segment through the PathIterator
 * interface.
 *
 * @author Jim Graham
 */
class LineIterator implements PathIterator {
  final Line2D line;
  final AffineTransform affine;
  int index;

  LineIterator(Line2D l, AffineTransform at) {
    line = l;
    affine = at;
  }

  /**
   * Return the winding rule for determining the insideness of the path.
   *
   * @see #WIND_EVEN_ODD
   * @see #WIND_NON_ZERO
   */
  @Override
  public int getWindingRule() {
    return WIND_NON_ZERO;
  }

  /**
   * Tests if there are more points to read.
   *
   * @return true if there are more points to read
   */
  @Override
  public boolean isDone() {
    return index > 1;
  }

  /**
   * Moves the iterator to the next segment of the path forwards along the primary direction of
   * traversal as long as there are more points in that direction.
   */
  @Override
  public void next() {
    index++;
  }

  /**
   * Returns the coordinates and type of the current path segment in the iteration. The return value
   * is the path segment type: SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE. A
   * float array of length 6 must be passed in and may be used to store the coordinates of the
   * point(s). Each point is stored as a pair of float x,y coordinates. SEG_MOVETO and SEG_LINETO
   * types will return one point, SEG_QUADTO will return two points, SEG_CUBICTO will return 3
   * points and SEG_CLOSE will not return any points.
   *
   * @see #SEG_MOVETO
   * @see #SEG_LINETO
   * @see #SEG_QUADTO
   * @see #SEG_CUBICTO
   * @see #SEG_CLOSE
   */
  @Override
  public int currentSegment(float[] coords) {
    if (isDone()) {
      throw new NoSuchElementException("line iterator out of bounds");
    }
    int type;
    if (index == 0) {
      coords[0] = (float) line.getX1();
      coords[1] = (float) line.getY1();
      type = SEG_MOVETO;
    } else {
      coords[0] = (float) line.getX2();
      coords[1] = (float) line.getY2();
      type = SEG_LINETO;
    }
    if (affine != null) {
      affine.transform(coords, 0, coords, 0, 1);
    }
    return type;
  }

  /**
   * Returns the coordinates and type of the current path segment in the iteration. The return value
   * is the path segment type: SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE. A
   * double array of length 6 must be passed in and may be used to store the coordinates of the
   * point(s). Each point is stored as a pair of double x,y coordinates. SEG_MOVETO and SEG_LINETO
   * types will return one point, SEG_QUADTO will return two points, SEG_CUBICTO will return 3
   * points and SEG_CLOSE will not return any points.
   *
   * @see #SEG_MOVETO
   * @see #SEG_LINETO
   * @see #SEG_QUADTO
   * @see #SEG_CUBICTO
   * @see #SEG_CLOSE
   */
  @Override
  public int currentSegment(double[] coords) {
    if (isDone()) {
      throw new NoSuchElementException("line iterator out of bounds");
    }
    int type;
    if (index == 0) {
      coords[0] = line.getX1();
      coords[1] = line.getY1();
      type = SEG_MOVETO;
    } else {
      coords[0] = line.getX2();
      coords[1] = line.getY2();
      type = SEG_LINETO;
    }
    if (affine != null) {
      affine.transform(coords, 0, coords, 0, 1);
    }
    return type;
  }
}

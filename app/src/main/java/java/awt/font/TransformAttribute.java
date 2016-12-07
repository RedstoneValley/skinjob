/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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

/*
 * (C) Copyright Taligent, Inc. 1996 - 1997, All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998, All Rights Reserved
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by Taligent, Inc., a wholly-owned subsidiary
 * of IBM. These materials are provided under terms of a License
 * Agreement between Taligent and Sun. This technology is protected
 * by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.awt.font;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * The {@code TransformAttribute} class provides an immutable wrapper for a transform so that it is
 * safe to use as an attribute.
 */
public final class TransformAttribute implements Serializable {

  /**
   * A {@code TransformAttribute} representing the identity transform.
   *
   * @since 1.6
   */
  public static final TransformAttribute IDENTITY = new TransformAttribute(null);
  // Added for serial backwards compatibility (4348425)
  static final long serialVersionUID = 3356247357827709530L;
  /**
   * The {@code AffineTransform} for this {@code TransformAttribute}, or {@code null} if {@code
   * AffineTransform} is the identity transform.
   */
  private AffineTransform transform;

  /**
   * Wraps the specified transform.  The transform is cloned and a reference to the clone is kept.
   * The original transform is unchanged. If null is passed as the argument, this constructor
   * behaves as though it were the identity transform.  (Note that it is preferable to use {@link
   * #IDENTITY} in this case.)
   *
   * @param transform the specified {@link AffineTransform} to be wrapped, or null.
   */
  public TransformAttribute(AffineTransform transform) {
    if (transform != null && !transform.isIdentity()) {
      this.transform = new AffineTransform(transform);
    }
  }

  /**
   * Returns a copy of the wrapped transform.
   *
   * @return a {@code AffineTransform} that is a copy of the wrapped transform of this {@code
   * TransformAttribute}.
   */
  public AffineTransform getTransform() {
    AffineTransform at = transform;
    return at == null ? new AffineTransform() : new AffineTransform(at);
  }

  /**
   * Returns {@code true} if the wrapped transform is an identity transform.
   *
   * @return {@code true} if the wrapped transform is an identity transform; {@code false}
   * otherwise.
   * @since 1.4
   */
  public boolean isIdentity() {
    return transform == null;
  }

  private void writeObject(ObjectOutputStream s) throws ClassNotFoundException, IOException {
    // sigh -- 1.3 expects transform is never null, so we need to always write one out
    if (transform == null) {
      transform = new AffineTransform();
    }
    s.defaultWriteObject();
  }

  /*
   * @since 1.6
   */
  private Object readResolve() throws ObjectStreamException {
    if (transform == null || transform.isIdentity()) {
      return IDENTITY;
    }
    return this;
  }

  /**
   * @since 1.6
   */
  @SuppressWarnings("NonFinalFieldReferencedInHashCode")
  public int hashCode() {
    return transform == null ? 0 : transform.hashCode();
  }

  /**
   * Returns {@code true} if rhs is a {@code TransformAttribute} whose transform is equal to this
   * {@code TransformAttribute}'s transform.
   *
   * @param rhs the object to compare to
   * @return {@code true} if the argument is a {@code TransformAttribute} whose transform is equal
   * to this {@code TransformAttribute}'s transform.
   * @since 1.6
   */
  @SuppressWarnings("NonFinalFieldReferenceInEquals")
  public boolean equals(Object rhs) {
    if (rhs != null) {
      try {
        TransformAttribute that = (TransformAttribute) rhs;
        if (transform == null) {
          return that.transform == null;
        }
        return transform.equals(that.transform);
      } catch (ClassCastException e) {
      }
    }
    return false;
  }
}

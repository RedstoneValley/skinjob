/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.dnd;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Tests if an object can truly be serialized by serializing it to a null OutputStream.
 *
 * @since 1.4
 */
final class SerializationTester {
  private static final ObjectOutputStream stream;

  static {
    try {
      stream = new ObjectOutputStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
      });
    } catch (IOException cannotHappen) {
      throw new RuntimeException(cannotHappen);
    }
  }

  private SerializationTester() {
  }

  static boolean test(Object obj) {
    if (!(obj instanceof Serializable)) {
      return false;
    }

    try {
      stream.writeObject(obj);
    } catch (IOException e) {
      return false;
    } finally {
      // Fix for 4503661.
      // Reset the stream so that it doesn't keep a reference to the
      // written object.
      try {
        stream.reset();
      } catch (IOException e) {
        // Ignore the exception.
      }
    }
    return true;
  }
}

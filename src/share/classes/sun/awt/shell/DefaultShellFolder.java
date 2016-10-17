/*
 * Copyright (c) 2000, 2002, Oracle and/or its affiliates. All rights reserved.
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

package sun.awt.shell;

import java.io.File;
import java.io.ObjectStreamException;

/**
 * @author Michael Martak
 * @since 1.4
 */

class DefaultShellFolder extends ShellFolder {

  /**
   * Create a file system shell folder from a file
   */
  DefaultShellFolder(ShellFolder parent, File f) {
    super(parent, f.getAbsolutePath());
  }

  /**
   * This method is implemented to make sure that no instances
   * of {@code ShellFolder} are ever serialized. An instance of
   * this default implementation can always be represented with a
   * {@code java.io.File} object instead.
   *
   * @returns a {@code java.io.File} replacement object.
   */
  @Override
  protected Object writeReplace() throws ObjectStreamException {
    return new File(getPath());
  }

  /**
   * @return An array of shell folders that are children of this shell folder
   * object, null if this shell folder is empty.
   */
  @Override
  public File[] listFiles() {
    File[] files = super.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        files[i] = new DefaultShellFolder(this, files[i]);
      }
    }
    return files;
  }

  /**
   * @return Whether this shell folder is a link
   */
  @Override
  public boolean isLink() {
    return false; // Not supported by default
  }

  /**
   * @return The shell folder linked to by this shell folder, or null
   * if this shell folder is not a link
   */
  @Override
  public ShellFolder getLinkLocation() {
    return null; // Not supported by default
  }

  /**
   * @return The name used to display this shell folder
   */
  @Override
  public String getDisplayName() {
    return getName();
  }

  /**
   * @return The type of shell folder as a string
   */
  @Override
  public String getFolderType() {
    return isDirectory() ? "File Folder" : "File";
  }

  /**
   * @return The executable type as a string
   */
  @Override
  public String getExecutableType() {
    return null; // Not supported by default
  }

  /**
   * @return Whether this shell folder is marked as hidden
   */
  @Override
  public boolean isHidden() {
    String fileName = getName();
    if (!fileName.isEmpty()) {
      return fileName.charAt(0) == '.';
    }
    return false;
  }
}

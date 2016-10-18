/*
 * Copyright (c) 2000, 2010, Oracle and/or its affiliates. All rights reserved.
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

package sun.java2d;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Locale;
import sun.font.FontManager;

/**
 * Headless decorator implementation of a SunGraphicsEnvironment
 */

public class HeadlessGraphicsEnvironment extends GraphicsEnvironment {

  private final GraphicsEnvironment ge;

  public HeadlessGraphicsEnvironment(GraphicsEnvironment ge) {
    this.ge = ge;
  }

  @Override
  public GraphicsDevice[] getScreenDevices() throws HeadlessException {
    throw new HeadlessException();
  }

  @Override
  public GraphicsDevice getDefaultScreenDevice() throws HeadlessException {
    throw new HeadlessException();
  }

  @Override
  public Graphics2D createGraphics(BufferedImage img) {
    return ge.createGraphics(img);
  }

  @Override
  public Font[] getAllFonts() {
    return ge.getAllFonts();
  }

  public String[] getAvailableFontFamilyNames() {
    return ge.getAvailableFontFamilyNames();
  }

  public String[] getAvailableFontFamilyNames(Locale l) {
    return ge.getAvailableFontFamilyNames(l);
  }

  public Point getCenterPoint() throws HeadlessException {
    throw new HeadlessException();
  }

  public Rectangle getMaximumWindowBounds() throws HeadlessException {
    throw new HeadlessException();
  }

  /* Used by FontManager : internal API */
  public GraphicsEnvironment getSunGraphicsEnvironment() {
    return ge;
  }

  @Override
  public boolean isHeadlessInstance() {
    return false;
  }

  @Override
  public boolean registerFont(Font font) {
    if (font == null) {
      throw new NullPointerException("font cannot be null.");
    }
    FontManager fm = FontManager.getInstance();
    return fm.registerFont(font);
  }

  @Override
  public void preferLocaleFonts() {
    FontManager fm = FontManager.getInstance();
    fm.preferLocaleFonts();
  }

  @Override
  public void preferProportionalFonts() {
    FontManager fm = FontManager.getInstance();
    fm.preferProportionalFonts();
  }
}

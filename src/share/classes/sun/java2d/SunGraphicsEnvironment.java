/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.AWTError;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.SkinJob;
import java.awt.image.BufferedImage;
import java.awt.peer.ComponentPeer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Locale;
import java.util.TreeMap;
import sun.awt.DisplayChangedListener;
import sun.awt.OSInfo;
import sun.awt.SunDisplayChanger;
import sun.font.FontManager;

/**
 * This is an implementation of a GraphicsEnvironment object for the
 * default local GraphicsEnvironment.
 *
 * @see GraphicsDevice
 * @see GraphicsConfiguration
 */
public abstract class SunGraphicsEnvironment extends GraphicsEnvironment
    implements DisplayChangedListener {

  public static boolean isOpenSolaris;
  protected GraphicsDevice[] screens;
  protected final SunDisplayChanger displayChanger = new SunDisplayChanger();

  public SunGraphicsEnvironment() {
    String version = System.getProperty(OSInfo.OS_VERSION, "0.0");
    try {
      float ver = Float.parseFloat(version);
      if (ver > 5.10f) {
        File f = new File("/etc/release");
        FileInputStream fis = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        if (line.contains("OpenSolaris")) {
          isOpenSolaris = true;
        } else {
          /* We are using isOpenSolaris as meaning we know the Solaris commercial fonts aren't
           * present. "Solaris Next" (03/10) did not include these even though its was not
           * OpenSolaris. Need to revisit how this is handled but for now as in 6ux, we'll use
           * the test for a standard font resource as being an indicator as to whether we need
           * to treat this as OpenSolaris from a font config perspective.
           */
          String courierNew = "/usr/openwin/lib/X11/fonts/TrueType/CourierNew.ttf";
          File courierFile = new File(courierNew);
          isOpenSolaris = !courierFile.exists();
        }
        fis.close();
      }
    } catch (Exception e) {
    }
  }

  /* Modifies the behaviour of a subsequent call to preferLocaleFonts()
   * to use Mincho instead of Gothic for dialoginput in JA locales
   * on windows. Not needed on other platforms.
   *
   * DO NOT MOVE OR RENAME OR OTHERWISE ALTER THIS METHOD.
   * ITS USED BY SOME NON-JRE INTERNAL CODE.
   */
  public static void useAlternateFontforJALocales() {
    FontManager.getInstance().useAlternateFontforJALocales();
  }

  /**
   * Returns an array of all of the screen devices.
   */
  @Override
  public synchronized GraphicsDevice[] getScreenDevices() {
    GraphicsDevice[] ret = screens;
    if (ret == null) {
      int num = getNumScreens();
      ret = new GraphicsDevice[num];
      for (int i = 0; i < num; i++) {
        ret[i] = makeScreenDevice(i);
      }
      screens = ret;
    }
    return ret;
  }

  /**
   * Returns the default screen graphics device.
   */
  @Override
  public GraphicsDevice getDefaultScreenDevice() {
    GraphicsDevice[] screens = getScreenDevices();
    if (screens.length == 0) {
      throw new AWTError("no screen devices");
    }
    return screens[0];
  }

  /**
   * Returns a Graphics2D object for rendering into the
   * given BufferedImage.
   *
   * @throws NullPointerException if BufferedImage argument is null
   */
  @Override
  public Graphics2D createGraphics(BufferedImage img) {
    if (img == null) {
      throw new NullPointerException("BufferedImage cannot be null");
    }
    SurfaceData sd = SurfaceData.getPrimarySurfaceData(img);
    return new SunGraphics2D(sd, Color.white, Color.black, SkinJob.defaultFont);
  }

  /**
   * Returns all fonts available in this environment.
   */
  @Override
  public Font[] getAllFonts() {
    FontManager fm = FontManager.getInstance();
    Font[] installedFonts = fm.getAllInstalledFonts();
    Font[] created = fm.getCreatedFonts();
    if (created == null || created.length == 0) {
      return installedFonts;
    } else {
      int newlen = installedFonts.length + created.length;
      Font[] fonts = Arrays.copyOf(installedFonts, newlen);
      System.arraycopy(created, 0, fonts, installedFonts.length, created.length);
      return fonts;
    }
  }

  @Override
  public String[] getAvailableFontFamilyNames() {
    return getAvailableFontFamilyNames(Locale.getDefault());
  }

  @Override
  public String[] getAvailableFontFamilyNames(Locale requestedLocale) {
    FontManager fm = FontManager.getInstance();
    String[] installed = fm.getInstalledFontFamilyNames(requestedLocale);
        /* Use a new TreeMap as used in getInstalledFontFamilyNames
         * and insert all the keys in lower case, so that the sort order
         * is the same as the installed families. This preserves historical
         * behaviour and inserts new families in the right place.
         * It would have been marginally more efficient to directly obtain
         * the tree map and just insert new entries, but not so much as
         * to justify the extra internal interface.
         */
    TreeMap<String, String> map = fm.getCreatedFontFamilyNames();
    if (map == null || map.isEmpty()) {
      return installed;
    } else {
      for (String anInstalled : installed) {
        map.put(anInstalled.toLowerCase(requestedLocale), anInstalled);
      }
      String[] retval = new String[map.size()];
      Object[] keyNames = map.keySet().toArray();
      for (int i = 0; i < keyNames.length; i++) {
        retval[i] = map.get(keyNames[i]);
      }
      return retval;
    }
  }

  /**
   * Returns the number of screen devices of this graphics environment.
   *
   * @return the number of screen devices of this graphics environment
   */
  protected abstract int getNumScreens();

  /**
   * Create and return the screen device with the specified number. The
   * device with number {@code 0} will be the default device (returned
   * by {@link #getDefaultScreenDevice()}.
   *
   * @param screennum the number of the screen to create
   * @return the created screen device
   */
  protected abstract GraphicsDevice makeScreenDevice(int screennum);

  /**
   * From the DisplayChangedListener interface; called
   * when the display mode has been changed.
   */
  @Override
  public void displayChanged() {
    // notify screens in device array to do display update stuff
    for (GraphicsDevice gd : getScreenDevices()) {
      if (gd instanceof DisplayChangedListener) {
        ((DisplayChangedListener) gd).displayChanged();
      }
    }

    // notify SunDisplayChanger list (e.g. VolatileSurfaceManagers and
    // SurfaceDataProxies) about the display change event
    displayChanger.notifyListeners();
  }

  /**
   * Part of the DisplayChangedListener interface:
   * propagate this event to listeners
   */
  @Override
  public void paletteChanged() {
    displayChanger.notifyPaletteChanged();
  }

    /*
     * ----DISPLAY CHANGE SUPPORT----
     */

  /**
   * Add a DisplayChangeListener to be notified when the display settings
   * are changed.
   */
  public void addDisplayChangedListener(DisplayChangedListener client) {
    displayChanger.add(client);
  }

  /*
     * ----END DISPLAY CHANGE SUPPORT----
     */

  /**
   * Returns true if FlipBufferStrategy with COPIED buffer contents
   * is preferred for this peer's GraphicsConfiguration over
   * BlitBufferStrategy, false otherwise.
   * <p>
   * The reason FlipBS could be preferred is that in some configurations
   * an accelerated copy to the screen is supported (like Direct3D 9)
   *
   * @return true if flip strategy should be used, false otherwise
   */
  public boolean isFlipStrategyPreferred(ComponentPeer peer) {
    return false;
  }
}

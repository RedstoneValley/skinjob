/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.awt;

import android.util.Log;

import java.awt.Dialog.ModalExclusionType;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.im.InputContext;
import java.awt.image.BufferStrategy;
import java.awt.peer.ComponentPeer;
import java.awt.peer.WindowPeer;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import skinjob.SkinJobGlobals;
import skinjob.internal.SkinJobInputContext;
import skinjob.util.SkinJobUtil;
import sun.awt.AppContext;
import sun.awt.CausedFocusEvent.Cause;
import sun.awt.SunToolkit;
import sun.awt.util.IdentityArrayList;

/**
 * A {@code Window} object is a top-level window with no borders and no menubar. The default layout
 * for a window is {@code BorderLayout}.
 * <p>
 * A window must have either a frame, dialog, or another window defined as its owner when it's
 * constructed.
 * <p>
 * In a multi-screen environment, you can create a {@code Window} on a different screen device by
 * constructing the {@code Window} with {@link #Window(Window, GraphicsConfiguration)}.  The {@code
 * GraphicsConfiguration} object is one of the {@code GraphicsConfiguration} objects of the target
 * screen device.
 * <p>
 * In a virtual device multi-screen environment in which the desktop area could span multiple
 * physical screen devices, the bounds of all configurations are relative to the virtual device
 * coordinate system. The origin of the virtual-coordinate system is at the upper left-hand corner
 * of the primary physical screen.  Depending on the location of the primary screen in the virtual
 * device, negative coordinates are possible, as shown in the following figure.
 * <p>
 * <img src="doc-files/MultiScreen.gif" alt="Diagram shows virtual device containing 4 physical
 * screens. Primary physical screen shows coords (0,0), other screen shows (-80,-100)."
 * style="float:center; margin: 7px 10px;">
 * <p>
 * In such an environment, when calling {@code setLocation}, you must pass a virtual coordinate to
 * this method.  Similarly, calling {@code getLocationOnScreen} on a {@code Window} returns virtual
 * device coordinates.  Call the {@code getBounds} method of a {@code GraphicsConfiguration} to find
 * its origin in the virtual coordinate system.
 * <p>
 * The following code sets the location of a {@code Window} at (10, 10) relative to the origin of
 * the physical screen of the corresponding {@code GraphicsConfiguration}.  If the bounds of the
 * {@code GraphicsConfiguration} is not taken into account, the {@code Window} location would be set
 * at (10, 10) relative to the virtual-coordinate system and would appear on the primary physical
 * screen, which might be different from the physical screen of the specified {@code
 * GraphicsConfiguration}.
 * <p>
 * <pre>
 *      Window w = new Window(Window owner, GraphicsConfiguration gc);
 *      Rectangle bounds = gc.getBounds();
 *      w.setLocation(10 + bounds.x, 10 + bounds.y);
 * </pre>
 * <p>
 * <p>
 * Note: the location and size of top-level windows (including {@code Window}s, {@code Frame}s, and
 * {@code Dialog}s) are under the control of the desktop's window management system. Calls to {@code
 * setLocation}, {@code setSize}, and {@code setBounds} are requests (not directives) which are
 * forwarded to the window management system.  Every effort will be made to honor such requests.
 * However, in some cases the window management system may ignore such requests, or modify the
 * requested geometry in order to place and size the {@code Window} in a way that more closely
 * matches the desktop settings.
 * <p>
 * Due to the asynchronous nature of native event handling, the results returned by {@code
 * getBounds}, {@code getLocation}, {@code getLocationOnScreen}, and {@code getSize} might not
 * reflect the actual geometry of the Window on screen until the last request has been processed.
 * During the processing of subsequent requests these values might change accordingly while the
 * window management system fulfills the requests.
 * <p>
 * An application may set the size and location of an invisible {@code Window} arbitrarily, but the
 * window management system may subsequently change its size and/or location when the {@code Window}
 * is made visible. One or more {@code ComponentEvent}s will be generated to indicate the new
 * geometry.
 * <p>
 * Windows are capable of generating the following WindowEvents: WindowOpened, WindowClosed,
 * WindowGainedFocus, WindowLostFocus.
 *
 * @author Sami Shaio
 * @author Arthur van Hoff
 * @see WindowEvent
 * @see #addWindowListener
 * @see BorderLayout
 * @since JDK1.0
 */
@SuppressWarnings("unchecked")
public class Window extends Container {

  static final int OPENED = 0x01;
  static final boolean systemSyncLWRequests;
  private static final String TAG = "java.awt.Window";
  /**
   * Contains all the windows that have a peer object associated, i. e. between addNotify() and
   * removeNotify() calls. The list of all Window instances can be obtained from AppContext object.
   *
   * @since 1.6
   */
  private static final IdentityArrayList<Window> allWindows = new IdentityArrayList<>();
  private static final String base = "win";
  /*
   * JDK 1.1 serialVersionUID
   */
  private static final long serialVersionUID = 4497834738069338734L;
  private static final boolean locationByPlatformProp;
  private static final AtomicBoolean beforeFirstWindowShown = new AtomicBoolean(true);
  private static int nameCounter;
  private transient InputContext sjInputContext;

  static {
    String s = System.getProperty("java.awt.syncLWRequests");
    systemSyncLWRequests = Boolean.valueOf(s);
    s = System.getProperty("java.awt.Window.locationByPlatform");
    locationByPlatformProp = Boolean.valueOf(s);
  }

  /**
   * A vector containing all the windows this window currently owns.
   *
   * @see #getOwnedWindows
   * @since 1.2
   */
  final transient Vector<WeakReference<Window>> ownedWindowList
      = new Vector<>();
  final transient Object inputContextLock = new Object();
  public android.view.Window sjAndroidWindow;
  /**
   * This represents the warning message that is to be displayed in a non secure window. ie : a
   * window that has a security manager installed that denies {@code AWTPermission("showWindowWithoutWarningBanner")}.
   * This message can be displayed anywhere in the window.
   *
   * @serial
   * @see #getWarningString
   */
  String warningString;
  /**
   * {@code icons} is the graphical way we can represent the frames and dialogs. {@code Window}
   * can't display icon but it's being inherited by owned {@code Dialog}s.
   *
   * @serial
   * @see #getIconImages
   * @see #setIconImages
   */
  transient java.util.List<Image> icons;
  boolean syncLWRequests;
  transient boolean beforeFirstShow = true;
  transient WindowDisposerRecord disposerRecord;
  /**
   * An Integer value representing the Window State.
   *
   * @serial
   * @see #show
   * @since 1.2
   */
  int state;
  transient boolean showWithParent;
  /**
   * Contains the modal dialog that blocks this window, or null if the window is unblocked.
   *
   * @since 1.6
   */
  transient Dialog modalBlocker;
  /**
   * @serial
   * @see ModalExclusionType
   * @see #getModalExclusionType
   * @see #setModalExclusionType
   * @since 1.6
   */
  ModalExclusionType modalExclusionType;
  transient WindowListener windowListener;
  transient WindowStateListener windowStateListener;
  transient WindowFocusListener windowFocusListener;
  transient InputContext inputContext;
  /*
     * Indicates that this window is being shown. This flag is set to true at
     * the beginning of show() and to false at the end of show().
     *
     * @see #show()
     * @see Dialog#shouldBlock
     */
  transient boolean isInShow;
  transient boolean isTrayIconWindow;
  transient Object anchor = new Object();
  transient boolean disposing;
  /*
     * We insert a weak reference into the Vector of all Windows in AppContext
     * instead of 'this' so that garbage collection can still take place
     * correctly.
     */
  transient WeakReference<Window> weakThis;
  /**
   * Indicates whether this window should receive focus on subsequently being shown (with a call to
   * {@code setVisible(true)}), or being moved to the front (with a call to {@code toFront()}).
   *
   * @serial
   * @see #setAutoRequestFocus
   * @see #isAutoRequestFocus
   * @since 1.7
   */
  volatile boolean autoRequestFocus = true;
  /**
   * The opacity level of the window
   *
   * @serial
   * @see #setOpacity(float)
   * @see #getOpacity()
   * @since 1.7
   */
  float opacity = 1.0f;
  /**
   * These fields are initialized in the native peer code or via AWTAccessor's WindowAccessor.
   */
  transient volatile int securityWarningWidth;
  transient volatile int securityWarningHeight;
  /**
   * These fields represent the desired location for the security warning if this window is
   * untrusted. See com.sun.awt.SecurityWarning for more details.
   */
  transient double securityWarningPointX = 2.0;
  transient double securityWarningPointY;
  transient float securityWarningAlignmentX = RIGHT_ALIGNMENT;
  transient float securityWarningAlignmentY = TOP_ALIGNMENT;
  /**
   * Holds the reference to the component which last had focus in this window before it lost focus.
   */
  private transient Component temporaryLostComponent;
  /**
   * A boolean value representing Window always-on-top state
   *
   * @serial
   * @see #setAlwaysOnTop
   * @see #isAlwaysOnTop
   * @since 1.5
   */
  private boolean alwaysOnTop;
  /**
   * Unused. Maintained for serialization backward-compatibility.
   *
   * @serial
   * @since 1.2
   */
  private FocusManager focusMgr;
  /**
   * Indicates whether this Window can become the focused Window.
   *
   * @serial
   * @see #getFocusableWindowState
   * @see #setFocusableWindowState
   * @since 1.4
   */
  private boolean focusableWindowState = true;
  /**
   * The shape assigned to this window. This field is set to {@code null} if no shape is set
   * (rectangular window).
   *
   * @serial
   * @see #getShape()
   * @see #setShape(Shape)
   * @since 1.7
   */
  private Shape shape;
  /**
   * Window type.
   * <p>
   * Synchronization: ObjectLock
   */
  private Type type = Type.NORMAL;
  /**
   * The window serialized data version.
   *
   * @serial
   */
  private int windowSerializedDataVersion = 2;
  private boolean locationByPlatform = locationByPlatformProp;

  /**
   * Constructs a new, initially invisible window in default size with the specified {@code
   * GraphicsConfiguration}.
   *
   * @param gc the {@code GraphicsConfiguration} of the target screen device. If {@code gc} is
   * {@code null}, the system default {@code GraphicsConfiguration} is assumed
   * @throws IllegalArgumentException if {@code gc} is not from a screen device
   * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
   * @see GraphicsEnvironment#isHeadless
   */
  Window(GraphicsConfiguration gc) {
    init(gc);
  }

  /**
   * Constructs a new, initially invisible window in the default size.
   * <p>
   * If there is a security manager set, it is invoked to check {@code
   * AWTPermission("showWindowWithoutWarningBanner")}. If that check fails with a {@code
   * SecurityException} then a warning banner is created.
   */
  Window() throws HeadlessException {
    this((GraphicsConfiguration) null);
  }

  /**
   * Constructs a new, initially invisible window with the specified {@code Frame} as its owner. The
   * window will not be focusable unless its owner is showing on the screen.
   * <p>
   * If there is a security manager set, it is invoked to check {@code
   * AWTPermission("showWindowWithoutWarningBanner")}. If that check fails with a {@code
   * SecurityException} then a warning banner is created.
   *
   * @param owner the {@code Frame} to act as owner or {@code null} if this window has no owner
   * @throws IllegalArgumentException if the {@code owner}'s {@code GraphicsConfiguration} is not
   * from a screen device
   * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless} returns {@code true}
   * @see GraphicsEnvironment#isHeadless
   * @see #isShowing
   */
  public Window(Frame owner) {
    this(owner == null ? null : owner.getGraphicsConfiguration());
    ownedInit(owner);
  }

  /**
   * Constructs a new, initially invisible window with the specified {@code Window} as its owner.
   * This window will not be focusable unless its nearest owning {@code Frame} or {@code Dialog} is
   * showing on the screen.
   * <p>
   * If there is a security manager set, it is invoked to check {@code
   * AWTPermission("showWindowWithoutWarningBanner")}. If that check fails with a {@code
   * SecurityException} then a warning banner is created.
   *
   * @param owner the {@code Window} to act as owner or {@code null} if this window has no owner
   * @throws IllegalArgumentException if the {@code owner}'s {@code GraphicsConfiguration} is not
   * from a screen device
   * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
   * @see GraphicsEnvironment#isHeadless
   * @see #isShowing
   * @since 1.2
   */
  public Window(Window owner) {
    this(owner == null ? null : owner.getGraphicsConfiguration());
    ownedInit(owner);
  }

  /**
   * Constructs a new, initially invisible window with the specified owner {@code Window} and a
   * {@code GraphicsConfiguration} of a screen device. The Window will not be focusable unless its
   * nearest owning {@code Frame} or {@code Dialog} is showing on the screen.
   * <p>
   * If there is a security manager set, it is invoked to check {@code
   * AWTPermission("showWindowWithoutWarningBanner")}. If that check fails with a {@code
   * SecurityException} then a warning banner is created.
   *
   * @param owner the window to act as owner or {@code null} if this window has no owner
   * @param gc the {@code GraphicsConfiguration} of the target screen device; if {@code gc} is
   * {@code null}, the system default {@code GraphicsConfiguration} is assumed
   * @throws IllegalArgumentException if {@code gc} is not from a screen device
   * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
   * @see GraphicsEnvironment#isHeadless
   * @see GraphicsConfiguration#getBounds
   * @see #isShowing
   * @since 1.3
   */
  public Window(Window owner, GraphicsConfiguration gc) {
    this(gc);
    ownedInit(owner);
  }

  static void updateChildFocusableWindowState(Window w) {
    if (w.getPeer() != null && w.isShowing()) {
      ((WindowPeer) w.getPeer()).updateFocusableWindowState();
    }
    for (int i = 0; i < w.ownedWindowList.size(); i++) {
      Window child = w.ownedWindowList.elementAt(i).get();
      if (child != null) {
        updateChildFocusableWindowState(child);
      }
    }
  }

  /*
     * Returns a list of all displayable Windows, i. e. all the
     * Windows which peer is not null.
     *
     * @see #addNotify
     * @see #removeNotify
     */
  static IdentityArrayList<Window> getAllWindows() {
    synchronized (allWindows) {
      IdentityArrayList<Window> v = new IdentityArrayList<>();
      v.addAll(allWindows);
      return v;
    }
  }

  static IdentityArrayList<Window> getAllUnblockedWindows() {
    synchronized (allWindows) {
      IdentityArrayList<Window> unblocked = new IdentityArrayList<>();
      for (int i = 0; i < allWindows.size(); i++) {
        Window w = allWindows.get(i);
        if (!w.isModalBlocked()) {
          unblocked.add(w);
        }
      }
      return unblocked;
    }
  }

  private static synchronized Window[] getWindows(AppContext appContext) {
    Window[] realCopy;
    @SuppressWarnings("unchecked") Vector<WeakReference<Window>> windowList
        = (Vector<WeakReference<Window>>) appContext.get(Window.class);
    if (windowList != null) {
      int fullSize = windowList.size();
      int realSize = 0;
      Window[] fullCopy = new Window[fullSize];
      for (int i = 0; i < fullSize; i++) {
        Window w = windowList.get(i).get();
        if (w != null) {
          fullCopy[realSize] = w;
          realSize++;
        }
      }
      realCopy = fullSize != realSize ? Arrays.copyOf(fullCopy, realSize) : fullCopy;
    } else {
      realCopy = new Window[0];
    }
    return realCopy;
  }

  /**
   * Returns an array of all {@code Window}s, both owned and ownerless, created by this application.
   * If called from an applet, the array includes only the {@code Window}s accessible by that
   * applet.
   * <p>
   * <b>Warning:</b> this method may return system created windows, such as a print dialog.
   * Applications should not assume the existence of these dialogs, nor should an application assume
   * anything about these dialogs such as component positions, {@code LayoutManager}s or
   * serialization.
   *
   * @see Frame#getFrames
   * @see Window#getOwnerlessWindows
   * @since 1.6
   */
  public static Window[] getWindows() {
    return getWindows(AppContext.getAppContext());
  }

  /**
   * Returns an array of all {@code Window}s created by this application that have no owner. They
   * include {@code Frame}s and ownerless {@code Dialog}s and {@code Window}s. If called from an
   * applet, the array includes only the {@code Window}s accessible by that applet.
   * <p>
   * <b>Warning:</b> this method may return system created windows, such as a print dialog.
   * Applications should not assume the existence of these dialogs, nor should an application assume
   * anything about these dialogs such as component positions, {@code LayoutManager}s or
   * serialization.
   *
   * @see Frame#getFrames
   * @see Window#getWindows()
   * @since 1.6
   */
  public static Window[] getOwnerlessWindows() {
    Window[] allWindows = getWindows();

    int ownerlessCount = 0;
    for (Window w : allWindows) {
      if (w.getOwner() == null) {
        ownerlessCount++;
      }
    }

    Window[] ownerless = new Window[ownerlessCount];
    int c = 0;
    for (Window w : allWindows) {
      if (w.getOwner() == null) {
        ownerless[c] = w;
        c++;
      }
    }

    return ownerless;
  }

  static synchronized void removeFromWindowList(
      AppContext context, WeakReference<Window> weakThis) {
    @SuppressWarnings("unchecked") Vector<WeakReference<Window>> windowList
        = (Vector<WeakReference<Window>>) context.get(Window.class);
    if (windowList != null) {
      windowList.remove(weakThis);
    }
  }

  private static Object tryInvokeMethod(Object o, String name, Object... args) {
    if (o == null) {
      return null;
    }
    try {
      Class<?> oClass = o.getClass();
      Method method = oClass.getMethod(name);
      return method.invoke(o, (Object[]) args);
    } catch (Exception e) {
      return null;
    }
  }

  private static void setLayersOpaque(Component component, boolean isOpaque) {
    // Shouldn't use instanceof to avoid loading Swing classes
    //    if it's a pure AWT application.
    if (SunToolkit.isInstanceOf(component, "javax.swing.RootPaneContainer")) {
      Object root = tryInvokeMethod(component, "getRootPane");
      Object lp = tryInvokeMethod(component, "getLayeredPane");
      Object c = tryInvokeMethod(root, "getContentPane");
      Object content = null;
      if (SunToolkit.isInstanceOf(c, "javax.swing.JComponent")) {
        content = c;
      }
      tryInvokeMethod(lp, "setOpaque", isOpaque);
      tryInvokeMethod(root, "setOpaque", isOpaque);
      if (content != null) {
        tryInvokeMethod(content, "setOpaque", isOpaque);
        Integer numChildren = (Integer) tryInvokeMethod(content, "getComponentCount");
        // Iterate down one level to see whether we have a JApplet
        // (which is also a RootPaneContainer) which requires processing
        if (numChildren != null && numChildren > 0) {
          Component child = (Component) tryInvokeMethod(content, "getComponent", 0);
          if (SunToolkit.isInstanceOf(child, "javax.swing.RootPaneContainer")) {
            setLayersOpaque(child, isOpaque);
          }
        }
      }
    }
  }

  /**
   * Limit the given double value with the given range.
   */
  private static double limit(double value, double min, double max) {
    value = Math.max(value, min);
    value = Math.min(value, max);
    return value;
  }

  private GraphicsConfiguration initGC(GraphicsConfiguration gc) {

    if (gc == null) {
      gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
          getDefaultScreenDevice().getDefaultConfiguration();
    }
    setGraphicsConfiguration(gc);

    return gc;
  }

  private void init(GraphicsConfiguration gc) {

    syncLWRequests = systemSyncLWRequests;

    weakThis = new WeakReference<>(this);
    addToWindowList();

    setWarningString();
    cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    visible = false;

    gc = initGC(gc);

    if (gc.getDevice().getType() != GraphicsDevice.TYPE_RASTER_SCREEN) {
      throw new IllegalArgumentException("not a screen device");
    }
    setLayout(new BorderLayout());

        /* offset the initial location with the original of the screen */
        /* and any insets                                              */
    Rectangle screenBounds = gc.getBounds();
    Insets screenInsets = getToolkit().getScreenInsets(gc);
    int x = getX() + screenBounds.x + screenInsets.left;
    int y = getY() + screenBounds.y + screenInsets.top;
    if (x != this.x || y != this.y) {
      setLocation(x, y);
            /* reset after setLocation */
      setLocationByPlatform(locationByPlatformProp);
    }

    modalExclusionType = ModalExclusionType.NO_EXCLUDE;
    disposerRecord = new WindowDisposerRecord(appContext, this);

    SunToolkit.checkAndSetPolicy(this);

    sjAndroidContext = SkinJobGlobals.getAndroidApplicationContext();
    sjAndroidWindow = SkinJobUtil.newAndroidWindow(sjAndroidContext);
    sjInputContext = new SkinJobInputContext();
  }

  private void ownedInit(Window owner) {
    parent = owner;
    if (owner != null) {
      owner.addOwnedWindow(weakThis);
      if (owner.isAlwaysOnTop()) {
        try {
          setAlwaysOnTop(true);
        } catch (SecurityException ignore) {
        }
      }
    }

    // WindowDisposerRecord requires a proper value of parent field.
    disposerRecord.updateOwner();
  }

  /**
   * Construct a name for this component.  Called by getName() when the name is null.
   */
  @Override
  String constructComponentName() {
    synchronized (Window.class) {
      String result = base + nameCounter;
      nameCounter++;
      return result;
    }
  }

  // A window has an owner, but it does NOT have a container
  @Override
  final Container getContainer() {
    return null;
  }

  @Override
  void setGraphicsConfiguration(GraphicsConfiguration gc) {
    if (gc == null) {
      gc = GraphicsEnvironment.
          getLocalGraphicsEnvironment().
          getDefaultScreenDevice().
          getDefaultConfiguration();
    }
    synchronized (getTreeLock()) {
      super.setGraphicsConfiguration(gc);
      Log.v(TAG, "+ Window.setGraphicsConfiguration(): new GC is \n+ " +
          getGraphicsConfiguration_NoClientCode() + "\n+ this is " + this);
    }
  }

  /**
   * Returns the toolkit of this frame.
   *
   * @return the toolkit of this window.
   * @see Toolkit
   * @see Toolkit#getDefaultToolkit
   * @see Component#getToolkit
   */
  @Override
  public Toolkit getToolkit() {
    return Toolkit.getDefaultToolkit();
  }

  /**
   * Shows or hides this {@code Window} depending on the value of parameter {@code b}.
   * <p>
   * If the method shows the window then the window is also made focused under the following
   * conditions: <ul> <li> The {@code Window} meets the requirements outlined in the {@link
   * #isFocusableWindow} method. <li> The {@code Window}'s {@code autoRequestFocus} property is of
   * the {@code true} value. <li> Native windowing system allows the {@code Window} to get focused.
   * </ul> There is an exception for the second condition (the value of the {@code autoRequestFocus}
   * property). The property is not taken into account if the window is a modal dialog, which blocks
   * the currently focused window.
   * <p>
   * Developers must never assume that the window is the focused or active window until it receives
   * a WINDOW_GAINED_FOCUS or WINDOW_ACTIVATED event.
   *
   * @param b if {@code true}, makes the {@code Window} visible, otherwise hides the {@code Window}.
   * If the {@code Window} and/or its owner are not yet displayable, both are made displayable.  The
   * {@code Window} will be validated prior to being made visible. If the {@code Window} is already
   * visible, this will bring the {@code Window} to the front.<p> If {@code false}, hides this
   * {@code Window}, its subcomponents, and all of its owned children. The {@code Window} and its
   * subcomponents can be made visible again with a call to {@code #setVisible(true)}.
   * @see Component#isDisplayable
   * @see Component#setVisible
   * @see Window#toFront
   * @see Window#dispose
   * @see Window#setAutoRequestFocus
   * @see Window#isFocusableWindow
   */
  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
  }

  /**
   * Determines whether this component will be displayed on the screen.
   *
   * @return {@code true} if the component and all of its ancestors until a toplevel window are
   * visible, {@code false} otherwise
   */
  @Override
  boolean isRecursivelyVisible() {
    // 5079694 fix: for a toplevel to be displayed, its parent doesn't have to be visible.
    // We're overriding isRecursivelyVisible to implement this policy.
    return visible;
  }

  /**
   * Checks if this Window is showing on screen.
   *
   * @see Component#setVisible
   */
  @Override
  public boolean isShowing() {
    return visible;
  }

  /**
   * Makes the Window visible. If the Window and/or its owner are not yet displayable, both are made
   * displayable.  The Window will be validated prior to being made visible. If the Window is
   * already visible, this will bring the Window to the front.
   *
   * @see Component#isDisplayable
   * @see #toFront
   * @deprecated As of JDK version 1.5, replaced by {@link #setVisible(boolean)}.
   */
  @Override
  @Deprecated
  public void show() {
    if (peer == null) {
      addNotify();
    }
    validateUnconditionally();

    isInShow = true;
    if (visible) {
      toFront();
    } else {
      beforeFirstShow = false;
      closeSplashScreen();
      Dialog.checkShouldBeBlocked(this);
      super.show();
      synchronized (getTreeLock()) {
        locationByPlatform = false;
      }
      for (int i = 0; i < ownedWindowList.size(); i++) {
        Window child = ownedWindowList.elementAt(i).get();
        if (child != null && child.showWithParent) {
          child.show();
          child.showWithParent = false;
        }       // endif
      }   // endfor
      if (!isModalBlocked()) {
        updateChildrenBlocking();
      } else {
        // fix for 6532736: after this window is shown, its blocker
        // should be raised to front
        modalBlocker.toFront_NoClientCode();
      }
      if (this instanceof Frame || this instanceof Dialog) {
        updateChildFocusableWindowState(this);
      }
    }
    isInShow = false;

    // If first time shown, generate WindowOpened event
    if ((state & OPENED) == 0) {
      postWindowEvent(WindowEvent.WINDOW_OPENED);
      state |= OPENED;
    }
  }

  /**
   * Hide this Window, its subcomponents, and all of its owned children. The Window and its
   * subcomponents can be made visible again with a call to {@code show}.
   *
   * @see #show
   * @see #dispose
   * @deprecated As of JDK version 1.5, replaced by {@link #setVisible(boolean)}.
   */
  @Override
  @Deprecated
  public void hide() {
    synchronized (ownedWindowList) {
      for (int i = 0; i < ownedWindowList.size(); i++) {
        Window child = ownedWindowList.elementAt(i).get();
        if (child != null && child.visible) {
          child.hide();
          child.showWithParent = true;
        }
      }
    }
    if (isModalBlocked()) {
      modalBlocker.unblockWindow(this);
    }
    super.hide();
    synchronized (getTreeLock()) {
      locationByPlatform = false;
    }
  }

  /**
   * Gets the background color of this window.
   * <p>
   * Note that the alpha component of the returned color indicates whether the window is in the
   * non-opaque (per-pixel translucent) mode.
   *
   * @return this component's background color
   * @see Window#setBackground(Color)
   * @see Window#isOpaque
   * @see WindowTranslucency
   */
  @Override
  public Color getBackground() {
    return super.getBackground();
  }

  /**
   * Sets the background color of this window.
   * <p>
   * If the windowing system supports the {@link WindowTranslucency#PERPIXEL_TRANSLUCENT
   * PERPIXEL_TRANSLUCENT} translucency, the alpha component of the given background color may
   * effect the mode of operation for this window: it indicates whether this window must be opaque
   * (alpha equals {@code 1.0f}) or per-pixel translucent (alpha is less than {@code 1.0f}). If the
   * given background color is {@code null}, the window is considered completely opaque.
   * <p>
   * All the following conditions must be met to enable the per-pixel transparency mode for this
   * window: <ul> <li>The {@link WindowTranslucency#PERPIXEL_TRANSLUCENT PERPIXEL_TRANSLUCENT}
   * translucency must be supported by the graphics device where this window is located <li>The
   * window must be undecorated (see {@link Frame#setUndecorated} and {@link Dialog#setUndecorated})
   * <li>The window must not be in full-screen mode (see {@link GraphicsDevice#setFullScreenWindow(Window)})
   * </ul>
   * <p>
   * If the alpha component of the requested background color is less than {@code 1.0f}, and any of
   * the above conditions are not met, the background color of this window will not change, the
   * alpha component of the given background color will not affect the mode of operation for this
   * window, and either the {@code UnsupportedOperationException} or {@code
   * IllegalComponentStateException} will be thrown.
   * <p>
   * When the window is per-pixel translucent, the drawing sub-system respects the alpha value of
   * each individual pixel. If a pixel gets painted with the alpha color component equal to zero, it
   * becomes visually transparent. If the alpha of the pixel is equal to 1.0f, the pixel is fully
   * opaque. Interim values of the alpha color component make the pixel semi-transparent. In this
   * mode, the background of the window gets painted with the alpha value of the given background
   * color. If the alpha value of the argument of this method is equal to {@code 0}, the background
   * is not painted at all.
   * <p>
   * The actual level of translucency of a given pixel also depends on window opacity (see {@link
   * #setOpacity(float)}), as well as the current shape of this window (see {@link
   * #setShape(Shape)}).
   * <p>
   * Note that painting a pixel with the alpha value of {@code 0} may or may not disable the mouse
   * event handling on this pixel. This is a platform-dependent behavior. To make sure the mouse
   * events do not get dispatched to a particular pixel, the pixel must be excluded from the shape
   * of the window.
   * <p>
   * Enabling the per-pixel translucency mode may change the graphics configuration of this window
   * due to the native platform requirements.
   *
   * @param bgColor the color to become this window's background color.
   * @throws IllegalComponentStateException if the alpha value of the given background color is less
   * than {@code 1.0f} and the window is decorated
   * @throws IllegalComponentStateException if the alpha value of the given background color is less
   * than {@code 1.0f} and the window is in full-screen mode
   * @throws UnsupportedOperationException if the alpha value of the given background color is less
   * than {@code 1.0f} and {@link WindowTranslucency#PERPIXEL_TRANSLUCENT PERPIXEL_TRANSLUCENT}
   * translucency is not supported
   * @see Window#getBackground
   * @see Window#isOpaque
   * @see Window#setOpacity(float)
   * @see Window#setShape(Shape)
   * @see Frame#isUndecorated
   * @see Dialog#isUndecorated
   * @see WindowTranslucency
   * @see GraphicsDevice#isWindowTranslucencySupported(WindowTranslucency)
   * @see GraphicsConfiguration#isTranslucencyCapable()
   */
  @Override
  public void setBackground(Color bgColor) {
    Color oldBg = getBackground();
    super.setBackground(bgColor);
    if (oldBg != null && oldBg.equals(bgColor)) {
      return;
    }
    int oldAlpha = oldBg != null ? oldBg.getAlpha() : 255;
    int alpha = bgColor != null ? bgColor.getAlpha() : 255;
    if (oldAlpha == 255 && alpha < 255) { // non-opaque window
      GraphicsConfiguration gc = getGraphicsConfiguration();
      GraphicsDevice gd = gc.getDevice();
      if (gc.getDevice().getFullScreenWindow() == this) {
        throw new IllegalComponentStateException(
            "Making full-screen window non opaque is not supported.");
      }
      if (!gc.isTranslucencyCapable()) {
        GraphicsConfiguration capableGC = gd.getTranslucencyCapableGC();
        if (capableGC == null) {
          throw new UnsupportedOperationException(
              "PERPIXEL_TRANSLUCENT translucency is not supported");
        }
        setGraphicsConfiguration(capableGC);
      }
      setLayersOpaque(this, false);
    } else if (oldAlpha < 255 && alpha == 255) {
      setLayersOpaque(this, true);
    }
    WindowPeer peer = (WindowPeer) getPeer();
    if (peer != null) {
      peer.setOpaque(alpha == 255);
    }
  }

  /**
   * @deprecated As of JDK version 1.1 replaced by {@code dispatchEvent(AWTEvent)}.
   */
  @Override
  @Deprecated
  public boolean postEvent(Event e) {
    if (handleEvent(e)) {
      e.consume();
      return true;
    }
    return false;
  }

  /**
   * Gets the {@code Locale} object that is associated with this window, if the locale has been set.
   * If no locale has been set, then the default locale is returned.
   *
   * @return the locale that is set for this window.
   * @see Locale
   * @since JDK1.1
   */
  @Override
  public Locale getLocale() {
    if (locale == null) {
      return Locale.getDefault();
    }
    return locale;
  }

  /**
   * {@inheritDoc}
   * <p>
   * The method changes the geometry-related data. Therefore, the native windowing system may ignore
   * such requests, or it may modify the requested data, so that the {@code Window} object is placed
   * and sized in a way that corresponds closely to the desktop settings.
   */
  @Override
  public void setLocation(Point p) {
    super.setLocation(p);
  }

  /**
   * {@inheritDoc}
   * <p>
   * The method changes the geometry-related data. Therefore, the native windowing system may ignore
   * such requests, or it may modify the requested data, so that the {@code Window} object is placed
   * and sized in a way that corresponds closely to the desktop settings.
   */
  @Override
  public void setLocation(int x, int y) {
    super.setLocation(x, y);
  }

  /**
   * {@inheritDoc}
   * <p>
   * The {@code d.width} and {@code d.height} values are automatically enlarged if either is less
   * than the minimum size as specified by previous call to {@code setMinimumSize}.
   * <p>
   * The method changes the geometry-related data. Therefore, the native windowing system may ignore
   * such requests, or it may modify the requested data, so that the {@code Window} object is placed
   * and sized in a way that corresponds closely to the desktop settings.
   *
   * @see #getSize
   * @see #setBounds
   * @see #setMinimumSize
   * @since 1.6
   */
  @Override
  public void setSize(Dimension d) {
    super.setSize(d);
  }

  /**
   * {@inheritDoc}
   * <p>
   * The {@code width} and {@code height} values are automatically enlarged if either is less than
   * the minimum size as specified by previous call to {@code setMinimumSize}.
   * <p>
   * The method changes the geometry-related data. Therefore, the native windowing system may ignore
   * such requests, or it may modify the requested data, so that the {@code Window} object is placed
   * and sized in a way that corresponds closely to the desktop settings.
   *
   * @see #getSize
   * @see #setBounds
   * @see #setMinimumSize
   * @since 1.6
   */
  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height);
  }

  /**
   * {@inheritDoc}
   * <p>
   * The {@code r.width} or {@code r.height} values will be automatically enlarged if either is less
   * than the minimum size as specified by previous call to {@code setMinimumSize}.
   * <p>
   * The method changes the geometry-related data. Therefore, the native windowing system may ignore
   * such requests, or it may modify the requested data, so that the {@code Window} object is placed
   * and sized in a way that corresponds closely to the desktop settings.
   *
   * @see #getBounds
   * @see #setLocation(int, int)
   * @see #setLocation(Point)
   * @see #setSize(int, int)
   * @see #setSize(Dimension)
   * @see #setMinimumSize
   * @see #setLocationByPlatform
   * @see #isLocationByPlatform
   * @since 1.6
   */
  @Override
  public void setBounds(Rectangle r) {
    setBounds(r.x, r.y, r.width, r.height);
  }

  /**
   * {@inheritDoc}
   * <p>
   * The {@code width} or {@code height} values are automatically enlarged if either is less than
   * the minimum size as specified by previous call to {@code setMinimumSize}.
   * <p>
   * The method changes the geometry-related data. Therefore, the native windowing system may ignore
   * such requests, or it may modify the requested data, so that the {@code Window} object is placed
   * and sized in a way that corresponds closely to the desktop settings.
   *
   * @see #getBounds
   * @see #setLocation(int, int)
   * @see #setLocation(Point)
   * @see #setSize(int, int)
   * @see #setSize(Dimension)
   * @see #setMinimumSize
   * @see #setLocationByPlatform
   * @see #isLocationByPlatform
   * @since 1.6
   */
  @Override
  public void setBounds(int x, int y, int width, int height) {
    synchronized (getTreeLock()) {
      if (getBoundsOp() == ComponentPeer.SET_LOCATION
          || getBoundsOp() == ComponentPeer.SET_BOUNDS) {
        locationByPlatform = false;
      }
      super.setBounds(x, y, width, height);
    }
  }

  /**
   * @deprecated As of JDK version 1.1, replaced by {@code setBounds(int, int, int, int)}.
   */
  @Override
  @Deprecated
  public void reshape(int x, int y, int width, int height) {
    if (isMinimumSizeSet()) {
      Dimension minSize = getMinimumSize();
      if (width < minSize.width) {
        width = minSize.width;
      }
      if (height < minSize.height) {
        height = minSize.height;
      }
    }
    super.reshape(x, y, width, height);
  }

  /**
   * Indicates if the window is currently opaque.
   * <p>
   * The method returns {@code false} if the background color of the window is not {@code null} and
   * the alpha component of the color is less than {@code 1.0f}. The method returns {@code true}
   * otherwise.
   *
   * @return {@code true} if the window is opaque, {@code false} otherwise
   * @see Window#getBackground
   * @see Window#setBackground(Color)
   * @since 1.7
   */
  @Override
  public boolean isOpaque() {
    Color bg = getBackground();
    return bg == null || bg.getAlpha() == 255;
  }

  /**
   * Sets the minimum size of this window to a constant value.  Subsequent calls to {@code
   * getMinimumSize} will always return this value. If current window's size is less than {@code
   * minimumSize} the size of the window is automatically enlarged to honor the minimum size.
   * <p>
   * If the {@code setSize} or {@code setBounds} methods are called afterwards with a width or
   * height less than that was specified by the {@code setMinimumSize} method the window is
   * automatically enlarged to meet the {@code minimumSize} value. The {@code minimumSize} value
   * also affects the behaviour of the {@code pack} method.
   * <p>
   * The default behavior is restored by setting the minimum size parameter to the {@code null}
   * value.
   * <p>
   * Resizing operation may be restricted if the user tries to resize window below the {@code
   * minimumSize} value. This behaviour is platform-dependent.
   *
   * @param minimumSize the new minimum size of this window
   * @see Component#setMinimumSize
   * @see #getMinimumSize
   * @see #isMinimumSizeSet
   * @see #setSize(Dimension)
   * @see #pack
   * @since 1.6
   */
  @Override
  public void setMinimumSize(Dimension minimumSize) {
    synchronized (getTreeLock()) {
      super.setMinimumSize(minimumSize);
      Dimension size = getSize();
      if (isMinimumSizeSet()) {
        if (size.width < minimumSize.width || size.height < minimumSize.height) {
          int nw = Math.max(width, minimumSize.width);
          int nh = Math.max(height, minimumSize.height);
          setSize(nw, nh);
        }
      }
      if (peer != null) {
        ((WindowPeer) peer).updateMinimumSize();
      }
    }
  }

  /**
   * Set the cursor image to a specified cursor.
   * <p>
   * The method may have no visual effect if the Java platform implementation and/or the native
   * system do not support changing the mouse cursor shape.
   *
   * @param cursor One of the constants defined by the {@code Cursor} class. If this parameter is
   * null then the cursor for this window will be set to the type Cursor.DEFAULT_CURSOR.
   * @see Component#getCursor
   * @see Cursor
   * @since JDK1.1
   */
  @Override
  public void setCursor(Cursor cursor) {
    if (cursor == null) {
      cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }
    super.setCursor(cursor);
  }

  /**
   * Creates a new strategy for multi-buffering on this component. Multi-buffering is useful for
   * rendering performance.  This method attempts to create the best strategy available with the
   * number of buffers supplied.  It will always create a {@code BufferStrategy} with that number of
   * buffers. A page-flipping strategy is attempted first, then a blitting strategy using
   * accelerated buffers.  Finally, an unaccelerated blitting strategy is used.
   * <p>
   * Each time this method is called, the existing buffer strategy for this component is discarded.
   *
   * @param numBuffers number of buffers to create
   * @throws IllegalArgumentException if numBuffers is less than 1.
   * @throws IllegalStateException if the component is not displayable
   * @see #isDisplayable
   * @see #getBufferStrategy
   * @since 1.4
   */
  @Override
  public void createBufferStrategy(int numBuffers) {
    super.createBufferStrategy(numBuffers);
  }

  /**
   * Creates a new strategy for multi-buffering on this component with the required buffer
   * capabilities.  This is useful, for example, if only accelerated memory or page flipping is
   * desired (as specified by the buffer capabilities).
   * <p>
   * Each time this method is called, the existing buffer strategy for this component is discarded.
   *
   * @param numBuffers number of buffers to create, including the front buffer
   * @param caps the required capabilities for creating the buffer strategy; cannot be {@code null}
   * @throws AWTException if the capabilities supplied could not be supported or met; this may
   * happen, for example, if there is not enough accelerated memory currently available, or if page
   * flipping is specified but not possible.
   * @throws IllegalArgumentException if numBuffers is less than 1, or if caps is {@code null}
   * @see #getBufferStrategy
   * @since 1.4
   */
  @Override
  public void createBufferStrategy(int numBuffers, BufferCapabilities caps) throws AWTException {
    super.createBufferStrategy(numBuffers, caps);
  }

  /**
   * Returns the {@code BufferStrategy} used by this component.  This method will return null if a
   * {@code BufferStrategy} has not yet been created or has been disposed.
   *
   * @return the buffer strategy used by this component
   * @see #createBufferStrategy
   * @since 1.4
   */
  @Override
  public BufferStrategy getBufferStrategy() {
    return super.getBufferStrategy();
  }

  /**
   * Overridden from Component.  Top-level Windows don't dispatch to ancestors
   */
  @Override
  boolean dispatchMouseWheelToAncestor(MouseWheelEvent e) {
    return false;
  }

  /*
     * Should only be called while holding the tree lock.
     * It's overridden here because parent == owner in Window,
     * and we shouldn't adjust counter on owner
     */
  @Override
  void adjustListeningChildrenOnParent(long mask, int num) {
  }

  /**
   * Gets the input context for this window. A window always has an input context, which is shared
   * by subcomponents unless they create and set their own.
   *
   * @see Component#getInputContext
   * @since 1.2
   */
  @Override
  public InputContext getInputContext() {
    synchronized (inputContextLock) {
      if (inputContext == null) {
        inputContext = InputContext.getInstance();
      }
    }
    return inputContext;
  }

  /**
   * Always returns {@code null} because Windows have no ancestors; they represent the top of the
   * Component hierarchy.
   *
   * @return {@code null}
   * @see Container#isFocusCycleRoot()
   * @since 1.4
   */
  @Override
  public final Container getFocusCycleRootAncestor() {
    return null;
  }

  @Override
  final Point getLocationOnWindow() {
    return new Point(0, 0);
  }

  @Override
  final void applyCurrentShape() {
    // The shape calculated by mixing code is not intended to be applied
    // to windows or frames
  }

  // a window doesn't need to be updated in the Z-order.
  @Override
  void updateZOrder() {
  }

  /**
   * Returns the sequence of images to be displayed as the icon for this window.
   * <p>
   * This method returns a copy of the internally stored list, so all operations on the returned
   * object will not affect the window's behavior.
   *
   * @return the copy of icon images' list for this window, or empty list if this window doesn't
   * have icon images.
   * @see #setIconImages
   * @see #setIconImage(Image)
   * @since 1.6
   */
  public java.util.List<Image> getIconImages() {
    java.util.List<Image> icons = this.icons;
    if (icons == null || icons.isEmpty()) {
      return new ArrayList<>();
    }
    return new ArrayList<>(icons);
  }

  /**
   * Sets the sequence of images to be displayed as the icon for this window. Subsequent calls to
   * {@code getIconImages} will always return a copy of the {@code icons} list.
   * <p>
   * Depending on the platform capabilities one or several images of different dimensions will be
   * used as the window's icon.
   * <p>
   * The {@code icons} list is scanned for the images of most appropriate dimensions from the
   * beginning. If the list contains several images of the same size, the first will be used.
   * <p>
   * Ownerless windows with no icon specified use platfrom-default icon. The icon of an owned window
   * may be inherited from the owner unless explicitly overridden. Setting the icon to {@code null}
   * or empty list restores the default behavior.
   * <p>
   * Note : Native windowing systems may use different images of differing dimensions to represent a
   * window, depending on the context (e.g. window decoration, window list, taskbar, etc.). They
   * could also use just a single image for all contexts or no image at all.
   *
   * @param icons the list of icon images to be displayed.
   * @see #getIconImages()
   * @see #setIconImage(Image)
   * @since 1.6
   */
  public synchronized void setIconImages(java.util.List<? extends Image> icons) {
    this.icons = icons == null ? new ArrayList<Image>() : new ArrayList<Image>(icons);
    WindowPeer peer = (WindowPeer) this.peer;
    if (peer != null) {
      peer.updateIconImages();
    }
    // Always send a property change event
    firePropertyChange("iconImage", null, null);
  }

  /**
   * Sets the image to be displayed as the icon for this window.
   * <p>
   * This method can be used instead of {@link #setIconImages setIconImages()} to specify a single
   * image as a window's icon.
   * <p>
   * The following statement:
   * <pre>
   *     setIconImage(image);
   * </pre>
   * is equivalent to:
   * <pre>
   *     ArrayList&lt;Image&gt; imageList = new ArrayList&lt;Image&gt;();
   *     imageList.add(image);
   *     setIconImages(imageList);
   * </pre>
   * <p>
   * Note : Native windowing systems may use different images of differing dimensions to represent a
   * window, depending on the context (e.g. window decoration, window list, taskbar, etc.). They
   * could also use just a single image for all contexts or no image at all.
   *
   * @param image the icon image to be displayed.
   * @see #setIconImages
   * @see #getIconImages()
   * @since 1.6
   */
  public void setIconImage(Image image) {
    ArrayList<Image> imageList = new ArrayList<>();
    if (image != null) {
      imageList.add(image);
    }
    setIconImages(imageList);
  }

  /**
   * Causes this Window to be sized to fit the preferred size and layouts of its subcomponents. The
   * resulting width and height of the window are automatically enlarged if either of dimensions is
   * less than the minimum size as specified by the previous call to the {@code setMinimumSize}
   * method.
   * <p>
   * If the window and/or its owner are not displayable yet, both of them are made displayable
   * before calculating the preferred size. The Window is validated after its size is being
   * calculated.
   *
   * @see Component#isDisplayable
   * @see #setMinimumSize
   */
  public void pack() {
    Container parent = this.parent;
    if (parent != null && parent.getPeer() == null) {
      parent.addNotify();
    }
    if (peer == null) {
      addNotify();
    }
    Dimension newSize = getPreferredSize();
    if (peer != null) {
      setClientSize(newSize.width, newSize.height);
    }

    if (beforeFirstShow) {
      isPacked = true;
    }

    validateUnconditionally();
  }

  void setClientSize(int w, int h) {
    synchronized (getTreeLock()) {
      setBoundsOp(ComponentPeer.SET_CLIENT_SIZE);
      setBounds(x, y, w, h);
    }
  }

  final void closeSplashScreen() {
    if (isTrayIconWindow) {
      return;
    }
    if (beforeFirstWindowShown.getAndSet(false)) {
      // We don't use SplashScreen.getSplashScreen() to avoid instantiating
      // the object if it hasn't been requested by user code explicitly
      SunToolkit.closeSplashScreen();
      SplashScreen.markClosed();
    }
  }

  synchronized void postWindowEvent(int id) {
    if (windowListener != null || (eventMask & AWTEvent.WINDOW_EVENT_MASK) != 0
        || Toolkit.enabledOnToolkit(AWTEvent.WINDOW_EVENT_MASK)) {
      WindowEvent e = new WindowEvent(this, id);
      Toolkit.getEventQueue().postEvent(e);
    }
  }

  /**
   * Releases all of the native screen resources used by this {@code Window}, its subcomponents, and
   * all of its owned children. That is, the resources for these {@code Component}s will be
   * destroyed, any memory they consume will be returned to the OS, and they will be marked as
   * undisplayable.
   * <p>
   * The {@code Window} and its subcomponents can be made displayable again by rebuilding the native
   * resources with a subsequent call to {@code pack} or {@code show}. The states of the recreated
   * {@code Window} and its subcomponents will be identical to the states of these objects at the
   * point where the {@code Window} was disposed (not accounting for additional modifications
   * between those actions).
   * <p>
   * <b>Note</b>: When the last displayable window within the Java virtual machine (VM) is disposed
   * of, the VM may terminate.  See <a href="doc-files/AWTThreadIssues.html#Autoshutdown"> AWT
   * Threading Issues</a> for more information.
   *
   * @see Component#isDisplayable
   * @see #pack
   * @see #show
   */
  public void dispose() {
    doDispose();
    disposerRecord.dispose();
  }

  /*
     * Fix for 4872170.
     * If dispose() is called on parent then its children have to be disposed as well
     * as reported in javadoc. So we need to implement this functionality even if a
     * child overrides dispose() in a wrong way without calling super.dispose().
     */
  void disposeImpl() {
    dispose();
    if (getPeer() != null) {
      doDispose();
    }
  }

  void doDispose() {
    class DisposeAction implements Runnable {
      @Override
      public void run() {
        disposing = true;
        try {
          // Check if this window is the fullscreen window for the
          // device. Exit the fullscreen mode prior to disposing
          // of the window if that's the case.
          GraphicsDevice gd = getGraphicsConfiguration().getDevice();
          if (gd.getFullScreenWindow() == Window.this) {
            gd.setFullScreenWindow(null);
          }

          Object[] ownedWindowArray;
          synchronized (ownedWindowList) {
            ownedWindowArray = new Object[ownedWindowList.size()];
            ownedWindowList.copyInto(ownedWindowArray);
          }
          for (Object anOwnedWindowArray : ownedWindowArray) {
            Window child = (Window) ((Reference) anOwnedWindowArray).get();
            if (child != null) {
              child.disposeImpl();
            }
          }
          hide();
          beforeFirstShow = true;
          removeNotify();
          synchronized (inputContextLock) {
            if (inputContext != null) {
              inputContext.dispose();
              inputContext = null;
            }
          }
          clearCurrentFocusCycleRootOnHide();
        } finally {
          disposing = false;
        }
      }
    }
    boolean fireWindowClosedEvent = isDisplayable();
    DisposeAction action = new DisposeAction();
    if (EventQueue.isDispatchThread()) {
      action.run();
    } else {
      try {
        EventQueue.invokeAndWait(this, action);
      } catch (InterruptedException e) {
        System.err.println("Disposal was interrupted:");
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        System.err.println("Exception during disposal:");
        e.printStackTrace();
      }
    }
    // Execute outside the Runnable because postWindowEvent is
    // synchronized on (this). We don't need to synchronize the call
    // on the EventQueue anyways.
    if (fireWindowClosedEvent) {
      postWindowEvent(WindowEvent.WINDOW_CLOSED);
    }
  }

  /**
   * If this Window is visible, brings this Window to the front and may make it the focused Window.
   * <p>
   * Places this Window at the top of the stacking order and shows it in front of any other Windows
   * in this VM. No action will take place if this Window is not visible. Some platforms do not
   * allow Windows which own other Windows to appear on top of those owned Windows. Some platforms
   * may not permit this VM to place its Windows above windows of native applications, or Windows of
   * other VMs. This permission may depend on whether a Window in this VM is already focused. Every
   * attempt will be made to move this Window as high as possible in the stacking order; however,
   * developers should not assume that this method will move this Window above all other windows in
   * every situation.
   * <p>
   * Developers must never assume that this Window is the focused or active Window until this Window
   * receives a WINDOW_GAINED_FOCUS or WINDOW_ACTIVATED event. On platforms where the top-most
   * window is the focused window, this method will <b>probably</b> focus this Window (if it is not
   * already focused) under the following conditions: <ul> <li> The window meets the requirements
   * outlined in the {@link #isFocusableWindow} method. <li> The window's property {@code
   * autoRequestFocus} is of the {@code true} value. <li> Native windowing system allows the window
   * to get focused. </ul> On platforms where the stacking order does not typically affect the
   * focused window, this method will <b>probably</b> leave the focused and active Windows
   * unchanged.
   * <p>
   * If this method causes this Window to be focused, and this Window is a Frame or a Dialog, it
   * will also become activated. If this Window is focused, but it is not a Frame or a Dialog, then
   * the first Frame or Dialog that is an owner of this Window will be activated.
   * <p>
   * If this window is blocked by modal dialog, then the blocking dialog is brought to the front and
   * remains above the blocked window.
   *
   * @see #toBack
   * @see #setAutoRequestFocus
   * @see #isFocusableWindow
   */
  public void toFront() {
    toFront_NoClientCode();
  }

  // This functionality is implemented in a final package-private method
  // to insure that it cannot be overridden by client subclasses.
  final void toFront_NoClientCode() {
    if (visible) {
      WindowPeer peer = (WindowPeer) this.peer;
      if (peer != null) {
        peer.toFront();
      }
      if (isModalBlocked()) {
        modalBlocker.toFront_NoClientCode();
      }
    }
  }

  /**
   * If this Window is visible, sends this Window to the back and may cause it to lose focus or
   * activation if it is the focused or active Window.
   * <p>
   * Places this Window at the bottom of the stacking order and shows it behind any other Windows in
   * this VM. No action will take place is this Window is not visible. Some platforms do not allow
   * Windows which are owned by other Windows to appear below their owners. Every attempt will be
   * made to move this Window as low as possible in the stacking order; however, developers should
   * not assume that this method will move this Window below all other windows in every situation.
   * <p>
   * Because of variations in native windowing systems, no guarantees about changes to the focused
   * and active Windows can be made. Developers must never assume that this Window is no longer the
   * focused or active Window until this Window receives a WINDOW_LOST_FOCUS or WINDOW_DEACTIVATED
   * event. On platforms where the top-most window is the focused window, this method will
   * <b>probably</b> cause this Window to lose focus. In that case, the next highest, focusable
   * Window in this VM will receive focus. On platforms where the stacking order does not typically
   * affect the focused window, this method will <b>probably</b> leave the focused and active
   * Windows unchanged.
   *
   * @see #toFront
   */
  public void toBack() {
    toBack_NoClientCode();
  }

  // This functionality is implemented in a final package-private method
  // to insure that it cannot be overridden by client subclasses.
  final void toBack_NoClientCode() {
    if (isAlwaysOnTop()) {
      try {
        setAlwaysOnTop(false);
      } catch (SecurityException e) {
      }
    }
    if (visible) {
      WindowPeer peer = (WindowPeer) this.peer;
      if (peer != null) {
        peer.toBack();
      }
    }
  }

  /**
   * Gets the warning string that is displayed with this window. If this window is insecure, the
   * warning string is displayed somewhere in the visible area of the window. A window is insecure
   * if there is a security manager and the security manager denies {@code
   * AWTPermission("showWindowWithoutWarningBanner")}.
   * <p>
   * If the window is secure, then {@code getWarningString} returns {@code null}. If the window is
   * insecure, this method checks for the system property {@code awt.appletWarning} and returns the
   * string value of that property.
   *
   * @return the warning string for this window.
   */
  public final String getWarningString() {
    return warningString;
  }

  private void setWarningString() {
    warningString = System.getProperty("awt.appletWarning", "Java Applet Window");
  }

  /**
   * Returns the owner of this window.
   *
   * @since 1.2
   */
  public Window getOwner() {
    return getOwner_NoClientCode();
  }

  final Window getOwner_NoClientCode() {
    return (Window) parent;
  }

  /**
   * Return an array containing all the windows this window currently owns.
   *
   * @since 1.2
   */
  public Window[] getOwnedWindows() {
    return getOwnedWindows_NoClientCode();
  }

  final Window[] getOwnedWindows_NoClientCode() {
    Window[] realCopy;

    synchronized (ownedWindowList) {
      // Recall that ownedWindowList is actually a Vector of
      // WeakReferences and calling get() on one of these references
      // may return null. Make two arrays-- one the size of the
      // Vector (fullCopy with size fullSize), and one the size of
      // all non-null get()s (realCopy with size realSize).
      int fullSize = ownedWindowList.size();
      int realSize = 0;
      Window[] fullCopy = new Window[fullSize];

      for (int i = 0; i < fullSize; i++) {
        fullCopy[realSize] = ownedWindowList.elementAt(i).get();

        if (fullCopy[realSize] != null) {
          realSize++;
        }
      }

      realCopy = fullSize != realSize ? Arrays.copyOf(fullCopy, realSize) : fullCopy;
    }

    return realCopy;
  }

  boolean isModalBlocked() {
    return modalBlocker != null;
  }

  void setModalBlocked(Dialog blocker, boolean blocked, boolean peerCall) {
    modalBlocker = blocked ? blocker : null;
    if (peerCall) {
      WindowPeer peer = (WindowPeer) this.peer;
      if (peer != null) {
        peer.setModalBlocked(blocker, blocked);
      }
    }
  }

  Dialog getModalBlocker() {
    return modalBlocker;
  }

  Window getDocumentRoot() {
    synchronized (getTreeLock()) {
      Window w = this;
      while (w.getOwner() != null) {
        w = w.getOwner();
      }
      return w;
    }
  }

  /**
   * Returns the modal exclusion type of this window.
   *
   * @return the modal exclusion type of this window
   * @see ModalExclusionType
   * @see Window#setModalExclusionType
   * @since 1.6
   */
  public ModalExclusionType getModalExclusionType() {
    return modalExclusionType;
  }

  /**
   * Specifies the modal exclusion type for this window. If a window is modal excluded, it is not
   * blocked by some modal dialogs. See {@link ModalExclusionType Dialog.ModalExclusionType} for
   * possible modal exclusion types.
   * <p>
   * If the given type is not supported, {@code NO_EXCLUDE} is used.
   * <p>
   * Note: changing the modal exclusion type for a visible window may have no effect until it is
   * hidden and then shown again.
   *
   * @param exclusionType the modal exclusion type for this window; a {@code null} value is
   * equivalent to {@link ModalExclusionType#NO_EXCLUDE NO_EXCLUDE}
   * @throws SecurityException if the calling thread does not have permission to set the modal
   * exclusion property to the window with the given {@code exclusionType}
   * @see ModalExclusionType
   * @see Window#getModalExclusionType
   * @see Toolkit#isModalExclusionTypeSupported
   * @since 1.6
   */
  public void setModalExclusionType(ModalExclusionType exclusionType) {
    if (exclusionType == null) {
      exclusionType = ModalExclusionType.NO_EXCLUDE;
    }
    if (!Toolkit.getDefaultToolkit().isModalExclusionTypeSupported(exclusionType)) {
      exclusionType = ModalExclusionType.NO_EXCLUDE;
    }
    if (modalExclusionType == exclusionType) {
      return;
    }
    modalExclusionType = exclusionType;

    // if we want on-fly changes, we need to uncomment the lines below
    //   and override the method in Dialog to use modalShow() instead
    //   of updateChildrenBlocking()
 /*
        if (isModalBlocked()) {
            modalBlocker.unblockWindow(this);
        }
        Dialog.checkShouldBeBlocked(this);
        updateChildrenBlocking();
 */
  }

  boolean isModalExcluded(ModalExclusionType exclusionType) {
    if (modalExclusionType != null && modalExclusionType.compareTo(exclusionType) >= 0) {
      return true;
    }
    Window owner = getOwner_NoClientCode();
    return owner != null && owner.isModalExcluded(exclusionType);
  }

  void updateChildrenBlocking() {
    Vector<Window> childHierarchy = new Vector<>();
    Window[] ownedWindows = getOwnedWindows();
    Collections.addAll(childHierarchy, ownedWindows);
    int k = 0;
    while (k < childHierarchy.size()) {
      Window w = childHierarchy.get(k);
      if (w.isVisible()) {
        if (w.isModalBlocked()) {
          Dialog blocker = w.getModalBlocker();
          blocker.unblockWindow(w);
        }
        Dialog.checkShouldBeBlocked(w);
        Window[] wOwned = w.getOwnedWindows();
        Collections.addAll(childHierarchy, wOwned);
      }
      k++;
    }
  }

  /**
   * Adds the specified window listener to receive window events from this window. If l is null, no
   * exception is thrown and no action is performed. <p>Refer to <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
   * >AWT Threading Issues</a> for details on AWT's threading model.
   *
   * @param l the window listener
   * @see #removeWindowListener
   * @see #getWindowListeners
   */
  public synchronized void addWindowListener(WindowListener l) {
    if (l == null) {
      return;
    }
    newEventsOnly = true;
    windowListener = AWTEventMulticaster.add(windowListener, l);
  }

  /**
   * Adds the specified window state listener to receive window events from this window.  If {@code
   * l} is {@code null}, no exception is thrown and no action is performed. <p>Refer to <a
   * href="doc-files/AWTThreadIssues.html#ListenersThreads" >AWT Threading Issues</a> for details on
   * AWT's threading model.
   *
   * @param l the window state listener
   * @see #removeWindowStateListener
   * @see #getWindowStateListeners
   * @since 1.4
   */
  public synchronized void addWindowStateListener(WindowStateListener l) {
    if (l == null) {
      return;
    }
    windowStateListener = AWTEventMulticaster.add(windowStateListener, l);
    newEventsOnly = true;
  }

  /**
   * Adds the specified window focus listener to receive window events from this window. If l is
   * null, no exception is thrown and no action is performed. <p>Refer to <a
   * href="doc-files/AWTThreadIssues.html#ListenersThreads" >AWT Threading Issues</a> for details on
   * AWT's threading model.
   *
   * @param l the window focus listener
   * @see #removeWindowFocusListener
   * @see #getWindowFocusListeners
   * @since 1.4
   */
  public synchronized void addWindowFocusListener(WindowFocusListener l) {
    if (l == null) {
      return;
    }
    windowFocusListener = AWTEventMulticaster.add(windowFocusListener, l);
    newEventsOnly = true;
  }

  /**
   * Removes the specified window listener so that it no longer receives window events from this
   * window. If l is null, no exception is thrown and no action is performed. <p>Refer to <a
   * href="doc-files/AWTThreadIssues.html#ListenersThreads" >AWT Threading Issues</a> for details on
   * AWT's threading model.
   *
   * @param l the window listener
   * @see #addWindowListener
   * @see #getWindowListeners
   */
  public synchronized void removeWindowListener(WindowListener l) {
    if (l == null) {
      return;
    }
    windowListener = AWTEventMulticaster.remove(windowListener, l);
  }

  /**
   * Removes the specified window state listener so that it no longer receives window events from
   * this window.  If {@code l} is {@code null}, no exception is thrown and no action is performed.
   * <p>Refer to <a href="doc-files/AWTThreadIssues.html#ListenersThreads" >AWT Threading Issues</a>
   * for details on AWT's threading model.
   *
   * @param l the window state listener
   * @see #addWindowStateListener
   * @see #getWindowStateListeners
   * @since 1.4
   */
  public synchronized void removeWindowStateListener(WindowStateListener l) {
    if (l == null) {
      return;
    }
    windowStateListener = AWTEventMulticaster.remove(windowStateListener, l);
  }

  /**
   * Removes the specified window focus listener so that it no longer receives window events from
   * this window. If l is null, no exception is thrown and no action is performed. <p>Refer to <a
   * href="doc-files/AWTThreadIssues.html#ListenersThreads" >AWT Threading Issues</a> for details on
   * AWT's threading model.
   *
   * @param l the window focus listener
   * @see #addWindowFocusListener
   * @see #getWindowFocusListeners
   * @since 1.4
   */
  public synchronized void removeWindowFocusListener(WindowFocusListener l) {
    if (l == null) {
      return;
    }
    windowFocusListener = AWTEventMulticaster.remove(windowFocusListener, l);
  }

  /**
   * Returns an array of all the window listeners registered on this window.
   *
   * @return all of this window's {@code WindowListener}s or an empty array if no window listeners
   * are currently registered
   * @see #addWindowListener
   * @see #removeWindowListener
   * @since 1.4
   */
  public synchronized WindowListener[] getWindowListeners() {
    return getListeners(WindowListener.class);
  }

  /**
   * Returns an array of all the window focus listeners registered on this window.
   *
   * @return all of this window's {@code WindowFocusListener}s or an empty array if no window focus
   * listeners are currently registered
   * @see #addWindowFocusListener
   * @see #removeWindowFocusListener
   * @since 1.4
   */
  public synchronized WindowFocusListener[] getWindowFocusListeners() {
    return getListeners(WindowFocusListener.class);
  }

  /**
   * Returns an array of all the window state listeners registered on this window.
   *
   * @return all of this window's {@code WindowStateListener}s or an empty array if no window state
   * listeners are currently registered
   * @see #addWindowStateListener
   * @see #removeWindowStateListener
   * @since 1.4
   */
  public synchronized WindowStateListener[] getWindowStateListeners() {
    return getListeners(WindowStateListener.class);
  }

  /**
   * Processes window events occurring on this window by dispatching them to any registered
   * WindowListener objects. NOTE: This method will not be called unless window events are enabled
   * for this component; this happens when one of the following occurs: <ul> <li>A WindowListener
   * object is registered via {@code addWindowListener} <li>Window events are enabled via {@code
   * enableEvents} </ul> <p>Note that if the event parameter is {@code null} the behavior is
   * unspecified and may result in an exception.
   *
   * @param e the window event
   * @see Component#enableEvents
   */
  protected void processWindowEvent(WindowEvent e) {
    WindowListener listener = windowListener;
    if (listener != null) {
      switch (e.getID()) {
        case WindowEvent.WINDOW_OPENED:
          listener.windowOpened(e);
          break;
        case WindowEvent.WINDOW_CLOSING:
          listener.windowClosing(e);
          break;
        case WindowEvent.WINDOW_CLOSED:
          listener.windowClosed(e);
          break;
        case WindowEvent.WINDOW_ICONIFIED:
          listener.windowIconified(e);
          break;
        case WindowEvent.WINDOW_DEICONIFIED:
          listener.windowDeiconified(e);
          break;
        case WindowEvent.WINDOW_ACTIVATED:
          listener.windowActivated(e);
          break;
        case WindowEvent.WINDOW_DEACTIVATED:
          listener.windowDeactivated(e);
          break;
        default:
          break;
      }
    }
  }

  /**
   * Processes window focus event occurring on this window by dispatching them to any registered
   * WindowFocusListener objects. NOTE: this method will not be called unless window focus events
   * are enabled for this window. This happens when one of the following occurs: <ul> <li>a
   * WindowFocusListener is registered via {@code addWindowFocusListener} <li>Window focus events
   * are enabled via {@code enableEvents} </ul> <p>Note that if the event parameter is {@code null}
   * the behavior is unspecified and may result in an exception.
   *
   * @param e the window focus event
   * @see Component#enableEvents
   * @since 1.4
   */
  protected void processWindowFocusEvent(WindowEvent e) {
    WindowFocusListener listener = windowFocusListener;
    if (listener != null) {
      switch (e.getID()) {
        case WindowEvent.WINDOW_GAINED_FOCUS:
          listener.windowGainedFocus(e);
          break;
        case WindowEvent.WINDOW_LOST_FOCUS:
          listener.windowLostFocus(e);
          break;
        default:
          break;
      }
    }
  }

  /**
   * Processes window state event occurring on this window by dispatching them to any registered
   * {@code WindowStateListener} objects. NOTE: this method will not be called unless window state
   * events are enabled for this window.  This happens when one of the following occurs: <ul> <li>a
   * {@code WindowStateListener} is registered via {@code addWindowStateListener} <li>window state
   * events are enabled via {@code enableEvents} </ul> <p>Note that if the event parameter is {@code
   * null} the behavior is unspecified and may result in an exception.
   *
   * @param e the window state event
   * @see Component#enableEvents
   * @since 1.4
   */
  protected void processWindowStateEvent(WindowEvent e) {
    WindowStateListener listener = windowStateListener;
    if (listener != null) {
      switch (e.getID()) {
        case WindowEvent.WINDOW_STATE_CHANGED:
          listener.windowStateChanged(e);
          break;
        default:
          break;
      }
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void setOwnedWindowsAlwaysOnTop(boolean alwaysOnTop) {
    WeakReference<Window>[] ownedWindowArray;
    synchronized (ownedWindowList) {
      ownedWindowArray = new WeakReference[ownedWindowList.size()];
      ownedWindowList.copyInto(ownedWindowArray);
    }

    for (WeakReference<Window> ref : ownedWindowArray) {
      Window window = ref.get();
      if (window != null) {
        try {
          window.setAlwaysOnTop(alwaysOnTop);
        } catch (SecurityException ignore) {
        }
      }
    }
  }

  /**
   * Returns whether the always-on-top mode is supported for this window. Some platforms may not
   * support always-on-top windows, some may support only some kinds of top-level windows; for
   * example, a platform may not support always-on-top modal dialogs.
   *
   * @return {@code true}, if the always-on-top mode is supported for this window and this window's
   * toolkit supports always-on-top windows, {@code false} otherwise
   * @see #setAlwaysOnTop(boolean)
   * @see #getToolkit
   * @see Toolkit#isAlwaysOnTopSupported
   * @since 1.6
   */
  public boolean isAlwaysOnTopSupported() {
    return Toolkit.getDefaultToolkit().isAlwaysOnTopSupported();
  }

  /**
   * Returns whether this window is an always-on-top window.
   *
   * @return {@code true}, if the window is in always-on-top state, {@code false} otherwise
   * @see #setAlwaysOnTop
   * @since 1.5
   */
  public final boolean isAlwaysOnTop() {
    return alwaysOnTop;
  }

  /**
   * Sets whether this window should always be above other windows.  If there are multiple
   * always-on-top windows, their relative order is unspecified and platform dependent.
   * <p>
   * If some other window is already always-on-top then the relative order between these windows is
   * unspecified (depends on platform).  No window can be brought to be over the always-on-top
   * window except maybe another always-on-top window.
   * <p>
   * All windows owned by an always-on-top window inherit this state and automatically become
   * always-on-top.  If a window ceases to be always-on-top, the windows that it owns will no longer
   * be always-on-top.  When an always-on-top window is sent {@link #toBack toBack}, its
   * always-on-top state is set to {@code false}.
   * <p>
   * <p> When this method is called on a window with a value of {@code true}, and the window is
   * visible and the platform supports always-on-top for this window, the window is immediately
   * brought forward, "sticking" it in the top-most position. If the window isn`t currently visible,
   * this method sets the always-on-top state to {@code true} but does not bring the window forward.
   * When the window is later shown, it will be always-on-top.
   * <p>
   * <p> When this method is called on a window with a value of {@code false} the always-on-top
   * state is set to normal. It may also cause an unspecified, platform-dependent change in the
   * z-order of top-level windows, but other always-on-top windows will remain in top-most position.
   * Calling this method with a value of {@code false} on a window that has a normal state has no
   * effect.
   * <p>
   * <p><b>Note</b>: some platforms might not support always-on-top windows.  To detect if
   * always-on-top windows are supported by the current platform, use {@link
   * Toolkit#isAlwaysOnTopSupported()} and {@link Window#isAlwaysOnTopSupported()}.  If
   * always-on-top mode isn't supported for this window or this window's toolkit does not support
   * always-on-top windows, calling this method has no effect.
   * <p>
   * If a SecurityManager is installed, the calling thread must be granted the AWTPermission
   * "setWindowAlwaysOnTop" in order to set the value of this property. If this permission is not
   * granted, this method will throw a SecurityException, and the current value of the property will
   * be left unchanged.
   *
   * @param alwaysOnTop true if the window should always be above other windows
   * @throws SecurityException if the calling thread does not have permission to set the value of
   * always-on-top property
   * @see #isAlwaysOnTop
   * @see #toFront
   * @see #toBack
   * @see AWTPermission
   * @see #isAlwaysOnTopSupported
   * @see #getToolkit
   * @see Toolkit#isAlwaysOnTopSupported
   * @since 1.5
   */
  public final void setAlwaysOnTop(boolean alwaysOnTop) throws SecurityException {
    boolean oldAlwaysOnTop;
    synchronized (this) {
      oldAlwaysOnTop = this.alwaysOnTop;
      this.alwaysOnTop = alwaysOnTop;
    }
    if (oldAlwaysOnTop != alwaysOnTop) {
      if (isAlwaysOnTopSupported()) {
        WindowPeer peer = (WindowPeer) this.peer;
        synchronized (getTreeLock()) {
          if (peer != null) {
            peer.updateAlwaysOnTopState();
          }
        }
      }
      firePropertyChange("alwaysOnTop", oldAlwaysOnTop, alwaysOnTop);
    }
    setOwnedWindowsAlwaysOnTop(alwaysOnTop);
  }

  /**
   * Returns the child Component of this Window that has focus if this Window is focused; returns
   * null otherwise.
   *
   * @return the child Component with focus, or null if this Window is not focused
   * @see #getMostRecentFocusOwner
   * @see #isFocused
   */
  public Component getFocusOwner() {
    return isFocused() ? KeyboardFocusManager.getCurrentKeyboardFocusManager().
        getFocusOwner() : null;
  }

  /**
   * Returns the child Component of this Window that will receive the focus when this Window is
   * focused. If this Window is currently focused, this method returns the same Component as {@code
   * getFocusOwner()}. If this Window is not focused, then the child Component that most recently
   * requested focus will be returned. If no child Component has ever requested focus, and this is a
   * focusable Window, then this Window's initial focusable Component is returned. If no child
   * Component has ever requested focus, and this is a non-focusable Window, null is returned.
   *
   * @return the child Component that will receive focus when this Window is focused
   * @see #getFocusOwner
   * @see #isFocused
   * @see #isFocusableWindow
   * @since 1.4
   */
  public Component getMostRecentFocusOwner() {
    if (isFocused()) {
      return getFocusOwner();
    } else {
      Component mostRecent = KeyboardFocusManager.getMostRecentFocusOwner(this);
      if (mostRecent != null) {
        return mostRecent;
      } else {
        return isFocusableWindow() ? getFocusTraversalPolicy().getInitialComponent(this) : null;
      }
    }
  }

  /**
   * Returns whether this Window is active. Only a Frame or a Dialog may be active. The native
   * windowing system may denote the active Window or its children with special decorations, such as
   * a highlighted title bar. The active Window is always either the focused Window, or the first
   * Frame or Dialog that is an owner of the focused Window.
   *
   * @return whether this is the active Window.
   * @see #isFocused
   * @since 1.4
   */
  public boolean isActive() {
    return KeyboardFocusManager.getCurrentKeyboardFocusManager().
        getActiveWindow() == this;
  }

  /**
   * Returns whether this Window is focused. If there exists a focus owner, the focused Window is
   * the Window that is, or contains, that focus owner. If there is no focus owner, then no Window
   * is focused.
   * <p>
   * If the focused Window is a Frame or a Dialog it is also the active Window. Otherwise, the
   * active Window is the first Frame or Dialog that is an owner of the focused Window.
   *
   * @return whether this is the focused Window.
   * @see #isActive
   * @since 1.4
   */
  public boolean isFocused() {
    return KeyboardFocusManager.getCurrentKeyboardFocusManager().
        getGlobalFocusedWindow() == this;
  }

  /**
   * Returns whether this Window can become the focused Window, that is, whether this Window or any
   * of its subcomponents can become the focus owner. For a Frame or Dialog to be focusable, its
   * focusable Window state must be set to {@code true}. For a Window which is not a Frame or Dialog
   * to be focusable, its focusable Window state must be set to {@code true}, its nearest owning
   * Frame or Dialog must be showing on the screen, and it must contain at least one Component in
   * its focus traversal cycle. If any of these conditions is not met, then neither this Window nor
   * any of its subcomponents can become the focus owner.
   *
   * @return {@code true} if this Window can be the focused Window; {@code false} otherwise
   * @see #getFocusableWindowState
   * @see #setFocusableWindowState
   * @see #isShowing
   * @see Component#isFocusable
   * @since 1.4
   */
  public final boolean isFocusableWindow() {
    // If a Window/Frame/Dialog was made non-focusable, then it is always
    // non-focusable.
    if (!getFocusableWindowState()) {
      return false;
    }

    // All other tests apply only to Windows.
    if (this instanceof Frame || this instanceof Dialog) {
      return true;
    }

    // A Window must have at least one Component in its root focus
    // traversal cycle to be focusable.
    if (getFocusTraversalPolicy().getDefaultComponent(this) == null) {
      return false;
    }

    // A Window's nearest owning Frame or Dialog must be showing on the
    // screen.
    for (Window owner = getOwner(); owner != null; owner = owner.getOwner()) {
      if (owner instanceof Frame || owner instanceof Dialog) {
        return owner.isShowing();
      }
    }

    return false;
  }

  /**
   * Returns whether this Window can become the focused Window if it meets the other requirements
   * outlined in {@code isFocusableWindow}. If this method returns {@code false}, then {@code
   * isFocusableWindow} will return {@code false} as well. If this method returns {@code true}, then
   * {@code isFocusableWindow} may return {@code true} or {@code false} depending upon the other
   * requirements which must be met in order for a Window to be focusable.
   * <p>
   * By default, all Windows have a focusable Window state of {@code true}.
   *
   * @return whether this Window can be the focused Window
   * @see #isFocusableWindow
   * @see #setFocusableWindowState
   * @see #isShowing
   * @see Component#setFocusable
   * @since 1.4
   */
  public boolean getFocusableWindowState() {
    return focusableWindowState;
  }

  /**
   * Sets whether this Window can become the focused Window if it meets the other requirements
   * outlined in {@code isFocusableWindow}. If this Window's focusable Window state is set to {@code
   * false}, then {@code isFocusableWindow} will return {@code false}. If this Window's focusable
   * Window state is set to {@code true}, then {@code isFocusableWindow} may return {@code true} or
   * {@code false} depending upon the other requirements which must be met in order for a Window to
   * be focusable.
   * <p>
   * Setting a Window's focusability state to {@code false} is the standard mechanism for an
   * application to identify to the AWT a Window which will be used as a floating palette or
   * toolbar, and thus should be a non-focusable Window.
   * <p>
   * Setting the focusability state on a visible {@code Window} can have a delayed effect on some
   * platforms &#151; the actual change may happen only when the {@code Window} becomes hidden and
   * then visible again.  To ensure consistent behavior across platforms, set the {@code Window}'s
   * focusable state when the {@code Window} is invisible and then show it.
   *
   * @param focusableWindowState whether this Window can be the focused Window
   * @see #isFocusableWindow
   * @see #getFocusableWindowState
   * @see #isShowing
   * @see Component#setFocusable
   * @since 1.4
   */
  public void setFocusableWindowState(boolean focusableWindowState) {
    boolean oldFocusableWindowState;
    synchronized (this) {
      oldFocusableWindowState = this.focusableWindowState;
      this.focusableWindowState = focusableWindowState;
    }
    WindowPeer peer = (WindowPeer) this.peer;
    if (peer != null) {
      peer.updateFocusableWindowState();
    }
    firePropertyChange("focusableWindowState", oldFocusableWindowState, focusableWindowState);
    if (oldFocusableWindowState && !focusableWindowState && isFocused()) {
      for (Window owner = getOwner(); owner != null; owner = owner.getOwner()) {
        Component toFocus = KeyboardFocusManager.getMostRecentFocusOwner(owner);
        if (toFocus != null && toFocus.requestFocus(false, Cause.ACTIVATION)) {
          return;
        }
      }
      KeyboardFocusManager.getCurrentKeyboardFocusManager().
          clearGlobalFocusOwnerPriv();
    }
  }

  /**
   * Returns whether this window should receive focus on subsequently being shown (with a call to
   * {@link #setVisible setVisible(true)}), or being moved to the front (with a call to {@link
   * #toFront}).
   * <p>
   * By default, the window has {@code autoRequestFocus} value of {@code true}.
   *
   * @return {@code autoRequestFocus} value
   * @see #setAutoRequestFocus
   * @since 1.7
   */
  public boolean isAutoRequestFocus() {
    return autoRequestFocus;
  }

  /**
   * Sets whether this window should receive focus on subsequently being shown (with a call to
   * {@link #setVisible setVisible(true)}), or being moved to the front (with a call to {@link
   * #toFront}).
   * <p>
   * Note that {@link #setVisible setVisible(true)} may be called indirectly (e.g. when showing an
   * owner of the window makes the window to be shown). {@link #toFront} may also be called
   * indirectly (e.g. when {@link #setVisible setVisible(true)} is called on already visible
   * window). In all such cases this property takes effect as well.
   * <p>
   * The value of the property is not inherited by owned windows.
   *
   * @param autoRequestFocus whether this window should be focused on subsequently being shown or
   * being moved to the front
   * @see #isAutoRequestFocus
   * @see #isFocusableWindow
   * @see #setVisible
   * @see #toFront
   * @since 1.7
   */
  public void setAutoRequestFocus(boolean autoRequestFocus) {
    this.autoRequestFocus = autoRequestFocus;
  }

  boolean isDisposing() {
    return disposing;
  }

  /**
   * @deprecated As of J2SE 1.4, replaced by {@link Component#applyComponentOrientation
   * Component.applyComponentOrientation}.
   */
  @Deprecated
  public void applyResourceBundle(ResourceBundle rb) {
    applyComponentOrientation(ComponentOrientation.getOrientation(rb));
  }

  /**
   * @deprecated As of J2SE 1.4, replaced by {@link Component#applyComponentOrientation
   * Component.applyComponentOrientation}.
   */
  @Deprecated
  public void applyResourceBundle(String rbName) {
    applyResourceBundle(ResourceBundle.getBundle(rbName));
  }

  /*
    * Support for tracking all windows owned by this window
    */
  void addOwnedWindow(WeakReference<Window> weakWindow) {
    if (weakWindow != null) {
      synchronized (ownedWindowList) {
        // this if statement should really be an assert, but we don't
        // have asserts...
        if (!ownedWindowList.contains(weakWindow)) {
          ownedWindowList.addElement(weakWindow);
        }
      }
    }
  }

  void removeOwnedWindow(WeakReference<Window> weakWindow) {
    if (weakWindow != null) {
      // synchronized block not required since removeElement is
      // already synchronized
      ownedWindowList.removeElement(weakWindow);
    }
  }

  void connectOwnedWindow(Window child) {
    child.parent = this;
    addOwnedWindow(child.weakThis);
    child.disposerRecord.updateOwner();
  }

  private void addToWindowList() {
    synchronized (Window.class) {
      @SuppressWarnings("unchecked") Vector<WeakReference<Window>> windowList
          = (Vector<WeakReference<Window>>) appContext.get(Window.class);
      if (windowList == null) {
        windowList = new Vector<>();
        appContext.put(Window.class, windowList);
      }
      windowList.add(weakThis);
    }
  }

  private void removeFromWindowList() {
    removeFromWindowList(appContext, weakThis);
  }

  /**
   * Returns the type of the window.
   *
   * @see #setType
   * @since 1.7
   */
  public Type getType() {
    synchronized (getObjectLock()) {
      return type;
    }
  }

  /**
   * Sets the type of the window.
   * <p>
   * This method can only be called while the window is not displayable.
   *
   * @throws IllegalComponentStateException if the window is displayable.
   * @throws IllegalArgumentException if the type is {@code null}
   * @see Component#isDisplayable
   * @see #getType
   * @since 1.7
   */
  @SuppressWarnings("NestedSynchronizedStatement")
  public void setType(Type type) {
    if (type == null) {
      throw new IllegalArgumentException("type should not be null.");
    }
    synchronized (getTreeLock()) {
      if (isDisplayable()) {
        throw new IllegalComponentStateException("The window is displayable.");
      }
      synchronized (getObjectLock()) {
        this.type = type;
      }
    }
  }

  /**
   * Writes default serializable fields to stream.  Writes a list of serializable {@code
   * WindowListener}s and {@code WindowFocusListener}s as optional data. Writes a list of child
   * windows as optional data. Writes a list of icon images as optional data
   *
   * @param s the {@code ObjectOutputStream} to write
   * @serialData {@code null} terminated sequence of 0 or more pairs; the pair consists of a {@code
   * String} and {@code Object}; the {@code String} indicates the type of object and is one of the
   * following: {@code windowListenerK} indicating a {@code WindowListener} object; {@code
   * windowFocusWindowK} indicating a {@code WindowFocusListener} object; {@code ownedWindowK}
   * indicating a child {@code Window} object
   * @see Component#windowListenerK
   * @see Component#windowFocusListenerK
   * @see Component#ownedWindowK
   * @see #readObject(ObjectInputStream)
   */
  private void writeObject(ObjectOutputStream s) throws IOException {
    synchronized (this) {
      // Update old focusMgr fields so that our object stream can be read
      // by previous releases
      focusMgr = new FocusManager();
      focusMgr.focusRoot = this;
      focusMgr.focusOwner = getMostRecentFocusOwner();

      s.defaultWriteObject();

      // Clear fields so that we don't keep extra references around
      focusMgr = null;

      AWTEventMulticaster.save(s, windowListenerK, windowListener);
      AWTEventMulticaster.save(s, windowFocusListenerK, windowFocusListener);
      AWTEventMulticaster.save(s, windowStateListenerK, windowStateListener);
    }

    s.writeObject(null);

    synchronized (ownedWindowList) {
      for (int i = 0; i < ownedWindowList.size(); i++) {
        Window child = ownedWindowList.elementAt(i).get();
        if (child != null) {
          s.writeObject(ownedWindowK);
          s.writeObject(child);
        }
      }
    }
    s.writeObject(null);

    //write icon array
    if (icons != null) {
      for (Image i : icons) {
        if (i instanceof Serializable) {
          //noinspection NonSerializableObjectPassedToObjectStream
          s.writeObject(i);
        }
      }
    }
    s.writeObject(null);
  }

  //
  // Part of deserialization procedure to be called before
  // user's code.
  //
  private void initDeserializedWindow() {
    setWarningString();

    // Deserialized Windows are not yet visible.
    visible = false;

    weakThis = new WeakReference<>(this);

    anchor = new Object();
    if (disposerRecord != null) {
      disposerRecord.dispose();
    }
    disposerRecord = new WindowDisposerRecord(appContext, this);

    addToWindowList();
    initGC(null);
    ownedWindowList.clear();
    sjInputContext = new SkinJobInputContext();
  }

  private void deserializeResources(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException {

    if (windowSerializedDataVersion < 2) {
      // Translate old-style focus tracking to new model. For 1.4 and
      // later releases, we'll rely on the Window's initial focusable
      // Component.
      if (focusMgr != null) {
        if (focusMgr.focusOwner != null) {
          KeyboardFocusManager.
              setMostRecentFocusOwner(this, focusMgr.focusOwner);
        }
      }

      // This field is non-transient and relies on default serialization.
      // However, the default value is insufficient, so we need to set
      // it explicitly for object data streams prior to 1.4.
      focusableWindowState = true;
    }

    Object keyOrNull;
    while (null != (keyOrNull = s.readObject())) {
      String key = ((String) keyOrNull).intern();

      if (windowListenerK == key) {
        addWindowListener((WindowListener) s.readObject());
      } else if (windowFocusListenerK == key) {
        addWindowFocusListener((WindowFocusListener) s.readObject());
      } else if (windowStateListenerK == key) {
        addWindowStateListener((WindowStateListener) s.readObject());
      } else // skip value for unrecognized key
      {
        s.readObject();
      }
    }

    try {
      while (null != (keyOrNull = s.readObject())) {
        String key = ((String) keyOrNull).intern();

        if (ownedWindowK == key) {
          connectOwnedWindow((Window) s.readObject());
        } else // skip value for unrecognized key
        {
          s.readObject();
        }
      }

      //read icons
      Object obj = s.readObject(); //Throws OptionalDataException
      //for pre1.6 objects.
      icons = new ArrayList<>(); //Frame.readObject() assumes
      //pre1.6 version if icons is null.
      while (obj != null) {
        if (obj instanceof Image) {
          icons.add((Image) obj);
        }
        obj = s.readObject();
      }
    } catch (OptionalDataException e) {
      // 1.1 serialized form
      // ownedWindowList will be updated by Frame.readObject
    }
  }

  /**
   * Reads the {@code ObjectInputStream} and an optional list of listeners to receive various events
   * fired by the component; also reads a list of (possibly {@code null}) child windows.
   * Unrecognized keys or values will be ignored.
   *
   * @param s the {@code ObjectInputStream} to read
   * @throws HeadlessException if {@code GraphicsEnvironment.isHeadless} returns {@code true}
   * @see GraphicsEnvironment#isHeadless
   * @see #writeObject
   */
  private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException {
    initDeserializedWindow();
    GetField f = s.readFields();

    syncLWRequests = f.get("syncLWRequests", systemSyncLWRequests);
    state = f.get("state", 0);
    focusableWindowState = f.get("focusableWindowState", true);
    windowSerializedDataVersion = f.get("windowSerializedDataVersion", 1);
    locationByPlatform = f.get("locationByPlatform", locationByPlatformProp);
    // Note: 1.4 (or later) doesn't use focusMgr
    focusMgr = (FocusManager) f.get("focusMgr", null);
    ModalExclusionType et = (ModalExclusionType) f.get("modalExclusionType",
        ModalExclusionType.NO_EXCLUDE);
    setModalExclusionType(et); // since 6.0
    boolean aot = f.get("alwaysOnTop", false);
    if (aot) {
      setAlwaysOnTop(true); // since 1.5; subject to permission check
    }
    shape = (Shape) f.get("shape", null);
    opacity = f.get("opacity", 1.0f);

    securityWarningWidth = 0;
    securityWarningHeight = 0;
    securityWarningPointX = 2.0;
    securityWarningPointY = 0.0;
    securityWarningAlignmentX = RIGHT_ALIGNMENT;
    securityWarningAlignmentY = TOP_ALIGNMENT;

    deserializeResources(s);
    sjAndroidContext = SkinJobGlobals.getAndroidApplicationContext();
    sjAndroidWindow = SkinJobUtil.newAndroidWindow(sjAndroidContext);
  }

  /**
   * Sets the location of the window relative to the specified component according to the following
   * scenarios.
   * <p>
   * The target screen mentioned below is a screen to which the window should be placed after the
   * setLocationRelativeTo method is called. <ul> <li>If the component is {@code null}, or the
   * {@code GraphicsConfiguration} associated with this component is {@code null}, the window is
   * placed in the center of the screen. The center point can be obtained with the {@link
   * GraphicsEnvironment#getCenterPoint GraphicsEnvironment.getCenterPoint} method. <li>If the
   * component is not {@code null}, but it is not currently showing, the window is placed in the
   * center of the target screen defined by the {@code GraphicsConfiguration} associated with this
   * component. <li>If the component is not {@code null} and is shown on the screen, then the window
   * is located in such a way that the center of the window coincides with the center of the
   * component. </ul>
   * <p>
   * If the screens configuration does not allow the window to be moved from one screen to another,
   * then the window is only placed at the location determined according to the above conditions and
   * its {@code GraphicsConfiguration} is not changed.
   * <p>
   * <b>Note</b>: If the lower edge of the window is out of the screen, then the window is placed to
   * the side of the {@code Component} that is closest to the center of the screen. So if the
   * component is on the right part of the screen, the window is placed to its left, and vice
   * versa.
   * <p>
   * If after the window location has been calculated, the upper, left, or right edge of the window
   * is out of the screen, then the window is located in such a way that the upper, left, or right
   * edge of the window coincides with the corresponding edge of the screen. If both left and right
   * edges of the window are out of the screen, the window is placed at the left side of the screen.
   * The similar placement will occur if both top and bottom edges are out of the screen. In that
   * case, the window is placed at the top side of the screen.
   * <p>
   * The method changes the geometry-related data. Therefore, the native windowing system may ignore
   * such requests, or it may modify the requested data, so that the {@code Window} object is placed
   * and sized in a way that corresponds closely to the desktop settings.
   *
   * @param c the component in relation to which the window's location is determined
   * @see GraphicsEnvironment#getCenterPoint
   * @since 1.4
   */
  public void setLocationRelativeTo(Component c) {
    // target location
    int dx, dy;
    // target GC
    GraphicsConfiguration gc = getGraphicsConfiguration_NoClientCode();
    Rectangle gcBounds = gc.getBounds();

    Dimension windowSize = getSize();

    // search a top-level of c
    Window componentWindow = SunToolkit.getContainingWindow(c);
    if (c == null || componentWindow == null) {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
      gcBounds = gc.getBounds();
      Point centerPoint = ge.getCenterPoint();
      dx = centerPoint.x - windowSize.width / 2;
      dy = centerPoint.y - windowSize.height / 2;
    } else if (!c.isShowing()) {
      gc = componentWindow.getGraphicsConfiguration();
      gcBounds = gc.getBounds();
      dx = gcBounds.x + (gcBounds.width - windowSize.width) / 2;
      dy = gcBounds.y + (gcBounds.height - windowSize.height) / 2;
    } else {
      gc = componentWindow.getGraphicsConfiguration();
      gcBounds = gc.getBounds();
      Dimension compSize = c.getSize();
      Point compLocation = c.getLocationOnScreen();
      dx = compLocation.x + (compSize.width - windowSize.width) / 2;
      dy = compLocation.y + (compSize.height - windowSize.height) / 2;

      // Adjust for bottom edge being offscreen
      if (dy + windowSize.height > gcBounds.y + gcBounds.height) {
        dy = gcBounds.y + gcBounds.height - windowSize.height;
        dx = compLocation.x - gcBounds.x + compSize.width / 2 < gcBounds.width / 2 ? compLocation.x
            + compSize.width : compLocation.x - windowSize.width;
      }
    }

    // Avoid being placed off the edge of the screen:
    // bottom
    if (dy + windowSize.height > gcBounds.y + gcBounds.height) {
      dy = gcBounds.y + gcBounds.height - windowSize.height;
    }
    // top
    if (dy < gcBounds.y) {
      dy = gcBounds.y;
    }
    // right
    if (dx + windowSize.width > gcBounds.x + gcBounds.width) {
      dx = gcBounds.x + gcBounds.width - windowSize.width;
    }
    // left
    if (dx < gcBounds.x) {
      dx = gcBounds.x;
    }

    setLocation(dx, dy);
  }

  /**
   * Overridden from Component.  Top-level Windows should not propagate a MouseWheelEvent beyond
   * themselves into their owning Windows.
   */
  void deliverMouseWheelToAncestor(MouseWheelEvent e) {
  }

  Component getTemporaryLostComponent() {
    return temporaryLostComponent;
  }

    /*
     * --- Accessibility Support ---
     *
     */

  Component setTemporaryLostComponent(Component component) {
    Component previousComp = temporaryLostComponent;
    // Check that "component" is an acceptable focus owner and don't store it otherwise
    // - or later we will have problems with opposite while handling  WINDOW_GAINED_FOCUS
    temporaryLostComponent = component == null || component.canBeFocusOwner() ? component : null;
    return previousComp;
  }

  @Override
  final void clearMostRecentFocusOwnerOnHide() {
        /* do nothing */
  }

  /**
   * @since 1.7
   */
  @Override
  public void paint(Graphics g) {
    if (!isOpaque()) {
      Graphics gg = g.create();
      try {
        if (gg instanceof Graphics2D) {
          gg.setColor(getBackground());
          ((Graphics2D) gg).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
          gg.fillRect(0, 0, getWidth(), getHeight());
        }
      } finally {
        gg.dispose();
      }
    }
    super.paint(g);
  }

  /**
   * Dispatches an event to this window or one of its sub components.
   *
   * @param e the event
   */
  @Override
  void dispatchEventImpl(AWTEvent e) {
    if (e.getID() == ComponentEvent.COMPONENT_RESIZED) {
      invalidate();
      validate();
    }
    super.dispatchEventImpl(e);
  }

  // REMIND: remove when filtering is handled at lower level
  @Override
  boolean eventEnabled(AWTEvent e) {
    switch (e.id) {
      case WindowEvent.WINDOW_OPENED:
      case WindowEvent.WINDOW_CLOSING:
      case WindowEvent.WINDOW_CLOSED:
      case WindowEvent.WINDOW_ICONIFIED:
      case WindowEvent.WINDOW_DEICONIFIED:
      case WindowEvent.WINDOW_ACTIVATED:
      case WindowEvent.WINDOW_DEACTIVATED:
        return (eventMask & AWTEvent.WINDOW_EVENT_MASK) != 0 || windowListener != null;
      case WindowEvent.WINDOW_GAINED_FOCUS:
      case WindowEvent.WINDOW_LOST_FOCUS:
        return (eventMask & AWTEvent.WINDOW_FOCUS_EVENT_MASK) != 0 || windowFocusListener != null;
      case WindowEvent.WINDOW_STATE_CHANGED:
        return (eventMask & AWTEvent.WINDOW_STATE_EVENT_MASK) != 0 || windowStateListener != null;
      default:
        break;
    }
    return super.eventEnabled(e);
  }

  /**
   * Returns an array of all the objects currently registered as <code><em>Foo</em>Listener</code>s
   * upon this {@code Window}. <code><em>Foo</em>Listener</code>s are registered using the
   * <code>add<em>Foo</em>Listener</code> method.
   * <p>
   * <p>
   * <p>
   * You can specify the {@code listenerType} argument with a class literal, such as
   * <code><em>Foo</em>Listener.class</code>. For example, you can query a {@code Window} {@code w}
   * for its window listeners with the following code:
   * <p>
   * <pre>WindowListener[] wls = (WindowListener[])(w.getListeners(WindowListener.class));</pre>
   *
   * If no such listeners exist, this method returns an empty array.
   *
   * @param listenerType the type of listeners requested; this parameter should specify an interface
   * that descends from {@code java.util.EventListener}
   * @return an array of all objects registered as <code><em>Foo</em>Listener</code>s on this
   * window, or an empty array if no such listeners have been added
   * @throws ClassCastException if {@code listenerType} doesn't specify a class or interface that
   * implements {@code java.util.EventListener}
   * @throws NullPointerException if {@code listenerType} is {@code null}
   * @see #getWindowListeners
   * @since 1.3
   */
  @Override
  public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
    EventListener l;
    if (listenerType == WindowFocusListener.class) {
      l = windowFocusListener;
    } else if (listenerType == WindowStateListener.class) {
      l = windowStateListener;
    } else if (listenerType == WindowListener.class) {
      l = windowListener;
    } else {
      return super.getListeners(listenerType);
    }
    return AWTEventMulticaster.getListeners(l, listenerType);
  }

  /**
   * Processes events on this window. If the event is an {@code WindowEvent}, it invokes the {@code
   * processWindowEvent} method, else it invokes its superclass's {@code processEvent}. <p>Note that
   * if the event parameter is {@code null} the behavior is unspecified and may result in an
   * exception.
   *
   * @param e the event
   */
  @Override
  protected void processEvent(AWTEvent e) {
    if (e instanceof WindowEvent) {
      switch (e.getID()) {
        case WindowEvent.WINDOW_OPENED:
        case WindowEvent.WINDOW_CLOSING:
        case WindowEvent.WINDOW_CLOSED:
        case WindowEvent.WINDOW_ICONIFIED:
        case WindowEvent.WINDOW_DEICONIFIED:
        case WindowEvent.WINDOW_ACTIVATED:
        case WindowEvent.WINDOW_DEACTIVATED:
          processWindowEvent((WindowEvent) e);
          break;
        case WindowEvent.WINDOW_GAINED_FOCUS:
        case WindowEvent.WINDOW_LOST_FOCUS:
          processWindowFocusEvent((WindowEvent) e);
          break;
        case WindowEvent.WINDOW_STATE_CHANGED:
          processWindowStateEvent((WindowEvent) e);
          break;
      }
      return;
    }
    super.processEvent(e);
  }

  /**
   * Makes this Window displayable by creating the connection to its native screen resource. This
   * method is called internally by the toolkit and should not be called directly by programs.
   *
   * @see Component#isDisplayable
   * @see Container#removeNotify
   * @since JDK1.0
   */
  @SuppressWarnings("NestedSynchronizedStatement")
  @Override
  public void addNotify() {
    synchronized (getTreeLock()) {
      Container parent = this.parent;
      if (parent != null && parent.getPeer() == null) {
        parent.addNotify();
      }
      if (peer == null) {
        peer = getToolkit().createWindow(this);
      }
      synchronized (allWindows) {
        allWindows.add(this);
      }
      super.addNotify();
    }
  }

  @SuppressWarnings("NestedSynchronizedStatement")
  @Override
  public void removeNotify() {
    synchronized (getTreeLock()) {
      synchronized (allWindows) {
        allWindows.remove(this);
      }
      super.removeNotify();
    }
  }

  /**
   * Gets a focus traversal key for this Window. (See {@code setFocusTraversalKeys} for a full
   * description of each key.)
   * <p>
   * If the traversal key has not been explicitly set for this Window, then this Window's parent's
   * traversal key is returned. If the traversal key has not been explicitly set for any of this
   * Window's ancestors, then the current KeyboardFocusManager's default traversal key is returned.
   *
   * @param id one of KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
   * KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, or KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS
   * @return the AWTKeyStroke for the specified key
   * @throws IllegalArgumentException if id is not one of KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
   * KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, or
   * KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS
   * @see Container#setFocusTraversalKeys
   * @see KeyboardFocusManager#FORWARD_TRAVERSAL_KEYS
   * @see KeyboardFocusManager#BACKWARD_TRAVERSAL_KEYS
   * @see KeyboardFocusManager#UP_CYCLE_TRAVERSAL_KEYS
   * @see KeyboardFocusManager#DOWN_CYCLE_TRAVERSAL_KEYS
   * @since 1.4
   */
  @Override
  public Set<AWTKeyStroke> getFocusTraversalKeys(int id) {
    if (id < 0 || id >= KeyboardFocusManager.TRAVERSAL_KEY_LENGTH) {
      throw new IllegalArgumentException("invalid focus traversal key identifier");
    }

    // Okay to return Set directly because it is an unmodifiable view
    @SuppressWarnings("rawtypes") Set keystrokes = focusTraversalKeys != null
        ? focusTraversalKeys[id] : null;

    return keystrokes != null ? keystrokes : KeyboardFocusManager.getCurrentKeyboardFocusManager().
        getDefaultFocusTraversalKeys(id);
  }

  /**
   * Adds a PropertyChangeListener to the listener list. The listener is registered for all bound
   * properties of this class, including the following: <ul> <li>this Window's font ("font")</li>
   * <li>this Window's background color ("background")</li> <li>this Window's foreground color
   * ("foreground")</li> <li>this Window's focusability ("focusable")</li> <li>this Window's focus
   * traversal keys enabled state ("focusTraversalKeysEnabled")</li> <li>this Window's Set of
   * FORWARD_TRAVERSAL_KEYS ("forwardFocusTraversalKeys")</li> <li>this Window's Set of
   * BACKWARD_TRAVERSAL_KEYS ("backwardFocusTraversalKeys")</li> <li>this Window's Set of
   * UP_CYCLE_TRAVERSAL_KEYS ("upCycleFocusTraversalKeys")</li> <li>this Window's Set of
   * DOWN_CYCLE_TRAVERSAL_KEYS ("downCycleFocusTraversalKeys")</li> <li>this Window's focus
   * traversal policy ("focusTraversalPolicy") </li> <li>this Window's focusable Window state
   * ("focusableWindowState") </li> <li>this Window's always-on-top state("alwaysOnTop")</li> </ul>
   * Note that if this Window is inheriting a bound property, then no event will be fired in
   * response to a change in the inherited property.
   * <p>
   * If listener is null, no exception is thrown and no action is performed.
   *
   * @param listener the PropertyChangeListener to be added
   * @see Component#removePropertyChangeListener
   * @see #addPropertyChangeListener(String, PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    super.addPropertyChangeListener(listener);
  }

  /**
   * Adds a PropertyChangeListener to the listener list for a specific property. The specified
   * property may be user-defined, or one of the following: <ul> <li>this Window's font
   * ("font")</li> <li>this Window's background color ("background")</li> <li>this Window's
   * foreground color ("foreground")</li> <li>this Window's focusability ("focusable")</li> <li>this
   * Window's focus traversal keys enabled state ("focusTraversalKeysEnabled")</li> <li>this
   * Window's Set of FORWARD_TRAVERSAL_KEYS ("forwardFocusTraversalKeys")</li> <li>this Window's Set
   * of BACKWARD_TRAVERSAL_KEYS ("backwardFocusTraversalKeys")</li> <li>this Window's Set of
   * UP_CYCLE_TRAVERSAL_KEYS ("upCycleFocusTraversalKeys")</li> <li>this Window's Set of
   * DOWN_CYCLE_TRAVERSAL_KEYS ("downCycleFocusTraversalKeys")</li> <li>this Window's focus
   * traversal policy ("focusTraversalPolicy") </li> <li>this Window's focusable Window state
   * ("focusableWindowState") </li> <li>this Window's always-on-top state("alwaysOnTop")</li> </ul>
   * Note that if this Window is inheriting a bound property, then no event will be fired in
   * response to a change in the inherited property.
   * <p>
   * If listener is null, no exception is thrown and no action is performed.
   *
   * @param propertyName one of the property names listed above
   * @param listener the PropertyChangeListener to be added
   * @see #addPropertyChangeListener(PropertyChangeListener)
   * @see Component#removePropertyChangeListener
   */
  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    super.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  final void mixOnReshaping() {
    // The shape calculated by mixing code is not intended to be applied
    // to windows or frames
  }

  /**
   * Checks whether this window can contain focus owner. Verifies that it is focusable and as
   * container it can container focus owner.
   *
   * @since 1.5
   */
  @Override
  boolean canContainFocusOwner(Component focusOwnerCandidate) {
    return super.canContainFocusOwner(focusOwnerCandidate) && isFocusableWindow();
  }

  // Should only be called while holding tree lock
  @Override
  void adjustDecendantsOnParent(int num) {
    // do nothing since parent == owner and we shouldn't
    // ajust counter on owner
  }

  /**
   * Indicates if this container is a validate root.
   * <p>
   * {@code Window} objects are the validate roots, and, therefore, they override this method to
   * return {@code true}.
   *
   * @return {@code true}
   * @since 1.7
   */
  @Override
  public boolean isValidateRoot() {
    return true;
  }

  // ******************** SHAPES & TRANSPARENCY CODE ********************

  /**
   * Always returns {@code true} because all Windows must be roots of a focus traversal cycle.
   *
   * @return {@code true}
   * @see #setFocusCycleRoot
   * @see Container#setFocusTraversalPolicy
   * @see Container#getFocusTraversalPolicy
   * @since 1.4
   */
  @Override
  public final boolean isFocusCycleRoot() {
    return true;
  }

  /**
   * Does nothing because Windows must always be roots of a focus traversal cycle. The passed-in
   * value is ignored.
   *
   * @param focusCycleRoot this value is ignored
   * @see #isFocusCycleRoot
   * @see Container#setFocusTraversalPolicy
   * @see Container#getFocusTraversalPolicy
   * @since 1.4
   */
  @Override
  public final void setFocusCycleRoot(boolean focusCycleRoot) {
  }

  /**
   * Implements a debugging hook -- checks to see if the user has typed <i>control-shift-F1</i>.  If
   * so, the list of child windows is dumped to {@code System.out}.
   *
   * @param e the keyboard event
   */
  @Override
  void preProcessKeyEvent(KeyEvent e) {
    // Dump the list of child windows to System.out.
    if (e.isActionKey() && e.getKeyCode() == KeyEvent.VK_F1 &&
        e.isControlDown() && e.isShiftDown() &&
        e.getID() == KeyEvent.KEY_PRESSED) {
      list(System.out, 0);
    }
  }

  @Override
  void postProcessKeyEvent(KeyEvent e) {
    // Do nothing
  }

  /**
   * Returns {@code true} if this Window will appear at the default location for the native
   * windowing system the next time this Window is made visible. This method always returns {@code
   * false} if the Window is showing on the screen.
   *
   * @return whether this Window will appear at the default location
   * @see #setLocationByPlatform
   * @see #isShowing
   * @since 1.5
   */
  public boolean isLocationByPlatform() {
    synchronized (getTreeLock()) {
      return locationByPlatform;
    }
  }

  /**
   * Sets whether this Window should appear at the default location for the native windowing system
   * or at the current location (returned by {@code getLocation}) the next time the Window is made
   * visible. This behavior resembles a native window shown without programmatically setting its
   * location.  Most windowing systems cascade windows if their locations are not explicitly set.
   * The actual location is determined once the window is shown on the screen.
   * <p>
   * This behavior can also be enabled by setting the System Property
   * "java.awt.Window.locationByPlatform" to "true", though calls to this method take precedence.
   * <p>
   * Calls to {@code setVisible}, {@code setLocation} and {@code setBounds} after calling {@code
   * setLocationByPlatform} clear this property of the Window.
   * <p>
   * For example, after the following code is executed:
   * <pre>
   * setLocationByPlatform(true);
   * setVisible(true);
   * boolean flag = isLocationByPlatform();
   * </pre>
   * The window will be shown at platform's default location and {@code flag} will be {@code
   * false}.
   * <p>
   * In the following sample:
   * <pre>
   * setLocationByPlatform(true);
   * setLocation(10, 10);
   * boolean flag = isLocationByPlatform();
   * setVisible(true);
   * </pre>
   * The window will be shown at (10, 10) and {@code flag} will be {@code false}.
   *
   * @param locationByPlatform {@code true} if this Window should appear at the default location,
   * {@code false} if at the current location
   * @throws IllegalComponentStateException if the window is showing on screen and
   * locationByPlatform is {@code true}.
   * @see #setLocation
   * @see #isShowing
   * @see #setVisible
   * @see #isLocationByPlatform
   * @see System#getProperty(String)
   * @since 1.5
   */
  public void setLocationByPlatform(boolean locationByPlatform) {
    synchronized (getTreeLock()) {
      if (locationByPlatform && isShowing()) {
        throw new IllegalComponentStateException("The window is showing on screen.");
      }
      this.locationByPlatform = locationByPlatform;
    }
  }

  /**
   * Returns the opacity of the window.
   *
   * @return the opacity of the window
   * @see Window#setOpacity(float)
   * @see WindowTranslucency
   * @since 1.7
   */
  public float getOpacity() {
    synchronized (getTreeLock()) {
      return opacity;
    }
  }

  /**
   * Sets the opacity of the window.
   * <p>
   * The opacity value is in the range [0..1]. Note that setting the opacity level of 0 may or may
   * not disable the mouse event handling on this window. This is a platform-dependent behavior.
   * <p>
   * The following conditions must be met in order to set the opacity value less than {@code 1.0f}:
   * <ul> <li>The {@link WindowTranslucency#TRANSLUCENT TRANSLUCENT} translucency must be supported
   * by the underlying system <li>The window must be undecorated (see {@link Frame#setUndecorated}
   * and {@link Dialog#setUndecorated}) <li>The window must not be in full-screen mode (see {@link
   * GraphicsDevice#setFullScreenWindow(Window)}) </ul>
   * <p>
   * If the requested opacity value is less than {@code 1.0f}, and any of the above conditions are
   * not met, the window opacity will not change, and the {@code IllegalComponentStateException}
   * will be thrown.
   * <p>
   * The translucency levels of individual pixels may also be effected by the alpha component of
   * their color (see {@link Window#setBackground(Color)}) and the current shape of this window (see
   * {@link #setShape(Shape)}).
   *
   * @param opacity the opacity level to set to the window
   * @throws IllegalArgumentException if the opacity is out of the range [0..1]
   * @throws IllegalComponentStateException if the window is decorated and the opacity is less than
   * {@code 1.0f}
   * @throws IllegalComponentStateException if the window is in full screen mode, and the opacity is
   * less than {@code 1.0f}
   * @throws UnsupportedOperationException if the {@code GraphicsDevice.WindowTranslucency#TRANSLUCENT
   * TRANSLUCENT} translucency is not supported and the opacity is less than {@code 1.0f}
   * @see Window#getOpacity
   * @see Window#setBackground(Color)
   * @see Window#setShape(Shape)
   * @see Frame#isUndecorated
   * @see Dialog#isUndecorated
   * @see WindowTranslucency
   * @see GraphicsDevice#isWindowTranslucencySupported(WindowTranslucency)
   * @since 1.7
   */
  public void setOpacity(float opacity) {
    synchronized (getTreeLock()) {
      if (opacity < 0.0f || opacity > 1.0f) {
        throw new IllegalArgumentException(
            "The value of opacity should be in the range [0.0f .. 1.0f].");
      }
      if (opacity < 1.0f) {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        GraphicsDevice gd = gc.getDevice();
        if (gc.getDevice().getFullScreenWindow() == this) {
          throw new IllegalComponentStateException(
              "Setting opacity for full-screen window is not supported.");
        }
        if (!gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT)) {
          throw new UnsupportedOperationException("TRANSLUCENT translucency is not supported.");
        }
      }
      this.opacity = opacity;
      WindowPeer peer = (WindowPeer) getPeer();
      if (peer != null) {
        peer.setOpacity(opacity);
      }
    }
  }

  /**
   * Returns the shape of the window.
   * <p>
   * The value returned by this method may not be the same as previously set with {@code
   * setShape(shape)}, but it is guaranteed to represent the same shape.
   *
   * @return the shape of the window or {@code null} if no shape is specified for the window
   * @see Window#setShape(Shape)
   * @see WindowTranslucency
   * @since 1.7
   */
  public Shape getShape() {
    synchronized (getTreeLock()) {
      return shape == null ? null : new Path2D.Float(shape);
    }
  }

  /**
   * Sets the shape of the window.
   * <p>
   * Setting a shape cuts off some parts of the window. Only the parts that belong to the given
   * {@link Shape} remain visible and clickable. If the shape argument is {@code null}, this method
   * restores the default shape, making the window rectangular on most platforms.
   * <p>
   * The following conditions must be met to set a non-null shape: <ul> <li>The {@link
   * WindowTranslucency#PERPIXEL_TRANSPARENT PERPIXEL_TRANSPARENT} translucency must be supported by
   * the underlying system <li>The window must be undecorated (see {@link Frame#setUndecorated} and
   * {@link Dialog#setUndecorated}) <li>The window must not be in full-screen mode (see {@link
   * GraphicsDevice#setFullScreenWindow(Window)}) </ul>
   * <p>
   * If the requested shape is not {@code null}, and any of the above conditions are not met, the
   * shape of this window will not change, and either the {@code UnsupportedOperationException} or
   * {@code IllegalComponentStateException} will be thrown.
   * <p>
   * The translucency levels of individual pixels may also be effected by the alpha component of
   * their color (see {@link Window#setBackground(Color)}) and the opacity value (see {@link
   * #setOpacity(float)}). See {@link WindowTranslucency} for more details.
   *
   * @param shape the shape to set to the window
   * @throws IllegalComponentStateException if the shape is not {@code null} and the window is
   * decorated
   * @throws IllegalComponentStateException if the shape is not {@code null} and the window is in
   * full-screen mode
   * @throws UnsupportedOperationException if the shape is not {@code null} and {@link
   * GraphicsDevice .WindowTranslucency#PERPIXEL_TRANSPARENT PERPIXEL_TRANSPARENT} translucency is
   * not supported
   * @see Window#getShape()
   * @see Window#setBackground(Color)
   * @see Window#setOpacity(float)
   * @see Frame#isUndecorated
   * @see Dialog#isUndecorated
   * @see WindowTranslucency
   * @see GraphicsDevice#isWindowTranslucencySupported(WindowTranslucency)
   * @since 1.7
   */
  public void setShape(Shape shape) {
    synchronized (getTreeLock()) {
      if (shape != null) {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        GraphicsDevice gd = gc.getDevice();
        if (gc.getDevice().getFullScreenWindow() == this) {
          throw new IllegalComponentStateException(
              "Setting shape for full-screen window is not supported.");
        }
        if (!gd.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSPARENT)) {
          throw new UnsupportedOperationException(
              "PERPIXEL_TRANSPARENT translucency is not supported.");
        }
      }
      this.shape = shape == null ? null : new Path2D.Float(shape);
      WindowPeer peer = (WindowPeer) getPeer();
      if (peer != null) {
        peer.applyShape(shape);
      }
    }
  }

  void updateWindow() {
    synchronized (getTreeLock()) {
      WindowPeer peer = (WindowPeer) getPeer();
      if (peer != null) {
        peer.updateWindow();
      }
    }
  }

  // ************************** MIXING CODE *******************************

  /**
   * Calculate the position of the security warning.
   * <p>
   * This method gets the window location/size as reported by the native system since the locally
   * cached values may represent outdated data.
   * <p>
   * The method is used from the native code, or via AWTAccessor.
   * <p>
   * NOTE: this method is invoked on the toolkit thread, and therefore is not supposed to become
   * public/user-overridable.
   */
  Point2D calculateSecurityWarningPosition(double x, double y, double w, double h) {
    // The position according to the spec of SecurityWarning.setPosition()
    double wx = x + w * securityWarningAlignmentX + securityWarningPointX;
    double wy = y + h * securityWarningAlignmentY + securityWarningPointY;

    // First, make sure the warning is not too far from the window bounds
    wx = limit(wx, x - securityWarningWidth - 2, x + w + 2);
    wy = limit(wy, y - securityWarningHeight - 2, y + h + 2);

    // Now make sure the warning window is visible on the screen
    GraphicsConfiguration graphicsConfig = getGraphicsConfiguration_NoClientCode();
    Rectangle screenBounds = graphicsConfig.getBounds();
    Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfig);

    wx = limit(wx,
        screenBounds.x + screenInsets.left,
        screenBounds.x + screenBounds.width - screenInsets.right - securityWarningWidth);
    wy = limit(wy,
        screenBounds.y + screenInsets.top,
        screenBounds.y + screenBounds.height - screenInsets.bottom - securityWarningHeight);

    return new Point2D.Double(wx, wy);
  }

  /**
   * Enumeration of available <i>window types</i>.
   * <p>
   * A window type defines the generic visual appearance and behavior of a top-level window. For
   * example, the type may affect the kind of decorations of a decorated {@code Frame} or {@code
   * Dialog} instance.
   * <p>
   * Some platforms may not fully support a certain window type. Depending on the level of support,
   * some properties of the window type may be disobeyed.
   *
   * @see #getType
   * @see #setType
   * @since 1.7
   */
  public enum Type {
    /**
     * Represents a <i>normal</i> window.
     * <p>
     * This is the default type for objects of the {@code Window} class or its descendants. Use this
     * type for regular top-level windows.
     */
    NORMAL,

    /**
     * Represents a <i>utility</i> window.
     * <p>
     * A utility window is usually a small window such as a toolbar or a palette. The native system
     * may render the window with smaller title-bar if the window is either a {@code Frame} or a
     * {@code Dialog} object, and if it has its decorations enabled.
     */
    UTILITY,

    /**
     * Represents a <i>popup</i> window.
     * <p>
     * A popup window is a temporary window such as a drop-down menu or a tooltip. On some
     * platforms, windows of that type may be forcibly made undecorated even if they are instances
     * of the {@code Frame} or {@code Dialog} class, and have decorations enabled.
     */
    POPUP
  }

  static class WindowDisposerRecord {
    final WeakReference<Window> weakThis;
    final WeakReference<AppContext> context;
    WeakReference<Window> owner;

    WindowDisposerRecord(AppContext context, Window victim) {
      weakThis = victim.weakThis;
      this.context = new WeakReference<>(context);
    }

    public void updateOwner() {
      Window victim = weakThis.get();
      owner = victim == null ? null : new WeakReference<>(victim.getOwner());
    }

    public void dispose() {
      if (owner != null) {
        Window parent = owner.get();
        if (parent != null) {
          parent.removeOwnedWindow(weakThis);
        }
      }
      AppContext ac = context.get();
      if (null != ac) {
        removeFromWindowList(ac, weakThis);
      }
    }
  }

  // ****************** END OF MIXING CODE ********************************

  /**
   * This class is no longer used, but is maintained for Serialization backward-compatibility.
   */
  static class FocusManager implements Serializable {
    /*
       * JDK 1.1 serialVersionUID
       */
    static final long serialVersionUID = 2491878825643557906L;
    Container focusRoot;
    Component focusOwner;
  }
} // class Window


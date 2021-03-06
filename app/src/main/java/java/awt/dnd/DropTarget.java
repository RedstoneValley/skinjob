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

package java.awt.dnd;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.dnd.peer.DropTargetPeer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

import skinjob.SkinJobGlobals;

/**
 * The {@code DropTarget} is associated with a {@code Component} when that {@code Component} wishes
 * to accept drops during Drag and Drop operations.
 * <p>
 * Each {@code DropTarget} is associated with a {@code FlavorMap}. The default {@code FlavorMap}
 * hereafter designates the {@code FlavorMap} returned by {@code SystemFlavorMap.getDefaultFlavorMap()}.
 *
 * @since 1.2
 */

public class DropTarget implements DropTargetListener, Serializable {

  private static final long serialVersionUID = -6283860791671019047L;
  /**
   * Default permissible actions supported by this DropTarget.
   *
   * @serial
   * @see #setDefaultActions
   * @see #getDefaultActions
   */
  int actions = DnDConstants.ACTION_COPY_OR_MOVE;
  /**
   * {@code true} if the DropTarget is accepting Drag &amp; Drop operations.
   *
   * @serial
   */
  boolean active = true;
  /**
   * The DropTargetContext associated with this DropTarget.
   *
   * @serial
   */
  private DropTargetContext dropTargetContext = createDropTargetContext();
  /**
   * The Component associated with this DropTarget.
   *
   * @serial
   */
  private Component component;
  /*
   * That Component's  Peer
   */
  private transient ComponentPeer componentPeer;
  /*
   * That Component's "native" Peer
   */
  private transient ComponentPeer nativePeer;
  private transient DropTargetAutoScroller autoScroller;
  private transient DropTargetListener dtListener;
  private transient FlavorMap flavorMap;
  /*
   * If the dragging is currently inside this drop target
   */
  private transient boolean isDraggingInside;

  /**
   * Creates a new DropTarget given the {@code Component} to associate itself with, an {@code int}
   * representing the default acceptable action(s) to support, a {@code DropTargetListener} to
   * handle event processing, a {@code boolean} indicating if the {@code DropTarget} is currently
   * accepting drops, and a {@code FlavorMap} to use (or null for the default {@code FlavorMap}).
   * <p>
   * The Component will receive drops only if it is enabled.
   *
   * @param c The {@code Component} with which this {@code DropTarget} is associated
   * @param ops The default acceptable actions for this {@code DropTarget}
   * @param dtl The {@code DropTargetListener} for this {@code DropTarget}
   * @param act Is the {@code DropTarget} accepting drops.
   * @param fm The {@code FlavorMap} to use, or null for the default {@code FlavorMap}
   * @throws HeadlessException if GraphicsEnvironment.isHeadless() returns true
   * @see GraphicsEnvironment#isHeadless
   */
  public DropTarget(
      Component c, int ops, DropTargetListener dtl, boolean act, FlavorMap fm)
      throws HeadlessException {

    component = c;

    setDefaultActions(ops);

    if (dtl != null) {
      try {
        addDropTargetListener(dtl);
      } catch (TooManyListenersException tmle) {
        // do nothing!
      }
    }

    if (c != null) {
      c.setDropTarget(this);
      setActive(act);
    }

    flavorMap = fm != null ? fm : SystemFlavorMap.getDefaultFlavorMap();
  }

  /**
   * Creates a {@code DropTarget} given the {@code Component} to associate itself with, an {@code
   * int} representing the default acceptable action(s) to support, a {@code DropTargetListener} to
   * handle event processing, and a {@code boolean} indicating if the {@code DropTarget} is
   * currently accepting drops.
   * <p>
   * The Component will receive drops only if it is enabled.
   *
   * @param c The {@code Component} with which this {@code DropTarget} is associated
   * @param ops The default acceptable actions for this {@code DropTarget}
   * @param dtl The {@code DropTargetListener} for this {@code DropTarget}
   * @param act Is the {@code DropTarget} accepting drops.
   * @throws HeadlessException if GraphicsEnvironment.isHeadless() returns true
   * @see GraphicsEnvironment#isHeadless
   */
  public DropTarget(
      Component c, int ops, DropTargetListener dtl, boolean act) throws HeadlessException {
    this(c, ops, dtl, act, null);
  }

  /**
   * Creates a {@code DropTarget}.
   *
   * @throws HeadlessException if GraphicsEnvironment.isHeadless() returns true
   * @see GraphicsEnvironment#isHeadless
   */
  public DropTarget() throws HeadlessException {
    this(null, DnDConstants.ACTION_COPY_OR_MOVE, null, true, null);
  }

  /**
   * Creates a {@code DropTarget} given the {@code Component} to associate itself with, and the
   * {@code DropTargetListener} to handle event processing.
   * <p>
   * The Component will receive drops only if it is enabled.
   *
   * @param c The {@code Component} with which this {@code DropTarget} is associated
   * @param dtl The {@code DropTargetListener} for this {@code DropTarget}
   * @throws HeadlessException if GraphicsEnvironment.isHeadless() returns true
   * @see GraphicsEnvironment#isHeadless
   */
  public DropTarget(Component c, DropTargetListener dtl) throws HeadlessException {
    this(c, DnDConstants.ACTION_COPY_OR_MOVE, dtl, true, null);
  }

  /**
   * Creates a {@code DropTarget} given the {@code Component} to associate itself with, an {@code
   * int} representing the default acceptable action(s) to support, and a {@code DropTargetListener}
   * to handle event processing.
   * <p>
   * The Component will receive drops only if it is enabled.
   *
   * @param c The {@code Component} with which this {@code DropTarget} is associated
   * @param ops The default acceptable actions for this {@code DropTarget}
   * @param dtl The {@code DropTargetListener} for this {@code DropTarget}
   * @throws HeadlessException if GraphicsEnvironment.isHeadless() returns true
   * @see GraphicsEnvironment#isHeadless
   */
  public DropTarget(Component c, int ops, DropTargetListener dtl) throws HeadlessException {
    this(c, ops, dtl, true);
  }

  /**
   * Gets the {@code Component} associated with this {@code DropTarget}.
   * <p>
   *
   * @return the current {@code Component}
   */

  public synchronized Component getComponent() {
    return component;
  }

  /**
   * Note: this interface is required to permit the safe association of a DropTarget with a
   * Component in one of two ways, either: {@code component.setDropTarget(droptarget); } or {@code
   * droptarget.setComponent(component); }
   * <p>
   * The Component will receive drops only if it is enabled.
   *
   * @param c The new {@code Component} this {@code DropTarget} is to be associated with.
   */

  public synchronized void setComponent(Component c) {
    if (component == c || component != null && component.equals(c)) {
      return;
    }

    Component old;
    ComponentPeer oldPeer = null;

    if ((old = component) != null) {
      clearAutoscroll();

      component = null;

      if (componentPeer != null) {
        oldPeer = componentPeer;
        removeNotify(componentPeer);
      }

      old.setDropTarget(null);
    }

    if ((component = c) != null) {
      try {
        c.setDropTarget(this);
      } catch (Exception e) { // undo the change
        if (old != null) {
          old.setDropTarget(this);
          addNotify(oldPeer);
        }
      }
    }
  }

  /*
   * Called by DropTargetContext.setTargetActions()
   * with appropriate synchronization.
   */
  void doSetDefaultActions(int ops) {
    actions = ops;
  }

  /**
   * Gets an {@code int} representing the current action(s) supported by this {@code DropTarget}.
   * <p>
   *
   * @return the current default actions
   */

  public int getDefaultActions() {
    return actions;
  }

  /**
   * Sets the default acceptable actions for this {@code DropTarget}
   * <p>
   *
   * @param ops the default actions
   * @see DnDConstants
   */

  public void setDefaultActions(int ops) {
    getDropTargetContext().setTargetActions(
        ops & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE));
  }

  /**
   * Reports whether or not this {@code DropTarget} is currently active (ready to accept drops).
   * <p>
   *
   * @return {@code true} if active, {@code false} if not
   */

  public boolean isActive() {
    return active;
  }

  /**
   * Sets the DropTarget active if {@code true}, inactive if {@code false}.
   * <p>
   *
   * @param isActive sets the {@code DropTarget} (in)active.
   */

  public synchronized void setActive(boolean isActive) {
    if (isActive != active) {
      active = isActive;
    }

    if (!active) {
      clearAutoscroll();
    }
  }

  /**
   * Adds a new {@code DropTargetListener} (UNICAST SOURCE).
   * <p>
   *
   * @param dtl The new {@code DropTargetListener}
   * <p>
   * @throws TooManyListenersException if a {@code DropTargetListener} is already added to this
   * {@code DropTarget}.
   */

  public synchronized void addDropTargetListener(DropTargetListener dtl)
      throws TooManyListenersException {
    if (dtl == null) {
      return;
    }

    if (equals(dtl)) {
      throw new IllegalArgumentException("DropTarget may not be its own " + "Listener");
    }

    if (dtListener == null) {
      dtListener = dtl;
    } else {
      throw new TooManyListenersException();
    }
  }

  /**
   * Removes the current {@code DropTargetListener} (UNICAST SOURCE).
   * <p>
   *
   * @param dtl the DropTargetListener to deregister.
   */

  public synchronized void removeDropTargetListener(DropTargetListener dtl) {
    if (dtl != null && dtListener != null) {
      if (dtListener.equals(dtl)) {
        dtListener = null;
      } else {
        throw new IllegalArgumentException("listener mismatch");
      }
    }
  }

  /**
   * Calls {@code dragEnter} on the registered {@code DropTargetListener} and passes it the
   * specified {@code DropTargetDragEvent}. Has no effect if this {@code DropTarget} is not active.
   *
   * @param dtde the {@code DropTargetDragEvent}
   * @throws NullPointerException if this {@code DropTarget} is active and {@code dtde} is {@code
   * null}
   * @see #isActive
   */
  @Override
  public synchronized void dragEnter(DropTargetDragEvent dtde) {
    isDraggingInside = true;

    if (!active) {
      return;
    }

    if (dtListener != null) {
      dtListener.dragEnter(dtde);
    } else {
      dtde.getDropTargetContext().setTargetActions(DnDConstants.ACTION_NONE);
    }

    initializeAutoscrolling(dtde.getLocation());
  }

  /**
   * Calls {@code dragOver} on the registered {@code DropTargetListener} and passes it the specified
   * {@code DropTargetDragEvent}. Has no effect if this {@code DropTarget} is not active.
   *
   * @param dtde the {@code DropTargetDragEvent}
   * @throws NullPointerException if this {@code DropTarget} is active and {@code dtde} is {@code
   * null}
   * @see #isActive
   */
  @Override
  public synchronized void dragOver(DropTargetDragEvent dtde) {
    if (!active) {
      return;
    }

    if (dtListener != null) {
      dtListener.dragOver(dtde);
    }

    updateAutoscroll(dtde.getLocation());
  }

  /**
   * Calls {@code dropActionChanged} on the registered {@code DropTargetListener} and passes it the
   * specified {@code DropTargetDragEvent}. Has no effect if this {@code DropTarget} is not active.
   *
   * @param dtde the {@code DropTargetDragEvent}
   * @throws NullPointerException if this {@code DropTarget} is active and {@code dtde} is {@code
   * null}
   * @see #isActive
   */
  @Override
  public synchronized void dropActionChanged(DropTargetDragEvent dtde) {
    if (!active) {
      return;
    }

    if (dtListener != null) {
      dtListener.dropActionChanged(dtde);
    }

    updateAutoscroll(dtde.getLocation());
  }

  /**
   * Calls {@code dragExit} on the registered {@code DropTargetListener} and passes it the specified
   * {@code DropTargetEvent}. Has no effect if this {@code DropTarget} is not active.
   * <p>
   * This method itself does not throw any exception for null parameter but for exceptions thrown by
   * the respective method of the listener.
   *
   * @param dte the {@code DropTargetEvent}
   * @see #isActive
   */
  @Override
  public synchronized void dragExit(DropTargetEvent dte) {
    isDraggingInside = false;

    if (!active) {
      return;
    }

    if (dtListener != null) {
      dtListener.dragExit(dte);
    }

    clearAutoscroll();
  }

  /**
   * Calls {@code drop} on the registered {@code DropTargetListener} and passes it the specified
   * {@code DropTargetDropEvent} if this {@code DropTarget} is active.
   *
   * @param dtde the {@code DropTargetDropEvent}
   * @throws NullPointerException if {@code dtde} is null and at least one of the following is true:
   * this {@code DropTarget} is not active, or there is no a {@code DropTargetListener} registered.
   * @see #isActive
   */
  @Override
  public synchronized void drop(DropTargetDropEvent dtde) {
    isDraggingInside = false;

    clearAutoscroll();

    if (dtListener != null && active) {
      dtListener.drop(dtde);
    } else { // we should'nt get here ...
      dtde.rejectDrop();
    }
  }

  /**
   * Gets the {@code FlavorMap} associated with this {@code DropTarget}. If no {@code FlavorMap} has
   * been set for this {@code DropTarget}, it is associated with the default {@code FlavorMap}.
   * <p>
   *
   * @return the FlavorMap for this DropTarget
   */

  public FlavorMap getFlavorMap() {
    return flavorMap;
  }

  /**
   * Sets the {@code FlavorMap} associated with this {@code DropTarget}.
   * <p>
   *
   * @param fm the new {@code FlavorMap}, or null to associate the default FlavorMap with this
   * DropTarget.
   */

  public void setFlavorMap(FlavorMap fm) {
    flavorMap = fm == null ? SystemFlavorMap.getDefaultFlavorMap() : fm;
  }

  /**
   * Notify the DropTarget that it has been associated with a Component
   * <p>
   * ********************************************************************* This method is usually
   * called from java.awt.Component.addNotify() of the Component associated with this DropTarget to
   * notify the DropTarget that a ComponentPeer has been associated with that Component.
   * <p>
   * Calling this method, other than to notify this DropTarget of the association of the
   * ComponentPeer with the Component may result in a malfunction of the DnD system.
   * *********************************************************************
   * <p>
   *
   * @param peer The Peer of the Component we are associated with!
   */

  public void addNotify(ComponentPeer peer) {
    if (peer == componentPeer) {
      return;
    }

    componentPeer = peer;

    for (Component c = component; c != null && peer instanceof LightweightPeer; c = c.getParent()) {
      peer = c.getPeer();
    }

    if (peer instanceof DropTargetPeer) {
      nativePeer = peer;
      ((DropTargetPeer) peer).addDropTarget(this);
    } else {
      nativePeer = null;
    }
  }

  /**
   * Notify the DropTarget that it has been disassociated from a Component
   * <p>
   * ********************************************************************* This method is usually
   * called from java.awt.Component.removeNotify() of the Component associated with this DropTarget
   * to notify the DropTarget that a ComponentPeer has been disassociated with that Component.
   * <p>
   * Calling this method, other than to notify this DropTarget of the disassociation of the
   * ComponentPeer from the Component may result in a malfunction of the DnD system.
   * *********************************************************************
   * <p>
   *
   * @param peer The Peer of the Component we are being disassociated from!
   */

  public void removeNotify(ComponentPeer peer) {
    if (nativePeer != null) {
      ((DropTargetPeer) nativePeer).removeDropTarget(this);
    }

    componentPeer = nativePeer = null;

    synchronized (this) {
      if (isDraggingInside) {
        dragExit(new DropTargetEvent(getDropTargetContext()));
      }
    }
  }

  /**
   * Gets the {@code DropTargetContext} associated with this {@code DropTarget}.
   * <p>
   *
   * @return the {@code DropTargetContext} associated with this {@code DropTarget}.
   */

  public DropTargetContext getDropTargetContext() {
    return dropTargetContext;
  }

  /**
   * Creates the DropTargetContext associated with this DropTarget. Subclasses may override this
   * method to instantiate their own DropTargetContext subclass.
   * <p>
   * This call is typically *only* called by the platform's DropTargetContextPeer as a drag
   * operation encounters this DropTarget. Accessing the Context while no Drag is current has
   * undefined results.
   */

  protected DropTargetContext createDropTargetContext() {
    return new DropTargetContext(this);
  }

  /**
   * Serializes this {@code DropTarget}. Performs default serialization, and then writes out this
   * object's {@code DropTargetListener} if and only if it can be serialized. If not, {@code null}
   * is written instead.
   *
   * @serialData The default serializable fields, in alphabetical order, followed by either a {@code
   * DropTargetListener} instance, or {@code null}.
   * @since 1.4
   */
  private void writeObject(ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();

    s.writeObject(SerializationTester.test(dtListener) ? dtListener : null);
  }

  /**
   * Deserializes this {@code DropTarget}. This method first performs default deserialization for
   * all non-{@code transient} fields. An attempt is then made to deserialize this object's {@code
   * DropTargetListener} as well. This is first attempted by deserializing the field {@code
   * dtListener}, because, in releases prior to 1.4, a non-{@code transient} field of this name
   * stored the {@code DropTargetListener}. If this fails, the next object in the stream is used
   * instead.
   *
   * @since 1.4
   */
  private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
    GetField f = s.readFields();

    try {
      dropTargetContext = (DropTargetContext) f.get("dropTargetContext", null);
    } catch (IllegalArgumentException e) {
      // Pre-1.4 support. 'dropTargetContext' was previously transient
    }
    if (dropTargetContext == null) {
      dropTargetContext = createDropTargetContext();
    }

    component = (Component) f.get("component", null);
    actions = f.get("actions", DnDConstants.ACTION_COPY_OR_MOVE);
    active = f.get("active", true);

    // Pre-1.4 support. 'dtListener' was previously non-transient
    try {
      dtListener = (DropTargetListener) f.get("dtListener", null);
    } catch (IllegalArgumentException e) {
      // 1.4-compatible byte stream. 'dtListener' was written explicitly
      dtListener = (DropTargetListener) s.readObject();
    }
  }

  /**
   * create an embedded autoscroller
   * <p>
   *
   * @param cComponent the {@code Component}
   * @param cAutoscroll the {@code Autoscroll}; should be the same object as {@code cComponent}
   * @param p the {@code Point}
   */

  protected DropTargetAutoScroller createDropTargetAutoScroller(
      Component cComponent, Autoscroll cAutoscroll, Point p) {
    return new DropTargetAutoScroller(cComponent, cAutoscroll, p);
  }

    /*
     * the auto scrolling object
     */

  /**
   * initialize autoscrolling
   * <p>
   *
   * @param p the {@code Point}
   */

  @SuppressWarnings("InstanceofIncompatibleInterface")
  protected void initializeAutoscrolling(Point p) {
    if (component instanceof Autoscroll) {
      autoScroller = createDropTargetAutoScroller(component, (Autoscroll) component, p);
    }
  }

    /*
     * The delegate
     */

  /**
   * update autoscrolling with current cursor location
   * <p>
   *
   * @param dragCursorLocn the {@code Point}
   */

  protected void updateAutoscroll(Point dragCursorLocn) {
    if (autoScroller != null) {
      autoScroller.updateLocation(dragCursorLocn);
    }
  }

    /*
     * The FlavorMap
     */

  /**
   * clear autoscrolling
   */

  protected void clearAutoscroll() {
    if (autoScroller != null) {
      autoScroller.stop();
      autoScroller = null;
    }
  }

  /**
   * this protected nested class implements autoscrolling
   */

  protected static class DropTargetAutoScroller extends TimerTask implements ActionListener {

    private final Component component;
    private final Autoscroll autoScroll;
    private final Timer timer;
    /*
     * fields
     */
    private final Rectangle outer = new Rectangle();
    private final Rectangle inner = new Rectangle();
    private boolean timerIsRunning;
    private Point locn;
    private Point prev;
    private int hysteresis = 10;
    private Integer initial = SkinJobGlobals.autoscrollInitialDelayMs;
    private Integer interval = SkinJobGlobals.autoscrollRefreshIntervalMs;

    /**
     * construct a DropTargetAutoScroller
     * <p>
     *
     * @param cComponent the {@code Component}
     * @param cAutoscroll the {@code Autoscroll}; should be the same object as {@code cComponent}.
     * @param p the {@code Point}
     */

    protected DropTargetAutoScroller(Component cComponent, Autoscroll cAutoscroll, Point p) {

      component = cComponent;
      autoScroll = cAutoscroll;

      Toolkit t = Toolkit.getDefaultToolkit();

      try {
        initial = (Integer) t.getDesktopProperty("DnD.Autoscroll.initialDelay");
      } catch (Exception e) {
        // ignore
      }

      try {
        interval = (Integer) t.getDesktopProperty("DnD.Autoscroll.interval");
      } catch (Exception e) {
        // ignore
      }

      timer = new Timer();

      locn = p;
      prev = p;

      try {
        hysteresis = (Integer) t.getDesktopProperty("DnD.Autoscroll.cursorHysteresis");
      } catch (Exception e) {
        // ignore
      }

      synchronized (this) {
        timer.scheduleAtFixedRate(this, initial, interval);
        timerIsRunning = true;
      }
    }

    /**
     * update the geometry of the autoscroll region
     */

    private void updateRegion() {
      Insets i = autoScroll.getAutoscrollInsets();
      Dimension size = component.getSize();

      if (size.width != outer.width || size.height != outer.height) {
        outer.reshape(0, 0, size.width, size.height);
      }

      if (inner.x != i.left || inner.y != i.top) {
        inner.setLocation(i.left, i.top);
      }

      int newWidth = size.width - (i.left + i.right);
      int newHeight = size.height - (i.top + i.bottom);

      if (newWidth != inner.width || newHeight != inner.height) {
        inner.setSize(newWidth, newHeight);
      }
    }

    /**
     * cause autoscroll to occur
     * <p>
     *
     * @param newLocn the {@code Point}
     */

    protected synchronized void updateLocation(Point newLocn) {
      prev = locn;
      locn = newLocn;

      if (Math.abs(locn.x - prev.x) > hysteresis || Math.abs(locn.y - prev.y) > hysteresis) {
        if (timerIsRunning) {
          timer.cancel();
          timerIsRunning = false;
        }
      } else {
        if (!timerIsRunning) {
          timer.scheduleAtFixedRate(this, initial, interval);
          timerIsRunning = true;
        }
      }
    }

    /**
     * cause autoscrolling to stop
     */

    protected void stop() {
      timer.cancel();
    }

    /**
     * cause autoscroll to occur
     * <p>
     *
     * @param ignored the {@code ActionEvent} (parameter present for backward-compatibility with
     * OpenJDK AWT)
     */

    @Override
    public synchronized void actionPerformed(ActionEvent ignored) {
      updateRegion();

      if (outer.contains(locn) && !inner.contains(locn)) {
        autoScroll.autoscroll(locn);
      }
    }

    @Override
    public void run() {
      actionPerformed(null);
    }
  }
}

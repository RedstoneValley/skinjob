/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.TextEvent;
import java.awt.event.WindowEvent;
import java.awt.peer.LightweightPeer;
import java.lang.reflect.Field;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.EventObject;

import sun.awt.AWTAccessor;
import sun.awt.AWTAccessor.AWTEventAccessor;

/**
 * The root event class for all AWT events. This class and its subclasses supercede the original
 * java.awt.Event class. Subclasses of this root AWTEvent class defined outside of the
 * java.awt.event package should define event ID values greater than the value defined by
 * RESERVED_ID_MAX.
 * <p>
 * The event masks defined in this class are needed by Component subclasses which are using
 * Component.enableEvents() to select for event types not selected by registered listeners. If a
 * listener is registered on a component, the appropriate event mask is already set internally by
 * the component.
 * <p>
 * The masks are also used to specify to which types of events an AWTEventListener should listen.
 * The masks are bitwise-ORed together and passed to Toolkit.addAWTEventListener.
 *
 * @author Carl Quinn
 * @author Amy Fowler
 * @see Component#enableEvents
 * @see Toolkit#addAWTEventListener
 * @see ActionEvent
 * @see AdjustmentEvent
 * @see ComponentEvent
 * @see java.awt.event.ContainerEvent
 * @see FocusEvent
 * @see InputMethodEvent
 * @see java.awt.event.InvocationEvent
 * @see ItemEvent
 * @see java.awt.event.HierarchyEvent
 * @see KeyEvent
 * @see MouseEvent
 * @see java.awt.event.MouseWheelEvent
 * @see java.awt.event.PaintEvent
 * @see TextEvent
 * @see WindowEvent
 * @since 1.1
 */
public abstract class AWTEvent extends EventObject {
  /**
   * The event mask for selecting component events.
   */
  public static final long COMPONENT_EVENT_MASK = 0x01;
  /**
   * The event mask for selecting container events.
   */
  public static final long CONTAINER_EVENT_MASK = 0x02;
  /**
   * The event mask for selecting focus events.
   */
  public static final long FOCUS_EVENT_MASK = 0x04;
  /**
   * The event mask for selecting key events.
   */
  public static final long KEY_EVENT_MASK = 0x08;
  /**
   * The event mask for selecting mouse events.
   */
  public static final long MOUSE_EVENT_MASK = 0x10;
  /**
   * The event mask for selecting mouse motion events.
   */
  public static final long MOUSE_MOTION_EVENT_MASK = 0x20;
  /**
   * The event mask for selecting window events.
   */
  public static final long WINDOW_EVENT_MASK = 0x40;
  /**
   * The event mask for selecting action events.
   */
  public static final long ACTION_EVENT_MASK = 0x80;
  /**
   * The event mask for selecting adjustment events.
   */
  public static final long ADJUSTMENT_EVENT_MASK = 0x100;
  /**
   * The event mask for selecting item events.
   */
  public static final long ITEM_EVENT_MASK = 0x200;
  /**
   * The event mask for selecting text events.
   */
  public static final long TEXT_EVENT_MASK = 0x400;
  /**
   * The event mask for selecting input method events.
   */
  public static final long INPUT_METHOD_EVENT_MASK = 0x800;
  /**
   * The event mask for selecting paint events.
   */
  public static final long PAINT_EVENT_MASK = 0x2000;
  /**
   * The event mask for selecting invocation events.
   */
  public static final long INVOCATION_EVENT_MASK = 0x4000;
  /**
   * The event mask for selecting hierarchy events.
   */
  public static final long HIERARCHY_EVENT_MASK = 0x8000;
  /**
   * The event mask for selecting hierarchy bounds events.
   */
  public static final long HIERARCHY_BOUNDS_EVENT_MASK = 0x10000;
  /**
   * The event mask for selecting mouse wheel events.
   *
   * @since 1.4
   */
  public static final long MOUSE_WHEEL_EVENT_MASK = 0x20000;
  /**
   * The event mask for selecting window state events.
   *
   * @since 1.4
   */
  public static final long WINDOW_STATE_EVENT_MASK = 0x40000;
  /**
   * The event mask for selecting window focus events.
   *
   * @since 1.4
   */
  public static final long WINDOW_FOCUS_EVENT_MASK = 0x80000;
  /**
   * The maximum value for reserved AWT event IDs. Programs defining their own event IDs should use
   * IDs greater than this value.
   */
  public static final int RESERVED_ID_MAX = 1999;
  /**
   * The pseudo event mask for enabling input methods. We're using one bit in the eventMask so we
   * don't need a separate field inputMethodsEnabled.
   */
  static final long INPUT_METHODS_ENABLED_MASK = 0x1000;
  private static final String TAG = "java.awt.AWTEvent";
  /*
   * JDK 1.1 serialVersionUID
   */
  private static final long serialVersionUID = -1825314779160409405L;
  // security stuff
  private static Field inputEvent_CanAccessSystemClipboard_Field;

  static {
    AWTAccessor.setAWTEventAccessor(new AWTEventAccessor() {
      @Override
      public void setPosted(AWTEvent ev) {
        ev.isPosted = true;
      }

      @Override
      public void setSystemGenerated(AWTEvent ev) {
        ev.isSystemGenerated = true;
      }

      @Override
      public boolean isSystemGenerated(AWTEvent ev) {
        return ev.isSystemGenerated;
      }
    });
  }

  /**
   * The event's id.
   *
   * @serial
   * @see #getID()
   * @see #AWTEvent
   */
  protected final int id;
  /*
   * The event's AccessControlContext.
   */
  private final transient AccessControlContext acc = AccessController.getContext();
  /**
   * Controls whether or not the event is sent back down to the peer once the source has processed
   * it - false means it's sent to the peer; true means it's not. Semantic events always have a
   * 'true' value since they were generated by the peer in response to a low-level event.
   *
   * @serial
   * @see #consume
   * @see #isConsumed
   */
  protected boolean consumed;
  transient boolean focusManagerIsDispatching;
  transient boolean isPosted;
  /**
   * WARNING: there are more mask defined privately.  See SunToolkit.GRAB_EVENT_MASK.
   */
  byte[] bdata;
  /**
   * Indicates whether this AWTEvent was generated by the system as opposed to by user code.
   */
  transient boolean isSystemGenerated;

  /**
   * Constructs an AWTEvent object from the parameters of a 1.0-style event.
   *
   * @param event the old-style event
   */
  public AWTEvent(Event event) {
    this(event.target, event.id);
  }

  /**
   * Constructs an AWTEvent object with the specified source object and type.
   *
   * @param source the object where the event originated
   * @param id the event type
   */
  public AWTEvent(Object source, int id) {
    super(source);
    this.id = id;
    switch (id) {
      case ActionEvent.ACTION_PERFORMED:
      case ItemEvent.ITEM_STATE_CHANGED:
      case AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED:
      case TextEvent.TEXT_VALUE_CHANGED:
        consumed = true;
        break;
      default:
    }
  }

  private static synchronized Field get_InputEvent_CanAccessSystemClipboard() {
    if (inputEvent_CanAccessSystemClipboard_Field == null) {
      inputEvent_CanAccessSystemClipboard_Field
          = AccessController.doPrivileged(new PrivilegedAction<Field>() {
        @Override
        public Field run() {
          Field field;
          try {
            field = InputEvent.class.
                getDeclaredField("canAccessSystemClipboard");
            field.setAccessible(true);
            return field;
          } catch (SecurityException e) {
            Log.d(
                TAG,
                "AWTEvent.get_InputEvent_CanAccessSystemClipboard() got SecurityException ",
                e);
          } catch (NoSuchFieldException e) {
            Log.d(
                TAG,
                "AWTEvent.get_InputEvent_CanAccessSystemClipboard() got NoSuchFieldException ",
                e);
          }
          return null;
        }
      });
    }

    return inputEvent_CanAccessSystemClipboard_Field;
  }

  /*
   * Returns the acc this event was constructed with.
   */
  final AccessControlContext getAccessControlContext() {
    if (acc == null) {
      throw new SecurityException("AWTEvent is missing AccessControlContext");
    }
    return acc;
  }

  /**
   * Retargets an event to a new source. This method is typically used to retarget an event to a
   * lightweight child Component of the original heavyweight source.
   * <p>
   * This method is intended to be used only by event targeting subsystems, such as client-defined
   * KeyboardFocusManagers. It is not for general client use.
   *
   * @param newSource the new Object to which the event should be dispatched
   * @since 1.4
   */
  public void setSource(Object newSource) {
    if (source == newSource) {
      return;
    }

    Component comp = null;
    if (newSource instanceof Component) {
      comp = (Component) newSource;
      while (comp != null && comp.peer != null &&
          comp.peer instanceof LightweightPeer) {
        comp = comp.parent;
      }
    }

    source = newSource;
  }

  /**
   * Returns the event type.
   */
  public int getID() {
    return id;
  }

  /**
   * Returns a String representation of this object.
   */
  public String toString() {
    String srcName = null;
    if (source instanceof Component) {
      srcName = ((Component) source).getName();
    } else if (source instanceof MenuComponent) {
      srcName = ((MenuComponent) source).getName();
    }
    return getClass().getName() + "[" + paramString() + "] on " +
        (srcName != null ? srcName : source);
  }

  /**
   * Returns a string representing the state of this {@code Event}. This method is intended to be
   * used only for debugging purposes, and the content and format of the returned string may vary
   * between implementations. The returned string may be empty but may not be {@code null}.
   *
   * @return a string representation of this event
   */
  public String paramString() {
    return "";
  }

  /**
   * Consumes this event, if this event can be consumed. Only low-level, system events can be
   * consumed
   */
  protected void consume() {
    switch (id) {
      case KeyEvent.KEY_PRESSED:
      case KeyEvent.KEY_RELEASED:
      case MouseEvent.MOUSE_PRESSED:
      case MouseEvent.MOUSE_RELEASED:
      case MouseEvent.MOUSE_MOVED:
      case MouseEvent.MOUSE_DRAGGED:
      case MouseEvent.MOUSE_ENTERED:
      case MouseEvent.MOUSE_EXITED:
      case MouseEvent.MOUSE_WHEEL:
      case InputMethodEvent.INPUT_METHOD_TEXT_CHANGED:
      case InputMethodEvent.CARET_POSITION_CHANGED:
        consumed = true;
        break;
      default:
        // event type cannot be consumed
    }
  }

  /**
   * Returns whether this event has been consumed.
   */
  protected boolean isConsumed() {
    return consumed;
  }

  /**
   * Converts a new event to an old one (used for compatibility). If the new event cannot be
   * converted (because no old equivalent exists) then this returns null.
   * <p>
   * Note: this method is here instead of in each individual new event class in java.awt.event
   * because we don't want to make it public and it needs to be called from java.awt.
   */
  Event convertToOld() {
    Object src = getSource();
    int newid = id;

    switch (id) {
      case KeyEvent.KEY_PRESSED:
      case KeyEvent.KEY_RELEASED:
        KeyEvent ke = (KeyEvent) this;
        if (ke.isActionKey()) {
          newid = id == KeyEvent.KEY_PRESSED ? Event.KEY_ACTION : Event.KEY_ACTION_RELEASE;
        }
        int keyCode = ke.getKeyCode();
        if (keyCode == KeyEvent.VK_SHIFT ||
            keyCode == KeyEvent.VK_CONTROL ||
            keyCode == KeyEvent.VK_ALT) {
          return null;  // suppress modifier keys in old event model.
        }
        // no mask for button1 existed in old Event - strip it out
        return new Event(
            src,
            ke.getWhen(),
            newid,
            0,
            0,
            Event.getOldEventKey(ke),
            ke.getModifiers() & ~InputEvent.BUTTON1_MASK);

      case MouseEvent.MOUSE_PRESSED:
      case MouseEvent.MOUSE_RELEASED:
      case MouseEvent.MOUSE_MOVED:
      case MouseEvent.MOUSE_DRAGGED:
      case MouseEvent.MOUSE_ENTERED:
      case MouseEvent.MOUSE_EXITED:
        MouseEvent me = (MouseEvent) this;
        // no mask for button1 existed in old Event - strip it out
        Event olde = new Event(
            src,
            me.getWhen(),
            newid,
            me.getX(),
            me.getY(),
            0,
            me.getModifiers() & ~InputEvent.BUTTON1_MASK);
        olde.clickCount = me.getClickCount();
        return olde;

      case FocusEvent.FOCUS_GAINED:
        return new Event(src, Event.GOT_FOCUS, null);

      case FocusEvent.FOCUS_LOST:
        return new Event(src, Event.LOST_FOCUS, null);

      case WindowEvent.WINDOW_CLOSING:
      case WindowEvent.WINDOW_ICONIFIED:
      case WindowEvent.WINDOW_DEICONIFIED:
        return new Event(src, newid, null);

      case ComponentEvent.COMPONENT_MOVED:
        if (src instanceof Frame || src instanceof Dialog) {
          Point p = ((Component) src).getLocation();
          return new Event(src, 0, Event.WINDOW_MOVED, p.x, p.y, 0, 0);
        }
        break;

      case ActionEvent.ACTION_PERFORMED:
        ActionEvent ae = (ActionEvent) this;
        String cmd;
        if (src instanceof Button) {
          cmd = ((Button) src).getLabel();
        } else if (src instanceof MenuItem) {
          cmd = ((MenuItem) src).getLabel();
        } else {
          cmd = ae.getActionCommand();
        }
        return new Event(src, 0, newid, 0, 0, 0, ae.getModifiers(), cmd);

      case ItemEvent.ITEM_STATE_CHANGED:
        ItemEvent ie = (ItemEvent) this;
        Object arg;
        if (src instanceof List) {
          newid = ie.getStateChange() == ItemEvent.SELECTED ? Event.LIST_SELECT
              : Event.LIST_DESELECT;
          arg = ie.getItem();
        } else {
          newid = Event.ACTION_EVENT;
          arg = src instanceof Choice ? ie.getItem()
              : Boolean.valueOf(ie.getStateChange() == ItemEvent.SELECTED);
        }
        return new Event(src, newid, arg);

      case AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED:
        AdjustmentEvent aje = (AdjustmentEvent) this;
        switch (aje.getAdjustmentType()) {
          case AdjustmentEvent.UNIT_INCREMENT:
            newid = Event.SCROLL_LINE_DOWN;
            break;
          case AdjustmentEvent.UNIT_DECREMENT:
            newid = Event.SCROLL_LINE_UP;
            break;
          case AdjustmentEvent.BLOCK_INCREMENT:
            newid = Event.SCROLL_PAGE_DOWN;
            break;
          case AdjustmentEvent.BLOCK_DECREMENT:
            newid = Event.SCROLL_PAGE_UP;
            break;
          case AdjustmentEvent.TRACK:
            newid = aje.getValueIsAdjusting() ? Event.SCROLL_ABSOLUTE : Event.SCROLL_END;
            break;
          default:
            return null;
        }
        return new Event(src, newid, aje.getValue());

      default:
    }
    return null;
  }

  /**
   * Copies all private data from this event into that. Space is allocated for the copied data that
   * will be freed when the that is finalized. Upon completion, this event is not changed.
   */
  void copyPrivateDataInto(AWTEvent that) {
    that.bdata = bdata;
    // Copy canAccessSystemClipboard value from this into that.
    if (this instanceof InputEvent && that instanceof InputEvent) {
      Field field = get_InputEvent_CanAccessSystemClipboard();
      if (field != null) {
        try {
          boolean b = field.getBoolean(this);
          field.setBoolean(that, b);
        } catch (IllegalAccessException e) {
          Log.d(TAG, "AWTEvent.copyPrivateDataInto() got IllegalAccessException ", e);
        }
      }
    }
    that.isSystemGenerated = isSystemGenerated;
  }

  void dispatched() {
    if (this instanceof InputEvent) {
      Field field = get_InputEvent_CanAccessSystemClipboard();
      if (field != null) {
        try {
          field.setBoolean(this, false);
        } catch (IllegalAccessException e) {
          Log.d(TAG, "AWTEvent.dispatched() got IllegalAccessException ", e);
        }
      }
    }
  }
} // class AWTEvent

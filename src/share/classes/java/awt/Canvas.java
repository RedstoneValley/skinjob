/*
 * Copyright (c) 1995, 2010, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.image.BufferStrategy;
import java.awt.peer.CanvasPeer;

/**
 * A {@code Canvas} component represents a blank rectangular
 * area of the screen onto which the application can draw or from
 * which the application can trap input events from the user.
 * <p>
 * An application must subclass the {@code Canvas} class in
 * order to get useful functionality such as creating a custom
 * component. The {@code paint} method must be overridden
 * in order to perform custom graphics on the canvas.
 *
 * @author Sami Shaio
 * @since JDK1.0
 */
public class Canvas extends Component {

  private static final String base = "canvas";
  /*
   * JDK 1.1 serialVersionUID
   */
  private static final long serialVersionUID = -2284879212465893870L;
  private static int nameCounter;
  protected final android.graphics.Canvas androidCanvas;

  /**
   * Constructs a new Canvas.
   */
  public Canvas() {
    // android.graphics.Canvas doesn't extend View
    super(SkinJobNullWidgetSupplier.getInstance());
    androidCanvas = new android.graphics.Canvas();
    peer = new SkinJobCanvasPeer(this);
  }

  /**
   * Constructs a new Canvas given a GraphicsConfiguration object.
   *
   * @param config a reference to a GraphicsConfiguration object.
   * @see GraphicsConfiguration
   */
  public Canvas(GraphicsConfiguration config) {
    this();
    setGraphicsConfiguration(config);
  }

  /**
   * Construct a name for this component.  Called by getName() when the
   * name is null.
   */
  @Override
  String constructComponentName() {
    synchronized (Canvas.class) {
      String result = base + nameCounter;
      nameCounter++;
      return result;
    }
  }

  @Override
  void setGraphicsConfiguration(GraphicsConfiguration gc) {
    synchronized (getTreeLock()) {
      CanvasPeer peer = (CanvasPeer) getPeer();
      if (peer != null) {
        gc = peer.getAppropriateGraphicsConfiguration(gc);
      }
      super.setGraphicsConfiguration(gc);
    }
  }

  /**
   * Paints this canvas.
   * <p>
   * Most applications that subclass {@code Canvas} should
   * override this method in order to perform some useful operation
   * (typically, custom painting of the canvas).
   * The default operation is simply to clear the canvas.
   * Applications that override this method need not call
   * super.paint(g).
   *
   * @param g the specified Graphics context
   * @see #update(Graphics)
   */
  @Override
  public void paint(Graphics g) {
    g.clearRect(0, 0, width, height);
  }

  /**
   * Updates this canvas.
   * <p>
   * This method is called in response to a call to {@code repaint}.
   * The canvas is first cleared by filling it with the background
   * color, and then completely redrawn by calling this canvas's
   * {@code paint} method.
   * Note: applications that override this method should either call
   * super.update(g) or incorporate the functionality described
   * above into their own code.
   *
   * @param g the specified Graphics context
   * @see #paint(Graphics)
   */
  @Override
  public void update(Graphics g) {
    g.clearRect(0, 0, width, height);
    paint(g);
  }

  /**
   * Creates a new strategy for multi-buffering on this component.
   * Multi-buffering is useful for rendering performance.  This method
   * attempts to create the best strategy available with the number of
   * buffers supplied.  It will always create a {@code BufferStrategy}
   * with that number of buffers.
   * A page-flipping strategy is attempted first, then a blitting strategy
   * using accelerated buffers.  Finally, an unaccelerated blitting
   * strategy is used.
   * <p>
   * Each time this method is called,
   * the existing buffer strategy for this component is discarded.
   *
   * @param numBuffers number of buffers to create, including the front buffer
   * @throws IllegalArgumentException if numBuffers is less than 1.
   * @throws IllegalStateException    if the component is not displayable
   * @see #isDisplayable
   * @see #getBufferStrategy
   * @since 1.4
   */
  @Override
  public void createBufferStrategy(int numBuffers) {
    super.createBufferStrategy(numBuffers);
  }

  /**
   * Creates a new strategy for multi-buffering on this component with the
   * required buffer capabilities.  This is useful, for example, if only
   * accelerated memory or page flipping is desired (as specified by the
   * buffer capabilities).
   * <p>
   * Each time this method
   * is called, the existing buffer strategy for this component is discarded.
   *
   * @param numBuffers number of buffers to create
   * @param caps       the required capabilities for creating the buffer strategy;
   *                   cannot be {@code null}
   * @throws AWTException             if the capabilities supplied could not be
   *                                  supported or met; this may happen, for example, if there
   *                                  is not enough
   *                                  accelerated memory currently available, or if page
   *                                  flipping is specified
   *                                  but not possible.
   * @throws IllegalArgumentException if numBuffers is less than 1, or if
   *                                  caps is {@code null}
   * @see #getBufferStrategy
   * @since 1.4
   */
  @Override
  public void createBufferStrategy(
      int numBuffers, BufferCapabilities caps) throws AWTException {
    super.createBufferStrategy(numBuffers, caps);
  }

  /**
   * Returns the {@code BufferStrategy} used by this component.  This
   * method will return null if a {@code BufferStrategy} has not yet
   * been created or has been disposed.
   *
   * @return the buffer strategy used by this component
   * @see #createBufferStrategy
   * @since 1.4
   */
  @Override
  public BufferStrategy getBufferStrategy() {
    return super.getBufferStrategy();
  }

  @Override
  boolean postsOldMouseEvents() {
    return true;
  }

  /**
   * Creates the peer of the canvas.  This peer allows you to change the
   * user interface of the canvas without changing its functionality.
   *
   * @see Toolkit#createCanvas(Canvas)
   * @see Component#getToolkit()
   */
  @Override
  public void addNotify() {
    synchronized (getTreeLock()) {
      if (peer == null) {
        peer = getToolkit().createCanvas(this);
      }
      super.addNotify();
    }
  }
}

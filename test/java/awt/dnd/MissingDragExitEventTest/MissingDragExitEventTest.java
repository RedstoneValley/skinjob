/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
  @test
 * @bug 8027913
 * @library ../../regtesthelpers
 * @build Util
 * @compile MissingDragExitEventTest.java
 * @run main/othervm MissingDragExitEventTest
 * @author Sergey Bylokhov
 */

import java.awt.Color;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import sun.awt.SunToolkit;

public final class MissingDragExitEventTest {

    private static volatile JFrame frame;
    static boolean FAILED;
    static boolean MOUSE_ENTERED_DT;
    static boolean MOUSE_ENTERED;
    static boolean MOUSE_EXIT_TD;
    static boolean MOUSE_EXIT;
    private static final int SIZE = 300;

    private MissingDragExitEventTest() {
    }

    static void initAndShowUI() {
        frame = new JFrame("Test frame");

        frame.setSize(SIZE, SIZE);
        frame.setLocationRelativeTo(null);
        JTextArea jta = new JTextArea();
        jta.setBackground(Color.RED);
        frame.add(jta);
        jta.setText("1234567890");
        jta.setFont(jta.getFont().deriveFont(150f));
        jta.setDragEnabled(true);
        jta.selectAll();
        jta.setDropTarget(new DropTarget(jta, DnDConstants.ACTION_COPY,
                                         new TestdropTargetListener()));
        jta.addMouseListener(new TestMouseAdapter());
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        try {
            Robot r = new Robot();
            r.setAutoDelay(50);
            r.mouseMove(100, 100);
            Util.waitForIdle(r);

            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    initAndShowUI();
                }
            });

            Point inside = new Point(frame.getLocationOnScreen());
            inside.translate(20, SIZE / 2);
            Point outer = new Point(inside);
            outer.translate(-40, 0);
            r.mouseMove(inside.x, inside.y);
            r.mousePress(InputEvent.BUTTON1_MASK);
            try {
                for (int i = 0; i < 3; ++i) {
                    Util.mouseMove(r, inside, outer);
                    Util.mouseMove(r, outer, inside);
                }
            } finally {
                r.mouseRelease(InputEvent.BUTTON1_MASK);
            }
            sleep();

            if (FAILED || !MOUSE_ENTERED || !MOUSE_ENTERED_DT || !MOUSE_EXIT
                    || !MOUSE_EXIT_TD) {
                throw new RuntimeException("Failed");
            }
        } finally {
            if (frame != null) {
                frame.dispose();
            }
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignored) {
        }
        ((SunToolkit) Toolkit.getDefaultToolkit()).realSync();
    }

    static class TestdropTargetListener extends DropTargetAdapter {

        private volatile boolean inside;

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            if (inside) {
                FAILED = true;
                Thread.dumpStack();
            }
            inside = true;
            MOUSE_ENTERED_DT = true;
            try {
                Thread.sleep(10000); // we should have time to leave a component
            } catch (InterruptedException ignored) {
            }
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            if (!inside) {
                FAILED = true;
                Thread.dumpStack();
            }
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            if (!inside) {
                FAILED = true;
                Thread.dumpStack();
            }
            inside = false;
            MOUSE_EXIT_TD = true;
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            if (!inside) {
                FAILED = true;
                Thread.dumpStack();
            }
            inside = false;
        }
    }

    static class TestMouseAdapter extends MouseAdapter {

        private volatile boolean inside;

        @Override
        public void mouseEntered(MouseEvent e) {
            if (inside) {
                FAILED = true;
                Thread.dumpStack();
            }
            inside = true;
            MOUSE_ENTERED = true;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!inside) {
                FAILED = true;
                Thread.dumpStack();
            }
            inside = false;
            MOUSE_EXIT = true;
        }
    }
}

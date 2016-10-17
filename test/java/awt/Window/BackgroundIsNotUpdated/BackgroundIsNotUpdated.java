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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;

import sun.awt.SunToolkit;

/**
 * @test
 * @bug 8001472
 * @summary Background of the window should not depend from the paint()/update()
 * @author Sergey Bylokhov
 */
@SuppressWarnings("MagicNumber")
public final class BackgroundIsNotUpdated extends Window {

    private static final long serialVersionUID = -602930317824748962L;

    public BackgroundIsNotUpdated(Frame owner) {
        super(owner);
    }

    @Override
    public void paint(Graphics ignored) {
        // Intentionally left blank
    }

    @Override
    public void update(Graphics ignored) {
        // Intentionally left blank
    }

    public static void main(String[] args) throws AWTException {
        Window window = new BackgroundIsNotUpdated(null);
        window.setSize(300, 300);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        sleep();
        window.setBackground(Color.GREEN);
        sleep();
        Robot robot = new Robot();
        robot.setAutoDelay(200);
        Point point = window.getLocationOnScreen();
        Color color = robot.getPixelColor(point.x + window.getWidth() / 2,
                                          point.y + window.getHeight() / 2);
        window.dispose();
        if (!color.equals(Color.GREEN)) {
            throw new RuntimeException(
                    "Expected: " + Color.GREEN + " , Actual: " + color);
        }
    }

    private static void sleep() {
        try {
            ((SunToolkit) Toolkit.getDefaultToolkit()).realSync();
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
    }
}

/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.BorderLayout;
import sun.awt.OSInfo;
import sun.awt.OSInfo.OSType;
import sun.awt.SunToolkit;

import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

/**
 * @test
 * @bug 6263470
 * @summary Tries to change font of MenuBar. Test passes if the font has changed
 * fails otherwise.
 * @author Vyacheslav.Baranov: area=menu
 * @run main MenuBarSetFont
 */
public final class MenuBarSetFont {

    private static final Frame frame = new Frame();
    private static final MenuBar mb = new MenuBar();
    static volatile boolean clicked;

    private MenuBarSetFont() {
    }

    private static final class Listener implements ActionListener {
        Listener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //Click on this button is performed
            //_only_ if font of MenuBar is not changed on time
            clicked = true;
        }
    }

    private static void addMenu() {
        mb.add(new Menu("w"));
        frame.validate();
    }

    public static void main(String[] args) throws Exception {

        if (OSInfo.getOSType() == OSType.MACOSX) {
            System.err.println("This test is not for OS X. Menu.setFont() is not supported on OS X.");
            return;
        }

        //Components initialization.
        frame.setMenuBar(mb);
        mb.setFont(new Font("Helvetica", Font.ITALIC, 5));

        Button button = new Button("Click Me");
        button.addActionListener(new Listener());
        frame.setLayout(new CardLayout());
        frame.add(button, BorderLayout.BEFORE_FIRST_LINE);
        frame.setSize(400, 400);
        frame.setVisible(true);
        sleep();

        int fInsets = frame.getInsets().top;  //Frame insets without menu.
        addMenu();
        int fMenuInsets = frame.getInsets().top; //Frame insets with menu.
        int menuBarHeight = fMenuInsets - fInsets;
        // There is no way to change menubar height on windows. But on windows
        // we can try to split menubar in 2 rows.
        for (int i = 0; i < 100 && fMenuInsets == frame.getInsets().top; ++i) {
            // Fill whole menubar.
            addMenu();
        }

        mb.remove(0);
        frame.validate();
        sleep();

        // Test execution.
        // On XToolkit, menubar font should be changed to 60.
        // On WToolkit, menubar font should be changed to default and menubar
        // should be splitted in 2 rows.
        mb.setFont(new Font("Helvetica", Font.ITALIC, 60));
        sleep();

        Robot r = new Robot();
        r.setAutoDelay(200);
        Point pt = frame.getLocation();
        r.mouseMove(pt.x + frame.getWidth() / 2,
                    pt.y + fMenuInsets + menuBarHeight / 2);
        r.mousePress(InputEvent.BUTTON1_MASK);
        r.mouseRelease(InputEvent.BUTTON1_MASK);

        sleep();
        frame.dispose();

        if (clicked) {
            fail("Font was not changed");
        }
    }

    private static void sleep() {
        ((SunToolkit) Toolkit.getDefaultToolkit()).realSync();
        try {
            Thread.sleep(500L);
        } catch (InterruptedException ignored) {
        }
    }

    private static void fail(String message) {
        throw new RuntimeException(message);
    }
}

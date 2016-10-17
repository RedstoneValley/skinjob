/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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
  @bug 7171412
  @summary awt Choice doesn't fire ItemStateChange when selecting item after select() call
  @author Oleg Pekhovskiy: area=awt-choice
  @library ../../regtesthelpers
  @build Util
  @run main ItemStateChangeTest
*/

import sun.awt.OSInfo.OSType;

import java.awt.*;
import java.awt.event.*;
import sun.awt.OSInfo;

public class ItemStateChangeTest extends Frame {

    private static final long serialVersionUID = -4470770084004993791L;
    int events;

    public static void main(String[] args) {
        new ItemStateChangeTest();
    }

    public ItemStateChangeTest() {

        if (OSInfo.getOSType() != OSType.WINDOWS) {
            return;
        }

        try {

            Robot robot = new Robot();
            robot.setAutoDelay(20);
            Util.waitForIdle(robot);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

            Choice choice = new Choice();
            choice.add("A");
            choice.add("B");
            choice.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    ++events;
                }
            });

            add(choice);
            setSize(200, 150);
            setVisible(true);
            toFront();

            // choose B
            int y = chooseB(choice, robot, 16);

            // reset to A
            choice.select(0);
            robot.delay(20);
            Util.waitForIdle(robot);

            // choose B again
            chooseB(choice, robot, y);

            if (events == 2) {
                System.out.println("Test passed!");
            }
            else {
                throw new RuntimeException("Test failed!");
            }

        }
        catch (AWTException e) {
            throw new RuntimeException("Test failed!");
        }
    }

    final int chooseB(Choice choice, Robot robot, int y) {
        while (true) {
            // show drop-down list
            Util.clickOnComp(choice, robot);
            Util.waitForIdle(robot);
            Point pt = choice.getLocationOnScreen();
            Dimension size = choice.getSize();
            // try to click B item
            robot.mouseMove(pt.x + size.width / 2, pt.y + size.height + y);
            Util.waitForIdle(robot);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Util.waitForIdle(robot);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Util.waitForIdle(robot);
            if (choice.getSelectedIndex() == 1) {
                break;
            }
            // if it's not B, position cursor lower by 2 pixels and try again
            y += 2;
        }
        return y;
    }
}

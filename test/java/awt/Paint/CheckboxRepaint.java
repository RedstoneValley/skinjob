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

import java.awt.*;
import java.awt.peer.CheckboxPeer;

/**
 * @test
 * @bug 7090424
 * @author Sergey Bylokhov
 */
public final class CheckboxRepaint extends Checkbox {

    private static final long serialVersionUID = -1477131506496487340L;

    public static void main(String[] args) {
        for (int i = 0; i < 10; ++i) {
            Frame frame = new Frame();
            frame.setSize(300, 300);
            frame.setLocationRelativeTo(null);
            CheckboxRepaint checkbox = new CheckboxRepaint();
            frame.add(checkbox);
            frame.setVisible(true);
            sleep();
            checkbox.test();
            frame.dispose();
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (!EventQueue.isDispatchThread()) {
            throw new RuntimeException("Wrong thread");
        }
        test();
    }

    void test() {
        setState(getState());
        ((CheckboxPeer) getPeer()).setState(getState());

        setCheckboxGroup(getCheckboxGroup());
        ((CheckboxPeer) getPeer()).setCheckboxGroup(getCheckboxGroup());

        setLabel("");
        setLabel(null);
        setLabel(getLabel());
        ((CheckboxPeer) getPeer()).setLabel("");
        ((CheckboxPeer) getPeer()).setLabel(null);
        ((CheckboxPeer) getPeer()).setLabel(getLabel());

        setFont(null);
        setFont(getFont());
        getPeer().setFont(getFont());

        setBackground(null);
        setBackground(getBackground());
        getPeer().setBackground(getBackground());

        setForeground(null);
        setForeground(getForeground());
        getPeer().setForeground(getForeground());

        setEnabled(isEnabled());
        getPeer().setEnabled(isEnabled());
    }
}

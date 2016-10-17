/*
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.dnd.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

class TargetFileListFrame extends Frame implements DropTargetListener {

    private static final long serialVersionUID = 2834114904320906232L;
    private final List list = new List(URIListBetweenJVMsTest.VISIBLE_RAWS_IN_LIST);
    private final int expectationTransferredFilesNumber;
    private DataFlavor dropFlavor;

    TargetFileListFrame(Point location, int expectationTransferredFilesNumber) {
        try {
            dropFlavor = new DataFlavor("text/uri-list;class=java.io.Reader");
        } catch (Exception ex) {
        }
        this.expectationTransferredFilesNumber = expectationTransferredFilesNumber;
        initGUI(location);
        setDropTarget(new DropTarget(list, DnDConstants.ACTION_COPY,
                this));
    }

    private void initGUI(Point location) {
        setLocation(location);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        add(new Panel().add(list));
        pack();
        setVisible(true);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.getCurrentDataFlavorsAsList().contains(dropFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        if (dtde.getCurrentDataFlavorsAsList().contains(dropFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        if (dtde.getCurrentDataFlavorsAsList().contains(dropFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        }
    }

    @Override
    public void dragExit(DropTargetEvent dte) {}

    @Override
    public void drop(DropTargetDropEvent dtde) {
        list.removeAll();
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        java.util.List<File> fileList = extractListOfFiles(dtde);
        for (File file:fileList) {
            list.add(file.getName());
        }

        if (fileList.size() != expectationTransferredFilesNumber)
        {
            System.err.println("ERROR: Expected file number:"
                    + expectationTransferredFilesNumber
                    + "; Received file number: "
                    + fileList.size());
            dispose();
            System.exit(InterprocessMessages.WRONG_FILES_NUMBER_ON_TARGET);
        }

        dispose();

    }

    private java.util.List<File> extractListOfFiles(DropTargetDropEvent dtde) {
        BufferedReader reader = null;
        ArrayList<File> files = new ArrayList<>();
        try {
            reader = new BufferedReader((Reader)dtde.getTransferable().
                    getTransferData(dropFlavor));
            String line;
            while ((line = reader.readLine()) != null) {
                files.add(new File(new URI(line)));
            }
        } catch (UnsupportedFlavorException | URISyntaxException | IllegalArgumentException | IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return files;
    }

    Point getDropTargetPoint() {
       return new Point((int) list.getLocationOnScreen().getX()+ list.getWidth()/2,
                (int) list.getLocationOnScreen().getY()+ list.getHeight()/2);
    }
}

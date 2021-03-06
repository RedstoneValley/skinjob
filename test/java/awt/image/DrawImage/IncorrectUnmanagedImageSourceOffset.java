/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.VolatileImage;
import java.io.File;

import static java.awt.image.BufferedImage.*;

/**
 * @test
 * @bug 8029253
 * @summary Tests asymmetric source offsets when unmanaged image is drawn to VI.
 *          Results of the blit to compatibleImage are used for comparison.
 * @author Sergey Bylokhov
 */
public final class IncorrectUnmanagedImageSourceOffset {

    private static final int[] TYPES = {TYPE_INT_RGB, TYPE_INT_ARGB,
                                        TYPE_INT_ARGB_PRE, TYPE_INT_BGR,
                                        TYPE_3BYTE_BGR, TYPE_4BYTE_ABGR,
                                        TYPE_4BYTE_ABGR_PRE,
                                        /*TYPE_USHORT_565_RGB,
                                        TYPE_USHORT_555_RGB, TYPE_BYTE_GRAY,
                                        TYPE_USHORT_GRAY,*/ TYPE_BYTE_BINARY,
                                        TYPE_BYTE_INDEXED};
    private static final int[] TRANSPARENCIES = {OPAQUE, BITMASK, TRANSLUCENT};

    private IncorrectUnmanagedImageSourceOffset() {
    }

    public static void main(String[] args) {
        for (int viType : TRANSPARENCIES) {
            for (int biType : TYPES) {
                BufferedImage bi = makeUnmanagedBI(biType);
                fill(bi);
                test(bi, viType);
            }
        }
    }

    private static void test(BufferedImage bi, int type) {
        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice()
                                     .getDefaultConfiguration();
        VolatileImage vi = gc.createCompatibleVolatileImage(511, 255, type);
        BufferedImage gold = gc.createCompatibleImage(511, 255, type);
        // draw to compatible Image
        Graphics2D big = gold.createGraphics();
        // force scaled blit
        big.drawImage(bi, 7, 11, 127, 111, 7, 11, 127 << 1, 111, null);
        big.dispose();
        // draw to volatile image
        BufferedImage snapshot;
        while (true) {
            vi.validate(gc);
            if (vi.validate(gc) != VolatileImage.IMAGE_OK) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                continue;
            }
            Graphics2D vig = vi.createGraphics();
            // force scaled blit
            vig.drawImage(bi, 7, 11, 127, 111, 7, 11, 127 << 1, 111, null);
            vig.dispose();
            snapshot = vi.getSnapshot();
            if (vi.contentsLost()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                continue;
            }
            break;
        }
        // validate images
        for (int x = 7; x < 127; ++x) {
            for (int y = 11; y < 111; ++y) {
                if (gold.getRGB(x, y) != snapshot.getRGB(x, y)) {
                    ImageIO.write(gold, "png", new File("gold.png"));
                    ImageIO.write(snapshot, "png", new File("bi.png"));
                    throw new RuntimeException("Test failed.");
                }
            }
        }
    }

    private static BufferedImage makeUnmanagedBI(int type) {
        BufferedImage bi = new BufferedImage(511, 255, type);
        DataBuffer db = bi.getRaster().getDataBuffer();
        if (db instanceof DataBufferInt) {
            ((DataBufferInt) db).getData();
        } else if (db instanceof DataBufferShort) {
            ((DataBufferShort) db).getData();
        } else if (db instanceof DataBufferByte) {
            ((DataBufferByte) db).getData();
        } else {
            try {
                bi.setAccelerationPriority(0.0f);
            } catch (Throwable ignored) {
            }
        }
        return bi;
    }

    private static void fill(Image image) {
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setComposite(AlphaComposite.Src);
        for (int i = 0; i < image.getHeight(null); ++i) {
            graphics.setColor(new Color(i, 0, 0));
            graphics.fillRect(0, i, image.getWidth(null), 1);
        }
        graphics.dispose();
    }
}

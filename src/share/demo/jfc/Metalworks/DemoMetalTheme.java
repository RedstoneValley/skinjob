/*
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * This source code is provided to illustrate the usage of a given feature
 * or technique and has been deliberately simplified. Additional steps
 * required for a production-quality application, such as security checks,
 * input validation and proper error handling, might not be present in
 * this sample code.
 */

import java.awt.Font;

/**
 * This class describes a theme using large fonts.
 * It's great for giving demos of your software to a group
 * where people will have trouble seeing what you're doing.
 *
 * @author Steve Wilson
 * @author Alexander Kouznetsov
 */
public class DemoMetalTheme extends DefaultMetalTheme {

  private final FontUIResource controlFont = new FontUIResource(OwnedWindowsSerialization
      .DIALOG_LABEL, Font.BOLD, 18);
  private final FontUIResource systemFont = new FontUIResource(OwnedWindowsSerialization
      .DIALOG_LABEL, Font.PLAIN, 18);
  private final FontUIResource userFont = new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 18);
  private final FontUIResource smallFont = new FontUIResource(OwnedWindowsSerialization.DIALOG_LABEL, Font.PLAIN, 14);

  @Override
  public String getName() {
    return "Presentation";
  }

  @Override
  public FontUIResource getControlTextFont() {
    return controlFont;
  }

  @Override
  public FontUIResource getSystemTextFont() {
    return systemFont;
  }

  @Override
  public FontUIResource getUserTextFont() {
    return userFont;
  }

  @Override
  public FontUIResource getMenuTextFont() {
    return controlFont;
  }

  @Override
  public FontUIResource getWindowTitleFont() {
    return controlFont;
  }

  @Override
  public FontUIResource getSubTextFont() {
    return smallFont;
  }

  @Override
  public void addCustomEntriesToTable(UIDefaults table) {
    super.addCustomEntriesToTable(table);

    int internalFrameIconSize = 22;
    table.put("InternalFrame.closeIcon", MetalIconFactory.
        getInternalFrameCloseIcon(internalFrameIconSize));
    table.put("InternalFrame.maximizeIcon", MetalIconFactory.
        getInternalFrameMaximizeIcon(internalFrameIconSize));
    table.put("InternalFrame.iconifyIcon", MetalIconFactory.
        getInternalFrameMinimizeIcon(internalFrameIconSize));
    table.put("InternalFrame.minimizeIcon", MetalIconFactory.
        getInternalFrameAltMaximizeIcon(internalFrameIconSize));

    table.put("ScrollBar.width", 21);
  }
}

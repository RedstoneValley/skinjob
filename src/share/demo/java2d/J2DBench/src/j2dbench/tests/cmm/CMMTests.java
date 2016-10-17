/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

package j2dbench.tests.cmm;

import j2dbench.Group;
import j2dbench.Option;
import j2dbench.Result;
import j2dbench.Test;
import j2dbench.TestEnvironment;
import java.awt.color.ColorSpace;

public class CMMTests extends Test {

  protected static Group cmmRoot;
  protected static Group cmmOptRoot;
  protected static Option csList;
  protected static Option usePlatfromProfiles;

  protected CMMTests(Group parent, String nodeName, String description) {
    super(parent, nodeName, description);
    addDependencies(cmmOptRoot, true);
  }

  public static void init() {
    cmmRoot = new Group("cmm", "Color Management Benchmarks");
    cmmRoot.setTabbed();

    cmmOptRoot = new Group(cmmRoot, "opts", "General Options");

        /*
        usePlatfromProfiles =
                new Option.Enable(cmmOptRoot, "csPlatfrom",
                        "Use Platfrom Profiles", false);
        */
    int[] colorspaces = {
        ColorSpace.CS_sRGB, ColorSpace.CS_GRAY, ColorSpace.CS_LINEAR_RGB, ColorSpace.CS_CIEXYZ};

    String[] csNames = {
        "CS_sRGB", "CS_GRAY", "CS_LINEAR_RGB", "CS_CIEXYZ"};

    csList = new IntList(cmmOptRoot,
        "profiles",
        "Color Profiles",
        colorspaces,
        csNames,
        csNames,
        0x8);

    ColorConversionTests.init();
    ProfileTests.init();
  }

  protected static ColorSpace getColorSpace(TestEnvironment env) {
    ColorSpace cs;
    Boolean usePlatfrom = true; //(Boolean)env.getModifier(usePlatfromProfiles);

    int cs_code = env.getIntValue(csList);
    cs = ColorSpace.getInstance(cs_code);
    return cs;
  }

  @Override
  public Object initTest(TestEnvironment te, Result result) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void runTest(Object o, int i) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void cleanupTest(TestEnvironment te, Object o) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}

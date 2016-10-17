/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
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
  @bug 6572263 6571808 8005920
  @summary  PIT:FileDialog minimized to taskbar(through 'Show Desktop')selecting the fileDialog using windowList
  @author dmitry.cherepanov: area=awt.modal
  @run main/manual Winkey
*/

/*
  Winkey.java

  summary: the test verifies that pressing combination of Windows key
           and M key to minimize all windows doesn't break AWT modality
 */

import java.awt.*;
import java.awt.event.*;

public final class Winkey
{

    private Winkey() {
    }

    private static void init()
    {
        //*** Create instructions for the user here ***

        String[] instructions =
        {
            " 0. This test is for MS Windows only, if you use other OS, press \"pass\" button.",
            " 1. there is a frame with a 'show modal' button, ",
            " 2. press the button to show a modal dialog, ",
            " 3. the modal dialog will be shown over the frame, ",
            " 4. please verify that all (5.1, 5.2.1, 5.2.2) the following tests pass: ",
            " ",
            " 5.1. press combination Windows Key and M key to minimize all windows, ",
            "      note that the modal dialog and modal blocked windows are NOT minimized",
            " 5.2. press combination Windows Key and D key to show desktop, ",
            "      5.2.1. restore the dialog by choosing this one in the ALT-TAB list, ",
            "      5.2.2. restore the dialog by mouse click on taskbar (on java or any other item)",
            " ",
            " 6. make sure that the dialog and the frame are visible, ",
            "    the bounds of the windows should be the same as before, ",
            "    if it's true, then the test passed; otherwise, it failed. "
        };
        Sysout.createDialog( );
        Sysout.printInstructions( instructions );

        Frame frame = new Frame();
        Button button = new Button("show modal");
        button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    FileDialog dialog = new FileDialog((Frame)null, "Sample", FileDialog.LOAD);
                    dialog.setVisible(true);
                }
            });
        frame.add(button);
        frame.setBounds(400, 400, 200, 200);
        frame.setVisible(true);

    }//End  init()

    /*****************************************************
     * Standard Test Machinery Section
     * DO NOT modify anything in this section -- it's a
     * standard chunk of code which has all of the
     * synchronisation necessary for the test harness.
     * By keeping it the same in all tests, it is easier
     * to read and understand someone else's test, as
     * well as insuring that all tests behave correctly
     * with the test harness.
     * There is a section following this for test-defined
     * classes
     ******************************************************/
    private static boolean theTestPassed;
    private static boolean testGeneratedInterrupt;
    private static String failureMessage = "";

    private static Thread mainThread;

    private static int sleepTime = 300000;

    public static void main(String[] args ) throws InterruptedException
    {
        mainThread = Thread.currentThread();
        try
        {
            init();
        }
        catch( TestPassedException e )
        {
            //The test passed, so just return from main and harness will
            // interepret this return as a pass
            return;
        }
        //At this point, neither test passed nor test failed has been
        // called -- either would have thrown an exception and ended the
        // test, so we know we have multiple threads.

        //Test involves other threads, so sleep and wait for them to
        // called pass() or fail()
        try
        {
            Thread.sleep( sleepTime );
            //Timed out, so fail the test
            throw new RuntimeException( "Timed out after " + sleepTime/1000 + " seconds" );
        }
        catch (InterruptedException e)
        {
            if( ! testGeneratedInterrupt ) {
                throw e;
            }

            //reset flag in case hit this code more than once for some reason (just safety)
            testGeneratedInterrupt = false;
            if (!theTestPassed)
            {
                throw new RuntimeException( failureMessage );
            }
        }

    }//main

    public static synchronized void setTimeoutTo( int seconds )
    {
        sleepTime = seconds * 1000;
    }

    public static synchronized void pass()
    {
        Sysout.println( "The test passed." );
        Sysout.println( "The test is over, hit  Ctl-C to stop Java VM" );
        //first check if this is executing in main thread
        if ( mainThread == Thread.currentThread() )
        {
            //Still in the main thread, so set the flag just for kicks,
            // and throw a test passed exception which will be caught
            // and end the test.
            theTestPassed = true;
            throw new TestPassedException();
        }
        //pass was called from a different thread, so set the flag and interrupt
        // the main thead.
        theTestPassed = true;
        testGeneratedInterrupt = true;
        mainThread.interrupt();
    }//pass()

    public static synchronized void fail()
    {
        //test writer didn't specify why test failed, so give generic
        fail( "it just plain failed! :-)" );
    }

    public static synchronized void fail( String whyFailed )
    {
        Sysout.println( "The test failed: " + whyFailed );
        Sysout.println( "The test is over, hit  Ctl-C to stop Java VM" );
        //check if this called from main thread
        if ( mainThread == Thread.currentThread() )
        {
            //If main thread, fail now 'cause not sleeping
            throw new RuntimeException( whyFailed );
        }
        theTestPassed = false;
        testGeneratedInterrupt = true;
        failureMessage = whyFailed;
        mainThread.interrupt();
    }//fail()

}// class ManualMainTest

//This exception is used to exit from any level of call nesting
// when it's determined that the test has passed, and immediately
// end the test.
class TestPassedException extends RuntimeException
{
    private static final long serialVersionUID = -6943661403316731039L;
}

//*********** End Standard Test Machinery Section **********


//************ Begin classes defined for the test ****************

// make listeners in a class defined here, and instantiate them in init()

/* Example of a class which may be written as part of a test
class NewClass implements anInterface
 {
   static int newVar = 0;

   public void eventDispatched(AWTEvent e)
    {
      //Counting events to see if we get enough
      eventCount++;

      if( eventCount == 20 )
       {
         //got enough events, so pass

         ManualMainTest.pass();
       }
      else if( tries == 20 )
       {
         //tried too many times without getting enough events so fail

         ManualMainTest.fail();
       }

    }// eventDispatched()

 }// NewClass class

*/


//************** End classes defined for the test *******************

/***************************************************
 Standard Test Machinery
 DO NOT modify anything below -- it's a standard
 chunk of code whose purpose is to make user
 interaction uniform, and thereby make it simpler
 to read and understand someone else's test.
 */

/**
 This is part of the standard test machinery.
 It creates a dialog (with the instructions), and is the interface
  for sending text messages to the user.
 To print the instructions, send an array of strings to Sysout.createDialog
  WithInstructions method.  Put one line of instructions per array entry.
 To display a message for the tester to see, simply call Sysout.println
  with the string to be displayed.
 This mimics System.out.println but works within the test harness as well
  as standalone.
 */

final class Sysout
{
    private static TestDialog dialog;

    private Sysout() {
    }

    public static void createDialogWithInstructions( String[] instructions )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        dialog.printInstructions( instructions );
        dialog.setVisible(true);
        println( "Any messages for the tester will display here." );
    }

    public static void createDialog( )
    {
        dialog = new TestDialog( new Frame(), "Instructions" );
        String[] defInstr = { "Instructions will appear here. ", "" } ;
        dialog.printInstructions( defInstr );
        dialog.setVisible(true);
        println( "Any messages for the tester will display here." );
    }


    public static void printInstructions( String[] instructions )
    {
        dialog.printInstructions( instructions );
    }


    public static void println( String messageIn )
    {
        dialog.displayMessage( messageIn );
    }

}// Sysout  class

/**
  This is part of the standard test machinery.  It provides a place for the
   test instructions to be displayed, and a place for interactive messages
   to the user to be displayed.
  To have the test instructions displayed, see Sysout.
  To have a message to the user be displayed, see Sysout.
  Do not call anything in this dialog directly.
  */
class TestDialog extends Dialog implements ActionListener
{

    private static final long serialVersionUID = -33520503483104552L;
    final TextArea instructionsText;
    final TextArea messageText;
    final int maxStringLength = 80;
    final Panel  buttonP = new Panel();
    Button passB = new Button( "pass" );
    Button failB = new Button( "fail" );

    //DO NOT call this directly, go through Sysout
    public TestDialog( Frame frame, String name )
    {
        super( frame, name );
        int scrollBoth = TextArea.SCROLLBARS_BOTH;
        instructionsText = new TextArea( "", 15, maxStringLength, scrollBoth );
        add(BorderLayout.NORTH, instructionsText);

        messageText = new TextArea( "", 5, maxStringLength, scrollBoth );
        add(BorderLayout.CENTER, messageText);

        passB = new Button( "pass" );
        passB.setActionCommand( "pass" );
        passB.addActionListener( this );
        buttonP.add( "East", passB);

        failB = new Button( "fail" );
        failB.setActionCommand( "fail" );
        failB.addActionListener( this );
        buttonP.add( "West", failB);

        add(BorderLayout.SOUTH, buttonP);
        pack();

        setVisible(true);
    }// TestDialog()

    //DO NOT call this directly, go through Sysout
    public void printInstructions( String[] instructions )
    {
        //Clear out any current instructions
        instructionsText.setText( "" );

        //Go down array of instruction strings

        String printStr, remainingStr;
        for (String instruction : instructions) {
            //chop up each into pieces maxSringLength long
            remainingStr = instruction;
            while (!remainingStr.isEmpty()) {
                //if longer than max then chop off first max chars to print
                if (remainingStr.length() >= maxStringLength) {
                    //Try to chop on a word boundary
                    int posOfSpace = remainingStr.
                        lastIndexOf(' ', maxStringLength - 1);

                    if (posOfSpace <= 0) {
                        posOfSpace = maxStringLength - 1;
                    }

                    printStr = remainingStr.substring(0, posOfSpace + 1);
                    remainingStr = remainingStr.substring(posOfSpace + 1);
                }
                //else just print
                else {
                    printStr = remainingStr;
                    remainingStr = "";
                }

                instructionsText.append(printStr + "\n");
            }// while
        }// for

    }//printInstructions()

    //DO NOT call this directly, go through Sysout
    public void displayMessage( String messageIn )
    {
        messageText.append( messageIn + "\n" );
        System.out.println(messageIn);
    }

    //catch presses of the passed and failed buttons.
    //simply call the standard pass() or fail() static methods of
    //ManualMainTest
    @Override
    public void actionPerformed( ActionEvent e )
    {
        if( e.getActionCommand() == "pass" )
        {
            Winkey.pass();
        }
        else
        {
            Winkey.fail();
        }
    }

}// TestDialog  class

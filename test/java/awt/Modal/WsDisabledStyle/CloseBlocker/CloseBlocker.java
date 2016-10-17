/*
 * Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.
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
  @test %I% %E%
  @bug 4080029
  @summary Modal Dialog block input to all frame windows not just its parent.
  @author dmitry.cherepanov: area=awt.modal
  @run main/manual CloseBlocker
*/

/*
  ManualMainTest.java

  summary: The test opens and closes blocker dialog, the test verifies
           that active window is correct when the dialog is closed.
 */

import java.awt.*;
import java.awt.event.*;

public final class CloseBlocker
{

    private static void init()
    {
        //*** Create instructions for the user here ***

        String[] instructions =
        {
            " the test will be run 6 times, to start next test just close all ",
            " windows of previous; the instructions are the same for all tests: ",
            " 1) there are two frames (one the frames has 'show modal' button), ",
            " 2) press the button to show a dialog, ",
            " 3) close the dialog (an alternative scenario - activate another",
            "    native window before closing the dialog), ",
            " 4) the frame with button should become next active window, ",
            "    if it's true, then the test passed, otherwise, it failed. ",
            " Press 'pass' button only after all of the 6 tests are completed, ",
            " the number of the currently executed test is displayed on the ",
            " output window. "
        };
        Sysout.createDialog( );
        Sysout.printInstructions( instructions );

        test(true, true, false);
        test(true, true, true);
        test(false, true, false); // 3rd parameter has no affect for ownerless

        test(true, false, false);
        test(true, false, true);
        test(false, false, false); // 3rd parameter has no affect for ownerless

    }//End  init()

    static final Object obj = new Object();
    private static int counter;

    /*
     * The ownerless parameter indicates whether the blocker dialog
     * has owner. The usual parameter indicates whether the blocker
     * dialog is a Java dialog (non-native dialog like file dialog).
     */
    private static void test(boolean ownerless, boolean usual, boolean initiallyOwnerIsActive) {

        ++counter;
        Sysout.print(" * test #" + counter + " is running ... ");

        Frame active = new Frame();
        Frame nonactive = new Frame();
        Button button = new Button("show modal");
        button.addActionListener(new ActionListener() {
               @Override
               public void actionPerformed(ActionEvent ae) {
                    Dialog dialog;
                    Frame parent = ownerless ? null : initiallyOwnerIsActive? active : nonactive;
                   dialog = usual ? new Dialog(parent, "Sample", true)
                       : new FileDialog(parent, "Sample", FileDialog.LOAD);
                    dialog.addWindowListener(new WindowAdapter(){
                        @Override
                        public void windowClosing(WindowEvent e){
                                e.getWindow().dispose();
                        }
                    });
                    dialog.setBounds(200, 200, 200, 200);
                    dialog.setVisible(true);
                }
        });

        active.add(button);
        active.setBounds(200, 400, 200, 200);
        WindowAdapter adapter = new WindowAdapter(){
              @Override
              public void windowClosing(WindowEvent e){
                    active.dispose();
                    nonactive.dispose();
                    synchronized(obj) {
                        obj.notify();
                    }
                }
             };
        active.addWindowListener(adapter);
        active.setVisible(true);

        nonactive.setBounds(400, 400, 200, 200);
        nonactive.addWindowListener(adapter);
        nonactive.setVisible(true);

        synchronized(obj) {
            try{
                obj.wait();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }

        Sysout.println(" completed. ");

    }

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
        dialog.displayMessage( messageIn, true );
    }

    public static void print( String messageIn )
    {
        dialog.displayMessage( messageIn, false );
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

    private static final long serialVersionUID = 8306280896419151608L;
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
    public void displayMessage( String messageIn, boolean nextLine )
    {
        messageText.append( messageIn + (nextLine? "\n" : "") );
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
            CloseBlocker.pass();
        }
        else
        {
            CloseBlocker.fail();
        }
    }

}// TestDialog  class

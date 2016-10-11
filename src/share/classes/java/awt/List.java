/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package java.awt;

import android.content.Context;
import android.view.View;
import android.widget.Spinner;

import java.util.Vector;
import java.util.EventListener;
import java.awt.peer.ListPeer;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;


/**
 * The <code>List</code> component presents the user with a
 * scrolling list of text items. The list can be set up so that
 * the user can choose either one item or multiple items.
 * <p>
 * For example, the code&nbsp;.&nbsp;.&nbsp;.
 *
 * <hr><blockquote><pre>
 * List lst = new List(4, false);
 * lst.add("Mercury");
 * lst.add("Venus");
 * lst.add("Earth");
 * lst.add("JavaSoft");
 * lst.add("Mars");
 * lst.add("Jupiter");
 * lst.add("Saturn");
 * lst.add("Uranus");
 * lst.add("Neptune");
 * lst.add("Pluto");
 * cnt.add(lst);
 * </pre></blockquote><hr>
 * <p>
 * where <code>cnt</code> is a container, produces the following
 * scrolling list:
 * <p>
 * <img src="doc-files/List-1.gif"
 * alt="Shows a list containing: Venus, Earth, JavaSoft, and Mars. Javasoft is selected." style="float:center; margin: 7px 10px;">
 * <p>
 * If the List allows multiple selections, then clicking on
 * an item that is already selected deselects it. In the preceding
 * example, only one item from the scrolling list can be selected
 * at a time, since the second argument when creating the new scrolling
 * list is <code>false</code>. If the List does not allow multiple
 * selections, selecting an item causes any other selected item
 * to be deselected.
 * <p>
 * Note that the list in the example shown was created with four visible
 * rows.  Once the list has been created, the number of visible rows
 * cannot be changed.  A default <code>List</code> is created with
 * four rows, so that <code>lst = new List()</code> is equivalent to
 * <code>list = new List(4, false)</code>.
 * <p>
 * Beginning with Java&nbsp;1.1, the Abstract Window Toolkit
 * sends the <code>List</code> object all mouse, keyboard, and focus events
 * that occur over it. (The old AWT event model is being maintained
 * only for backwards compatibility, and its use is discouraged.)
 * <p>
 * When an item is selected or deselected by the user, AWT sends an instance
 * of <code>ItemEvent</code> to the list.
 * When the user double-clicks on an item in a scrolling list,
 * AWT sends an instance of <code>ActionEvent</code> to the
 * list following the item event. AWT also generates an action event
 * when the user presses the return key while an item in the
 * list is selected.
 * <p>
 * If an application wants to perform some action based on an item
 * in this list being selected or activated by the user, it should implement
 * <code>ItemListener</code> or <code>ActionListener</code>
 * as appropriate and register the new listener to receive
 * events from this list.
 * <p>
 * For multiple-selection scrolling lists, it is considered a better
 * user interface to use an external gesture (such as clicking on a
 * button) to trigger the action.
 * @author      Sami Shaio
 * @see         java.awt.event.ItemEvent
 * @see         java.awt.event.ItemListener
 * @see         java.awt.event.ActionEvent
 * @see         java.awt.event.ActionListener
 * @since       JDK1.0
 */
public class List extends Component implements ItemSelectable {
    /**
     * A vector created to contain items which will become
     * part of the List Component.
     *
     * @serial
     * @see #addItem(String)
     * @see #getItem(int)
     */
    Vector<String>      items = new Vector<>();

    /**
     * This field will represent the number of visible rows in the
     * <code>List</code> Component.  It is specified only once, and
     * that is when the list component is actually
     * created.  It will never change.
     *
     * @serial
     * @see #getRows()
     */
    int         rows = 0;

    /**
     * <code>multipleMode</code> is a variable that will
     * be set to <code>true</code> if a list component is to be set to
     * multiple selection mode, that is where the user can
     * select more than one item in a list at one time.
     * <code>multipleMode</code> will be set to false if the
     * list component is set to single selection, that is where
     * the user can only select one item on the list at any
     * one time.
     *
     * @serial
     * @see #isMultipleMode()
     * @see #setMultipleMode(boolean)
     */
    boolean     multipleMode = false;

    /**
     * <code>selected</code> is an array that will contain
     * the indices of items that have been selected.
     *
     * @serial
     * @see #getSelectedIndexes()
     * @see #getSelectedIndex()
     */
    int         selected[] = new int[0];

    /**
     * This variable contains the value that will be used
     * when trying to make a particular list item visible.
     *
     * @serial
     * @see #makeVisible(int)
     */
    int         visibleIndex = -1;

    transient ActionListener actionListener;
    transient ItemListener itemListener;

    private static final String base = "list";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = -3304312411574666869L;

    /**
     * Creates a new scrolling list.
     * By default, there are four visible lines and multiple selections are
     * not allowed.  Note that this is a convenience method for
     * <code>List(0, false)</code>.  Also note that the number of visible
     * lines in the list cannot be changed after it has been created.
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public List() throws HeadlessException {
        this(0, false);
    }

    /**
     * Creates a new scrolling list initialized with the specified
     * number of visible lines. By default, multiple selections are
     * not allowed.  Note that this is a convenience method for
     * <code>List(rows, false)</code>.  Also note that the number
     * of visible rows in the list cannot be changed after it has
     * been created.
     * @param       rows the number of items to show.
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since       JDK1.1
     */
    public List(int rows) throws HeadlessException {
        this(rows, false);
    }

    /**
     * The default number of visible rows is 4.  A list with
     * zero rows is unusable and unsightly.
     */
    final static int    DEFAULT_VISIBLE_ROWS = 4;

    /**
     * Creates a new scrolling list initialized to display the specified
     * number of rows. Note that if zero rows are specified, then
     * the list will be created with a default of four rows.
     * Also note that the number of visible rows in the list cannot
     * be changed after it has been created.
     * If the value of <code>multipleMode</code> is
     * <code>true</code>, then the user can select multiple items from
     * the list. If it is <code>false</code>, only one item at a time
     * can be selected.
     * @param       rows   the number of items to show.
     * @param       multipleMode   if <code>true</code>,
     *                     then multiple selections are allowed;
     *                     otherwise, only one item can be selected at a time.
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public List(int rows, boolean multipleMode) throws HeadlessException {
        super(Spinner.class);
        GraphicsEnvironment.checkHeadless();
        this.rows = (rows != 0) ? rows : DEFAULT_VISIBLE_ROWS;
        this.multipleMode = multipleMode;
    }

    /**
     * Construct a name for this component.  Called by
     * <code>getName</code> when the name is <code>null</code>.
     */
    String constructComponentName() {
        synchronized (List.class) {
            return base + nameCounter++;
        }
    }

    /**
     * Creates the peer for the list.  The peer allows us to modify the
     * list's appearance without changing its functionality.
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createList(this);
            super.addNotify();
        }
    }

    /**
     * Removes the peer for this list.  The peer allows us to modify the
     * list's appearance without changing its functionality.
     */
    public void removeNotify() {
        synchronized (getTreeLock()) {
            ListPeer peer = (ListPeer)this.peer;
            if (peer != null) {
                selected = peer.getSelectedIndexes();
            }
            super.removeNotify();
        }
    }

    /**
     * Gets the number of items in the list.
     * @return     the number of items in the list
     * @see        #getItem
     * @since      JDK1.1
     */
    public int getItemCount() {
        return countItems();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getItemCount()</code>.
     */
    @Deprecated
    public int countItems() {
        return items.size();
    }

    /**
     * Gets the item associated with the specified index.
     * @return       an item that is associated with
     *                    the specified index
     * @param        index the position of the item
     * @see          #getItemCount
     */
    public String getItem(int index) {
        return getItemImpl(index);
    }

    // NOTE: This method may be called by privileged threads.
    //       We implement this functionality in a package-private method
    //       to insure that it cannot be overridden by client subclasses.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    final String getItemImpl(int index) {
        return items.elementAt(index);
    }

    /**
     * Gets the items in the list.
     * @return       a string array containing items of the list
     * @see          #select
     * @see          #deselect
     * @see          #isIndexSelected
     * @since        JDK1.1
     */
    public synchronized String[] getItems() {
        String itemCopies[] = new String[items.size()];
        items.copyInto(itemCopies);
        return itemCopies;
    }

    /**
     * Adds the specified item to the end of scrolling list.
     * @param item the item to be added
     * @since JDK1.1
     */
    public void add(String item) {
        addItem(item);
    }

    /**
     * @deprecated      replaced by <code>add(String)</code>.
     */
    @Deprecated
    public void addItem(String item) {
        addItem(item, -1);
    }

    /**
     * Adds the specified item to the the scrolling list
     * at the position indicated by the index.  The index is
     * zero-based.  If the value of the index is less than zero,
     * or if the value of the index is greater than or equal to
     * the number of items in the list, then the item is added
     * to the end of the list.
     * @param       item   the item to be added;
     *              if this parameter is <code>null</code> then the item is
     *              treated as an empty string, <code>""</code>
     * @param       index  the position at which to add the item
     * @since       JDK1.1
     */
    public void add(String item, int index) {
        addItem(item, index);
    }

    /**
     * @deprecated      replaced by <code>add(String, int)</code>.
     */
    @Deprecated
    public synchronized void addItem(String item, int index) {
        if (index < -1 || index >= items.size()) {
            index = -1;
        }

        if (item == null) {
            item = "";
        }

        if (index == -1) {
            items.addElement(item);
        } else {
            items.insertElementAt(item, index);
        }

        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            peer.add(item, index);
        }
    }

    /**
     * Replaces the item at the specified index in the scrolling list
     * with the new string.
     * @param       newValue   a new string to replace an existing item
     * @param       index      the position of the item to replace
     * @exception ArrayIndexOutOfBoundsException if <code>index</code>
     *          is out of range
     */
    public synchronized void replaceItem(String newValue, int index) {
        remove(index);
        add(newValue, index);
    }

    /**
     * Removes all items from this list.
     * @see #remove
     * @see #delItems
     * @since JDK1.1
     */
    public void removeAll() {
        clear();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>removeAll()</code>.
     */
    @Deprecated
    public synchronized void clear() {
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            peer.removeAll();
        }
        items = new Vector<>();
        selected = new int[0];
    }

    /**
     * Removes the first occurrence of an item from the list.
     * If the specified item is selected, and is the only selected
     * item in the list, the list is set to have no selection.
     * @param        item  the item to remove from the list
     * @exception    IllegalArgumentException
     *                     if the item doesn't exist in the list
     * @since        JDK1.1
     */
    public synchronized void remove(String item) {
        int index = items.indexOf(item);
        if (index < 0) {
            throw new IllegalArgumentException("item " + item +
                                               " not found in list");
        } else {
            remove(index);
        }
    }

    /**
     * Removes the item at the specified position
     * from this scrolling list.
     * If the item with the specified position is selected, and is the
     * only selected item in the list, the list is set to have no selection.
     * @param      position   the index of the item to delete
     * @see        #add(String, int)
     * @since      JDK1.1
     * @exception    ArrayIndexOutOfBoundsException
     *               if the <code>position</code> is less than 0 or
     *               greater than <code>getItemCount()-1</code>
     */
    public void remove(int position) {
        delItem(position);
    }

    /**
     * @deprecated     replaced by <code>remove(String)</code>
     *                         and <code>remove(int)</code>.
     */
    @Deprecated
    public void delItem(int position) {
        delItems(position, position);
    }

    /**
     * Gets the index of the selected item on the list,
     *
     * @return        the index of the selected item;
     *                if no item is selected, or if multiple items are
     *                selected, <code>-1</code> is returned.
     * @see           #select
     * @see           #deselect
     * @see           #isIndexSelected
     */
    public synchronized int getSelectedIndex() {
        int sel[] = getSelectedIndexes();
        return (sel.length == 1) ? sel[0] : -1;
    }

    /**
     * Gets the selected indexes on the list.
     *
     * @return        an array of the selected indexes on this scrolling list;
     *                if no item is selected, a zero-length array is returned.
     * @see           #select
     * @see           #deselect
     * @see           #isIndexSelected
     */
    public synchronized int[] getSelectedIndexes() {
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            selected = peer.getSelectedIndexes();
        }
        return selected.clone();
    }

    /**
     * Gets the selected item on this scrolling list.
     *
     * @return        the selected item on the list;
     *                if no item is selected, or if multiple items are
     *                selected, <code>null</code> is returned.
     * @see           #select
     * @see           #deselect
     * @see           #isIndexSelected
     */
    public synchronized String getSelectedItem() {
        int index = getSelectedIndex();
        return (index < 0) ? null : getItem(index);
    }

    /**
     * Gets the selected items on this scrolling list.
     *
     * @return        an array of the selected items on this scrolling list;
     *                if no item is selected, a zero-length array is returned.
     * @see           #select
     * @see           #deselect
     * @see           #isIndexSelected
     */
    public synchronized String[] getSelectedItems() {
        int sel[] = getSelectedIndexes();
        String str[] = new String[sel.length];
        for (int i = 0 ; i < sel.length ; i++) {
            str[i] = getItem(sel[i]);
        }
        return str;
    }

    /**
     * Gets the selected items on this scrolling list in an array of Objects.
     * @return        an array of <code>Object</code>s representing the
     *                selected items on this scrolling list;
     *                if no item is selected, a zero-length array is returned.
     * @see #getSelectedItems
     * @see ItemSelectable
     */
    public Object[] getSelectedObjects() {
        return getSelectedItems();
    }

    /**
     * Selects the item at the specified index in the scrolling list.
     *<p>
     * Note that passing out of range parameters is invalid,
     * and will result in unspecified behavior.
     *
     * <p>Note that this method should be primarily used to
     * initially select an item in this component.
     * Programmatically calling this method will <i>not</i> trigger
     * an <code>ItemEvent</code>.  The only way to trigger an
     * <code>ItemEvent</code> is by user interaction.
     *
     * @param        index the position of the item to select
     * @see          #getSelectedItem
     * @see          #deselect
     * @see          #isIndexSelected
     */
    public void select(int index) {
        // Bug #4059614: select can't be synchronized while calling the peer,
        // because it is called from the Window Thread.  It is sufficient to
        // synchronize the code that manipulates 'selected' except for the
        // case where the peer changes.  To handle this case, we simply
        // repeat the selection process.

        ListPeer peer;
        do {
            peer = (ListPeer)this.peer;
            if (peer != null) {
                peer.select(index);
                return;
            }

            synchronized(this)
            {
                boolean alreadySelected = false;

                for (int i = 0 ; i < selected.length ; i++) {
                    if (selected[i] == index) {
                        alreadySelected = true;
                        break;
                    }
                }

                if (!alreadySelected) {
                    if (!multipleMode) {
                        selected = new int[1];
                        selected[0] = index;
                    } else {
                        int newsel[] = new int[selected.length + 1];
                        System.arraycopy(selected, 0, newsel, 0,
                                         selected.length);
                        newsel[selected.length] = index;
                        selected = newsel;
                    }
                }
            }
        } while (peer != this.peer);
    }

    /**
     * Deselects the item at the specified index.
     * <p>
     * Note that passing out of range parameters is invalid,
     * and will result in unspecified behavior.
     * <p>
     * If the item at the specified index is not selected,
     * then the operation is ignored.
     * @param        index the position of the item to deselect
     * @see          #select
     * @see          #getSelectedItem
     * @see          #isIndexSelected
     */
    public synchronized void deselect(int index) {
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            if (isMultipleMode() || (getSelectedIndex() == index)) {
                peer.deselect(index);
            }
        }

        for (int i = 0 ; i < selected.length ; i++) {
            if (selected[i] == index) {
                int newsel[] = new int[selected.length - 1];
                System.arraycopy(selected, 0, newsel, 0, i);
                System.arraycopy(selected, i+1, newsel, i, selected.length - (i+1));
                selected = newsel;
                return;
            }
        }
    }

    /**
     * Determines if the specified item in this scrolling list is
     * selected.
     * @param      index   the item to be checked
     * @return     <code>true</code> if the specified item has been
     *                       selected; <code>false</code> otherwise
     * @see        #select
     * @see        #deselect
     * @since      JDK1.1
     */
    public boolean isIndexSelected(int index) {
        return isSelected(index);
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>isIndexSelected(int)</code>.
     */
    @Deprecated
    public boolean isSelected(int index) {
        int sel[] = getSelectedIndexes();
        for (int i = 0 ; i < sel.length ; i++) {
            if (sel[i] == index) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the number of visible lines in this list.  Note that
     * once the <code>List</code> has been created, this number
     * will never change.
     * @return     the number of visible lines in this scrolling list
     */
    public int getRows() {
        return rows;
    }

    /**
     * Determines whether this list allows multiple selections.
     * @return     <code>true</code> if this list allows multiple
     *                 selections; otherwise, <code>false</code>
     * @see        #setMultipleMode
     * @since      JDK1.1
     */
    public boolean isMultipleMode() {
        return allowsMultipleSelections();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>isMultipleMode()</code>.
     */
    @Deprecated
    public boolean allowsMultipleSelections() {
        return multipleMode;
    }

    /**
     * Sets the flag that determines whether this list
     * allows multiple selections.
     * When the selection mode is changed from multiple-selection to
     * single-selection, the selected items change as follows:
     * If a selected item has the location cursor, only that
     * item will remain selected.  If no selected item has the
     * location cursor, all items will be deselected.
     * @param       b   if <code>true</code> then multiple selections
     *                      are allowed; otherwise, only one item from
     *                      the list can be selected at once
     * @see         #isMultipleMode
     * @since       JDK1.1
     */
    public void setMultipleMode(boolean b) {
        setMultipleSelections(b);
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setMultipleMode(boolean)</code>.
     */
    @Deprecated
    public synchronized void setMultipleSelections(boolean b) {
        if (b != multipleMode) {
            multipleMode = b;
            ListPeer peer = (ListPeer)this.peer;
            if (peer != null) {
                peer.setMultipleMode(b);
            }
        }
    }

    /**
     * Gets the index of the item that was last made visible by
     * the method <code>makeVisible</code>.
     * @return      the index of the item that was last made visible
     * @see         #makeVisible
     */
    public int getVisibleIndex() {
        return visibleIndex;
    }

    /**
     * Makes the item at the specified index visible.
     * @param       index    the position of the item
     * @see         #getVisibleIndex
     */
    public synchronized void makeVisible(int index) {
        visibleIndex = index;
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            peer.makeVisible(index);
        }
    }

    /**
     * Gets the preferred dimensions for a list with the specified
     * number of rows.
     * @param      rows    number of rows in the list
     * @return     the preferred dimensions for displaying this scrolling list
     *             given that the specified number of rows must be visible
     * @see        java.awt.Component#getPreferredSize
     * @since      JDK1.1
     */
    public Dimension getPreferredSize(int rows) {
        return preferredSize(rows);
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getPreferredSize(int)</code>.
     */
    @Deprecated
    public Dimension preferredSize(int rows) {
        synchronized (getTreeLock()) {
            ListPeer peer = (ListPeer)this.peer;
            return (peer != null) ?
                       peer.getPreferredSize(rows) :
                       super.preferredSize();
        }
    }

    /**
     * Gets the preferred size of this scrolling list.
     * @return     the preferred dimensions for displaying this scrolling list
     * @see        java.awt.Component#getPreferredSize
     * @since      JDK1.1
     */
    public Dimension getPreferredSize() {
        return preferredSize();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getPreferredSize()</code>.
     */
    @Deprecated
    public Dimension preferredSize() {
        synchronized (getTreeLock()) {
            return (rows > 0) ?
                       preferredSize(rows) :
                       super.preferredSize();
        }
    }

    /**
     * Gets the minimum dimensions for a list with the specified
     * number of rows.
     * @param      rows    number of rows in the list
     * @return     the minimum dimensions for displaying this scrolling list
     *             given that the specified number of rows must be visible
     * @see        java.awt.Component#getMinimumSize
     * @since      JDK1.1
     */
    public Dimension getMinimumSize(int rows) {
        return minimumSize(rows);
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getMinimumSize(int)</code>.
     */
    @Deprecated
    public Dimension minimumSize(int rows) {
        synchronized (getTreeLock()) {
            ListPeer peer = (ListPeer)this.peer;
            return (peer != null) ?
                       peer.getMinimumSize(rows) :
                       super.minimumSize();
        }
    }

    /**
     * Determines the minimum size of this scrolling list.
     * @return       the minimum dimensions needed
     *                        to display this scrolling list
     * @see          java.awt.Component#getMinimumSize()
     * @since        JDK1.1
     */
    public Dimension getMinimumSize() {
        return minimumSize();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getMinimumSize()</code>.
     */
    @Deprecated
    public Dimension minimumSize() {
        synchronized (getTreeLock()) {
            return (rows > 0) ? minimumSize(rows) : super.minimumSize();
        }
    }

    /**
     * Adds the specified item listener to receive item events from
     * this list.  Item events are sent in response to user input, but not
     * in response to calls to <code>select</code> or <code>deselect</code>.
     * If listener <code>l</code> is <code>null</code>,
     * no exception is thrown and no action is performed.
     * <p>Refer to <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT Threading Issues</a> for details on AWT's threading model.
     *
     * @param         l the item listener
     * @see           #removeItemListener
     * @see           #getItemListeners
     * @see           #select
     * @see           #deselect
     * @see           java.awt.event.ItemEvent
     * @see           java.awt.event.ItemListener
     * @since         JDK1.1
     */
    public synchronized void addItemListener(ItemListener l) {
        if (l == null) {
            return;
        }
        itemListener = AWTEventMulticaster.add(itemListener, l);
        newEventsOnly = true;
    }

    /**
     * Removes the specified item listener so that it no longer
     * receives item events from this list.
     * If listener <code>l</code> is <code>null</code>,
     * no exception is thrown and no action is performed.
     * <p>Refer to <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT Threading Issues</a> for details on AWT's threading model.
     *
     * @param           l the item listener
     * @see             #addItemListener
     * @see             #getItemListeners
     * @see             java.awt.event.ItemEvent
     * @see             java.awt.event.ItemListener
     * @since           JDK1.1
     */
    public synchronized void removeItemListener(ItemListener l) {
        if (l == null) {
            return;
        }
        itemListener = AWTEventMulticaster.remove(itemListener, l);
    }

    /**
     * Returns an array of all the item listeners
     * registered on this list.
     *
     * @return all of this list's <code>ItemListener</code>s
     *         or an empty array if no item
     *         listeners are currently registered
     *
     * @see             #addItemListener
     * @see             #removeItemListener
     * @see             java.awt.event.ItemEvent
     * @see             java.awt.event.ItemListener
     * @since 1.4
     */
    public synchronized ItemListener[] getItemListeners() {
        return getListeners(ItemListener.class);
    }

    /**
     * Adds the specified action listener to receive action events from
     * this list. Action events occur when a user double-clicks
     * on a list item or types Enter when the list has the keyboard
     * focus.
     * <p>
     * If listener <code>l</code> is <code>null</code>,
     * no exception is thrown and no action is performed.
     * <p>Refer to <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT Threading Issues</a> for details on AWT's threading model.
     *
     * @param         l the action listener
     * @see           #removeActionListener
     * @see           #getActionListeners
     * @see           java.awt.event.ActionEvent
     * @see           java.awt.event.ActionListener
     * @since         JDK1.1
     */
    public synchronized void addActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        actionListener = AWTEventMulticaster.add(actionListener, l);
        newEventsOnly = true;
    }

    /**
     * Removes the specified action listener so that it no longer
     * receives action events from this list. Action events
     * occur when a user double-clicks on a list item.
     * If listener <code>l</code> is <code>null</code>,
     * no exception is thrown and no action is performed.
     * <p>Refer to <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT Threading Issues</a> for details on AWT's threading model.
     *
     * @param           l     the action listener
     * @see             #addActionListener
     * @see             #getActionListeners
     * @see             java.awt.event.ActionEvent
     * @see             java.awt.event.ActionListener
     * @since           JDK1.1
     */
    public synchronized void removeActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        actionListener = AWTEventMulticaster.remove(actionListener, l);
    }

    /**
     * Returns an array of all the action listeners
     * registered on this list.
     *
     * @return all of this list's <code>ActionListener</code>s
     *         or an empty array if no action
     *         listeners are currently registered
     *
     * @see             #addActionListener
     * @see             #removeActionListener
     * @see             java.awt.event.ActionEvent
     * @see             java.awt.event.ActionListener
     * @since 1.4
     */
    public synchronized ActionListener[] getActionListeners() {
        return getListeners(ActionListener.class);
    }

    /**
     * Returns an array of all the objects currently registered
     * as <code><em>Foo</em>Listener</code>s
     * upon this <code>List</code>.
     * <code><em>Foo</em>Listener</code>s are registered using the
     * <code>add<em>Foo</em>Listener</code> method.
     *
     * <p>
     * You can specify the <code>listenerType</code> argument
     * with a class literal, such as
     * <code><em>Foo</em>Listener.class</code>.
     * For example, you can query a
     * <code>List</code> <code>l</code>
     * for its item listeners with the following code:
     *
     * <pre>ItemListener[] ils = (ItemListener[])(l.getListeners(ItemListener.class));</pre>
     *
     * If no such listeners exist, this method returns an empty array.
     *
     * @param listenerType the type of listeners requested; this parameter
     *          should specify an interface that descends from
     *          <code>java.util.EventListener</code>
     * @return an array of all objects registered as
     *          <code><em>Foo</em>Listener</code>s on this list,
     *          or an empty array if no such
     *          listeners have been added
     * @exception ClassCastException if <code>listenerType</code>
     *          doesn't specify a class or interface that implements
     *          <code>java.util.EventListener</code>
     *
     * @see #getItemListeners
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == ActionListener.class) {
            l = actionListener;
        } else if  (listenerType == ItemListener.class) {
            l = itemListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e) {
        switch(e.id) {
          case ActionEvent.ACTION_PERFORMED:
            if ((eventMask & AWTEvent.ACTION_EVENT_MASK) != 0 ||
                actionListener != null) {
                return true;
            }
            return false;
          case ItemEvent.ITEM_STATE_CHANGED:
            if ((eventMask & AWTEvent.ITEM_EVENT_MASK) != 0 ||
                itemListener != null) {
                return true;
            }
            return false;
          default:
            break;
        }
        return super.eventEnabled(e);
    }

    /**
     * Processes events on this scrolling list. If an event is
     * an instance of <code>ItemEvent</code>, it invokes the
     * <code>processItemEvent</code> method. Else, if the
     * event is an instance of <code>ActionEvent</code>,
     * it invokes <code>processActionEvent</code>.
     * If the event is not an item event or an action event,
     * it invokes <code>processEvent</code> on the superclass.
     * <p>Note that if the event parameter is <code>null</code>
     * the behavior is unspecified and may result in an
     * exception.
     *
     * @param        e the event
     * @see          java.awt.event.ActionEvent
     * @see          java.awt.event.ItemEvent
     * @see          #processActionEvent
     * @see          #processItemEvent
     * @since        JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ItemEvent) {
            processItemEvent((ItemEvent)e);
            return;
        } else if (e instanceof ActionEvent) {
            processActionEvent((ActionEvent)e);
            return;
        }
        super.processEvent(e);
    }

    /**
     * Processes item events occurring on this list by
     * dispatching them to any registered
     * <code>ItemListener</code> objects.
     * <p>
     * This method is not called unless item events are
     * enabled for this component. Item events are enabled
     * when one of the following occurs:
     * <ul>
     * <li>An <code>ItemListener</code> object is registered
     * via <code>addItemListener</code>.
     * <li>Item events are enabled via <code>enableEvents</code>.
     * </ul>
     * <p>Note that if the event parameter is <code>null</code>
     * the behavior is unspecified and may result in an
     * exception.
     *
     * @param       e the item event
     * @see         java.awt.event.ItemEvent
     * @see         java.awt.event.ItemListener
     * @see         #addItemListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processItemEvent(ItemEvent e) {
        ItemListener listener = itemListener;
        if (listener != null) {
            listener.itemStateChanged(e);
        }
    }

    /**
     * Processes action events occurring on this component
     * by dispatching them to any registered
     * <code>ActionListener</code> objects.
     * <p>
     * This method is not called unless action events are
     * enabled for this component. Action events are enabled
     * when one of the following occurs:
     * <ul>
     * <li>An <code>ActionListener</code> object is registered
     * via <code>addActionListener</code>.
     * <li>Action events are enabled via <code>enableEvents</code>.
     * </ul>
     * <p>Note that if the event parameter is <code>null</code>
     * the behavior is unspecified and may result in an
     * exception.
     *
     * @param       e the action event
     * @see         java.awt.event.ActionEvent
     * @see         java.awt.event.ActionListener
     * @see         #addActionListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processActionEvent(ActionEvent e) {
        ActionListener listener = actionListener;
        if (listener != null) {
            listener.actionPerformed(e);
        }
    }

    /**
     * Returns the parameter string representing the state of this
     * scrolling list. This string is useful for debugging.
     * @return    the parameter string of this scrolling list
     */
    protected String paramString() {
        return super.paramString() + ",selected=" + getSelectedItem();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * Not for public use in the future.
     * This method is expected to be retained only as a package
     * private method.
     */
    @Deprecated
    public synchronized void delItems(int start, int end) {
        for (int i = end; i >= start; i--) {
            items.removeElementAt(i);
        }
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
            peer.delItems(start, end);
        }
    }

    /*
     * Serialization support.  Since the value of the selected
     * field isn't necessarily up to date, we sync it up with the
     * peer before serializing.
     */

    /**
     * The <code>List</code> component's
     * Serialized Data Version.
     *
     * @serial
     */
    private int listSerializedDataVersion = 1;

    /**
     * Writes default serializable fields to stream.  Writes
     * a list of serializable <code>ItemListeners</code>
     * and <code>ActionListeners</code> as optional data.
     * The non-serializable listeners are detected and
     * no attempt is made to serialize them.
     *
     * @serialData <code>null</code> terminated sequence of 0
     *  or more pairs; the pair consists of a <code>String</code>
     *  and an <code>Object</code>; the <code>String</code>
     *  indicates the type of object and is one of the
     *  following:
     *  <code>itemListenerK</code> indicating an
     *    <code>ItemListener</code> object;
     *  <code>actionListenerK</code> indicating an
     *    <code>ActionListener</code> object
     *
     * @param s the <code>ObjectOutputStream</code> to write
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see java.awt.Component#itemListenerK
     * @see java.awt.Component#actionListenerK
     * @see #readObject(ObjectInputStream)
     */
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
      synchronized (this) {
        ListPeer peer = (ListPeer)this.peer;
        if (peer != null) {
          selected = peer.getSelectedIndexes();
        }
      }
      s.defaultWriteObject();

      AWTEventMulticaster.save(s, itemListenerK, itemListener);
      AWTEventMulticaster.save(s, actionListenerK, actionListener);
      s.writeObject(null);
    }

    /**
     * Reads the <code>ObjectInputStream</code> and if it
     * isn't <code>null</code> adds a listener to receive
     * both item events and action events (as specified
     * by the key stored in the stream) fired by the
     * <code>List</code>.
     * Unrecognized keys or values will be ignored.
     *
     * @param s the <code>ObjectInputStream</code> to write
     * @exception HeadlessException if
     *   <code>GraphicsEnvironment.isHeadless</code> returns
     *   <code>true</code>
     * @see #removeItemListener(ItemListener)
     * @see #addItemListener(ItemListener)
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see #writeObject(ObjectOutputStream)
     */
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
      GraphicsEnvironment.checkHeadless();
      s.defaultReadObject();

      Object keyOrNull;
      while(null != (keyOrNull = s.readObject())) {
        String key = ((String)keyOrNull).intern();

        if (itemListenerK == key)
          addItemListener((ItemListener)(s.readObject()));

        else if (actionListenerK == key)
          addActionListener((ActionListener)(s.readObject()));

        else // skip value for unrecognized key
          s.readObject();
      }
    }
}

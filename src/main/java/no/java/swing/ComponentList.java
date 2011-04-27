/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package no.java.swing;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicListUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A ComponentList is a JList that uses actual JComponents to display and edit it's list items.
 * These components, called editor components, are placed on top of the list items' bounds,
 * more precisely, on top of a custom cell renderer that only paints the cell background.
 * <p/>
 * Please note that the component editors used <em>must</em> be non opaque or you will get repaint issues.
 * <p/>
 * Setting a cell renderer has no effect on a component list.
 *
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 * @author <a href="mailto:alexander@escenic.com">Alexander Thomas</a>.
 * @author <a href="mailto:kristian.nordal@arktekk.no">Kristian Nordal</a>.
 * @see ComponentFactory
 */
//TODO: Figure out why editors are not installed for some elements.
public class ComponentList extends JList {

    public static final String EDITOR_REMOVED_EVENT = "editorRemoved";

    private final ListDataListener modelListener = new ModelListener();
    private final Map<Object, JComponent> editors = new HashMap<Object, JComponent>();
    private final ComponentFactory componentFactory;
    private final FlushableListUI listUI;
    private boolean keepEditors;

    public ComponentList(final ComponentFactory componentFactory) {
        this(new DefaultListModel(), componentFactory);
    }

    public ComponentList(final ListModel listModel, final ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
        super.setCellRenderer(new CellBackgroundRender());
        setModel(listModel);
        listUI = new FlushableListUI();
        setUI(listUI);
        addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(final ListSelectionEvent event) {
                        if (!event.getValueIsAdjusting()) {
                            int index = getIndexOfComponent(KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner());
                            if (index != -1 && !isSelectedIndex(index)) {
                                requestFocusInWindow();
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void setCellRenderer(final ListCellRenderer cellRenderer) {
        // ignore
    }

    @Override
    public void setModel(final ListModel listModel) {
        if (getModel() != null) {
            // uninstall listener and remove any installed editor components
            getModel().removeListDataListener(modelListener);
            for (JComponent editor : editors.values()) {
                remove(editor);
            }
            editors.clear();
        }
        super.setModel(listModel);
        if (listModel != null) {
            // install listener and add editor components for all model values
            listModel.addListDataListener(modelListener);
            for (int index = 0; index < listModel.getSize(); index++) {
                Object value = listModel.getElementAt(index);
                installEditor(value);
            }
        }
    }

    @Override
    public void remove(final Component component) {
        super.remove(component);
        firePropertyChange(EDITOR_REMOVED_EVENT, component, null);
    }

    /**
     * Temporarily disables processing of model removal events in this component list.
     * This is useful when you need to keep your editors spanning multiple model changes.
     * For instance, if you need to swap two elements in your model (remove then insert).
     * Under normal circumstances this would result in the editors for the swapped values
     * being removed then re-installed (thus loosing state).
     * <p/>
     * NB! Please remember to reset immediately after your model changes.
     *
     * @param keepEditors {@code true} to make this list ignore model events changes.
     */
    public void setKeepEditors(boolean keepEditors) {
        boolean oldValue = this.keepEditors;
        this.keepEditors = keepEditors;
        setIgnoreRepaint(this.keepEditors);
        if (oldValue && !this.keepEditors) {
            modelListener.contentsChanged(new ListDataEvent(getModel(), ListDataEvent.CONTENTS_CHANGED, -1, -1));
        }
    }

    private void installEditor(final Object value) {
        if (editors.containsKey(value)) {
            return;
        }
        final JComponent editor = componentFactory.createComponent(value);
        SwingHelper.visitChildren(
                editor,
                new SwingHelper.ComponentVisitor() {
                    public void visit(Component component) {
                        component.addFocusListener(
                                new FocusAdapter() {
                                    @Override
                                    public void focusGained(final FocusEvent event) {
                                        if (!event.isTemporary()) {
                                            int index = getIndexInModel(value);
                                            if (index != -1 && !isSelectedIndex(index)) {
                                                setSelectedIndex(index);
                                            }
                                        }
                                    }
                                }
                        );
                    }
                },
                true
        );
        editors.put(value, editor);
        add(editor);
    }

    /**
     * Returns the index of the specified object in the model.
     *
     * @param value the value to find the index of.
     * @return index of the value in the model or -1 if not found.
     */
    public int getIndexInModel(final Object value) {
        ListModel model = getModel();
        for (int index = 0; index < model.getSize(); index++) {
            if (model.getElementAt(index) == value) {
                return index;
            }
        }
        return -1;
    }

    /**
     * If the given component is a list element, the index of that list element is returned. If the component
     * is descending from any of the list elements, the index of that ancestor list element is returned. Else
     * -1 is returned.
     *
     * @param component component to look up the index of.
     * @return the list index containing the given component, or -1 if not found.
     */
    public int getIndexOfComponent(final Component component) {
        if (component != null && component != this && SwingUtilities.isDescendingFrom(component, this)) {
            Component c = component;
            while (!(c.getParent() instanceof ComponentList)) {
                c = c.getParent();
            }
            for (Map.Entry<Object, JComponent> entry : editors.entrySet()) {
                if (entry.getValue() == c) {
                    return getIndexInModel(entry.getKey());
                }
            }
        }
        return -1;
    }

    @Override
    public void doLayout() {
        ListModel model = getModel();
        for (int index = 0; index < model.getSize(); index++) {
            Object value = model.getElementAt(index);
            JComponent editor = editors.get(value);
            if (editor != null) {
                editor.setBounds(getCellBounds(index, index));                
            }
        }
    }

    @Override
    public void invalidate() {
        // reset BasicListUI's cached cell bounds, so that doLayout will get the correct values from getCellBounds
        listUI.flushCellBounds();
        super.invalidate();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        for (JComponent component : editors.values()) {
            component.setEnabled(enabled);
        }
    }

    public Component getComponentByIndex(final int index) {
        return editors.get(getModel().getElementAt(index));
    }

    private class CellBackgroundRender extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean selected, final boolean focused) {
            super.getListCellRendererComponent(list, null, index, selected, focused);
            // inherit the preferred size of the editor component
            // this is used to calculate the list size and in doLayout()
            // to correctly set the bounds of the editor components.
            JComponent editor = editors.get(value);
            if (editor != null) {
                setPreferredSize(editor.getPreferredSize());                
            }
            return this;
        }

    }

    private class ModelListener implements ListDataListener {

        public void intervalAdded(final ListDataEvent event) {
            // install editors for any new objects in the model
            for (int index = event.getIndex0(); index <= event.getIndex1(); index++) {
                installEditor(getModel().getElementAt(index));
            }
        }

        public void intervalRemoved(final ListDataEvent event) {
            if (!keepEditors) {
                // remove installed editors for any objects no longer in the model
                for (Object value : new HashSet<Object>(editors.keySet())) {
                    if (getIndexInModel(value) == -1) {
                        remove(editors.remove(value));
                    }
                }
            }
        }

        public void contentsChanged(final ListDataEvent event) {
            // remove installed editors for any objects no longer in the model
            for (Object value : new HashSet<Object>(editors.keySet())) {
                if (getIndexInModel(value) == -1) {
                    JComponent comp = editors.remove(value);
                    if (comp != null) {
                        remove(comp);                        
                    }
                }
            }
            // install editors for any new objects in the model
            ListModel model = getModel();
            for (int index = 0; index < model.getSize(); index++) {
                Object value = model.getElementAt(index);
                if (!editors.containsKey(value)) {
                    installEditor(value);
                }
            }
        }

    }

    private static class FlushableListUI extends BasicListUI {

        public void flushCellBounds() {
            updateLayoutStateNeeded = modelChanged;
        }
    }

    /**
     * A component factory creates model views in the form of JComponents for model objects.
     * For example "component factory x" could return a panel for editing a person model object's
     * properties or a check box for a file's read only flag.
     *
     * @see ComponentList
     */
    public interface ComponentFactory {

        /**
         * Create a view component for the provided model object. Implementaions may not reuse
         * a component for two or more model objects.
         *
         * @param modelObject the model object. May not be {@code null}.
         * @return the created component. May not be {@code null}.
         * @throws IllegalArgumentException if the provided model object is invalid.
         */
        JComponent createComponent(final Object modelObject);

    }

}

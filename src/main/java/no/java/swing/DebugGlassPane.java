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

import com.jgoodies.forms.factories.Borders;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class DebugGlassPane extends JComponent implements AWTEventListener {

    private final AbstractAction activationAction;
    private final String shortcut;
    private Component activeComponent;
    private Component oldGlassPane;
    private boolean oldGlassPaneVisibility;

    private DebugGlassPane() {
        MouseHandler mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        activationAction = new ToggleGlassPaneAction();
        shortcut = activationAction.getValue(Action.ACCELERATOR_KEY).toString().replaceAll(" pressed", "");
    }

    @Override
    protected void paintComponent(final Graphics graphics) {

        Graphics2D g = (Graphics2D)graphics;
        String text;

        if (activeComponent != null && !(activeComponent instanceof Window)) {

            Rectangle componentBounds = activeComponent.getBounds();
            if (activeComponent instanceof JComponent) {
                Rectangle visibleRect = ((JComponent)activeComponent).getVisibleRect();
                componentBounds.width = visibleRect.width;
                componentBounds.height = visibleRect.height;
            }
            Rectangle bounds = SwingUtilities.convertRectangle(activeComponent.getParent(), componentBounds, this);
            Insets insets = new Insets(0, 0, 0, 0);
            if (activeComponent instanceof JComponent) {
                insets = ((JComponent)activeComponent).getInsets();
            }

            SwingHelper.enableAA(g);
            SwingHelper.setAlpha(g, .4f);
            g.setColor(Color.PINK);
            g.fillRect(
                    bounds.x + insets.left,
                    bounds.y + insets.top,
                    bounds.width - insets.left - insets.right,
                    bounds.height - insets.top - insets.bottom
            );
            g.setColor(Color.MAGENTA);
            // top border area
            g.fillRect(bounds.x, bounds.y, bounds.width, insets.top);
            // bottom border area
            g.fillRect(bounds.x, bounds.y + bounds.height - insets.bottom, bounds.width, insets.bottom);
            // left border area
            g.fillRect(bounds.x, bounds.y + insets.top, insets.left, bounds.height - insets.top - insets.bottom);
            // right border area
            g.fillRect(bounds.x + bounds.width - insets.right, bounds.y + insets.top, insets.right, bounds.height - insets.top - insets.bottom);

            text = String.format(
                    "%s [%s, %s, %s, %s], name=%s (Press %s to deactivate)",
                    activeComponent.getClass().getName(),
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    activeComponent.getName(),
                    shortcut
            );

        } else {
            text = "Move the mouse over any component. Click to view hierarchy. (Press " + shortcut + " to deactivate)";
        }
        TextLayout layout = new TextLayout(text, g.getFont(), g.getFontRenderContext());
        SwingHelper.setAlpha(g, .6f);
        g.setColor(Color.BLACK);
        Rectangle b = layout.getPixelBounds(g.getFontRenderContext(), 10, 20);
        int padding = 4;
        g.fillRect(b.x - padding, b.y - padding, b.width + padding * 2, b.height + padding * 2);
        SwingHelper.setAlpha(g, 1f);
        g.setColor(Color.WHITE);
        layout.draw(g, 10, 20);

    }

    public void eventDispatched(AWTEvent event) {
        switch (event.getID()) {
            case MouseEvent.MOUSE_MOVED:
                setActiveComponent((Component)event.getSource());
                break;
            default:
        }
    }

    private void setActiveComponent(Component activeComponent) {
        this.activeComponent = activeComponent;
        repaint();
    }

    public static void install(JComponent component) {
        DebugGlassPane debugGlassPane = new DebugGlassPane();
        SwingHelper.bindAction(
                debugGlassPane.activationAction,
                component,
                (KeyStroke)debugGlassPane.activationAction.getValue(Action.ACCELERATOR_KEY),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private class ToggleGlassPaneAction extends AbstractAction {

        private ToggleGlassPaneAction() {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(ResourceBundleLoader.load().getString("DebugGlassPane.keyStroke"));
            if (keyStroke == null) {
                keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK);
            }
            System.err.println("Press \"" + keyStroke.toString().replaceAll(" pressed", "") + "\" to activate/deactivate debug glasspane");
            putValue(ACCELERATOR_KEY, keyStroke);
        }

        public void actionPerformed(ActionEvent event) {
            JRootPane rootPane = SwingUtilities.getRootPane((Component)event.getSource());
            if (rootPane.getGlassPane() == DebugGlassPane.this) {
                rootPane.setGlassPane(oldGlassPane);
                oldGlassPane.setVisible(oldGlassPaneVisibility);
            } else {
                oldGlassPane = rootPane.getGlassPane();
                oldGlassPaneVisibility = oldGlassPane.isVisible();
                rootPane.setGlassPane(DebugGlassPane.this);
                DebugGlassPane.this.setVisible(true);
                DebugGlassPane.this.repaint();
            }
        }

    }

    private class MouseHandler extends MouseInputAdapter {

        private JTree tree;

        @Override
        public void mouseMoved(MouseEvent e) {
            Component componentUnderMouse = SwingUtilities.getDeepestComponentAt(
                    SwingUtilities.getRootPane(DebugGlassPane.this).getContentPane(),
                    e.getX(),
                    e.getY()
            );
            if (activeComponent != componentUnderMouse) {
                setActiveComponent(componentUnderMouse);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setActiveComponent(null);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (tree != null) {
                List<Component> parents = new ArrayList<Component>();
                for (Component c = activeComponent; c != null; c = c.getParent()) {
                    parents.add(0, c);
                }
                tree.setSelectionPath(new TreePath(parents.toArray()));
                return;
            }
            if (activeComponent != null) {
                tree = new JTree(new ComponentHierarchyModel(activeComponent)) {
                    @Override
                    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                        Component component = (Component)value;
                        return String.format(
                                "%s, name=%s",
                                component.getClass().getName(),
                                component.getName()
                        );
                    }
                };
                List<Component> parents = new ArrayList<Component>();
                for (Component c = activeComponent; c != null; c = c.getParent()) {
                    parents.add(0, c);
                }
                tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                tree.setSelectionPath(new TreePath(parents.toArray()));
                tree.setShowsRootHandles(true);
                tree.getSelectionModel().addTreeSelectionListener(
                        new TreeSelectionListener() {
                            public void valueChanged(TreeSelectionEvent e) {
                                setActiveComponent((Component)e.getPath().getLastPathComponent());
                            }
                        }
                );
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(Borders.DIALOG_BORDER);
                panel.add(new JScrollPane(tree));

                final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(activeComponent), "Component Hierarchy", Dialog.ModalityType.MODELESS);
                SwingHelper.bindAction(
                        new AbstractAction() {
                            public void actionPerformed(ActionEvent e) {
                                dialog.setVisible(false);
                            }
                        },
                        tree,
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        WHEN_IN_FOCUSED_WINDOW
                );
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setContentPane(panel);
                dialog.pack();
                dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(activeComponent));
                dialog.addWindowListener(
                        new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                tree = null;
                            }
                        }
                );
                dialog.setVisible(true);
            }
        }

    }

    private static class ComponentHierarchyModel implements TreeModel {

        private final Component component;

        public ComponentHierarchyModel(Component component) {
            this.component = component;
        }

        public Object getRoot() {
            return SwingUtilities.getWindowAncestor(component);
        }

        public Object getChild(Object parent, int index) {
            return ((Container)parent).getComponent(index);
        }

        public int getChildCount(Object parent) {
            return parent instanceof Container ? ((Container)parent).getComponentCount() : 0;
        }

        public boolean isLeaf(Object node) {
            return !(node instanceof Container) || ((component instanceof Container && getChildCount(node) == 0));
        }

        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        public int getIndexOfChild(Object parent, Object child) {
            if (parent instanceof Container) {
                Component[] children = ((Container)parent).getComponents();
                for (int index = 0; index < children.length; index++) {
                    Component tmp = children[index];
                    if (tmp.equals(child)) {
                        return index;
                    }
                }
            }
            return -1;
        }

        public void addTreeModelListener(TreeModelListener listener) {
        }

        public void removeTreeModelListener(TreeModelListener listener) {
        }

    }

}

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

import org.apache.commons.lang.Validate;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.rmi.server.UID;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A collection of Swing related utilities.
 *
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class SwingHelper {

    private SwingHelper() {
    }

    public static void setTabFocusTraversalKeys(final JComponent component) {
        component.setFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                new HashSet<AWTKeyStroke>(Arrays.asList(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)))
        );
        component.setFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                new HashSet<AWTKeyStroke>(
                        Arrays.asList(
                                KeyStroke.getKeyStroke(
                                        KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK
                                )
                        )
                )
        );
    }

    /**
     * Returns a new JScrollPane with the specified component and with an empty border.
     *
     * @param component the component to add to the scroll pane.
     * @return a new scroll pane.
     */
    public static JScrollPane borderlessScrollPane(final Component component) {
        JScrollPane pane = new JScrollPane(component);
        pane.setBorder(BorderFactory.createEmptyBorder());
        return pane;
    }

    /**
     * Binds an action to a component with the specified key stroke and conditions.
     *
     * @param action     the action to bind.
     * @param component  taget component.
     * @param keyStroke  the key stroke for the bining.
     * @param conditions any combination of
     *                   {@link javax.swing.JComponent#WHEN_FOCUSED},
     *                   {@link javax.swing.JComponent#WHEN_ANCESTOR_OF_FOCUSED_COMPONENT} or
     *                   {@link javax.swing.JComponent#WHEN_IN_FOCUSED_WINDOW}.
     *                   When empty, {@link javax.swing.JComponent#WHEN_FOCUSED} is used.
     * @throws IllegalArgumentException if either parameter is <code>null</code>.
     */
    public static void bindAction(final Action action, final JComponent component, final KeyStroke keyStroke, final int... conditions) {
        Validate.notNull(action, "action can not be null");
        Validate.notNull(component, "component can not be null");
        Validate.notNull(keyStroke, "keyStroke can not be null");
        String key = new UID().toString();
        for (int condition : conditions) {
            component.getInputMap(condition).put(keyStroke, key);
        }
        if (conditions.length == 0) {
            component.getInputMap().put(keyStroke, key);
        }
        component.getActionMap().put(key, action);
    }

    /**
     * Visits all components in the component hierarchy <em>below</em> the specified container, optionally including the root itself.
     *
     * @param container   container to visit children of.
     * @param visitor     visitor callback.
     * @param includeRoot when <code>true</code>, the root container is also visited.
     * @see SwingHelper#visitParents(java.awt.Component, no.java.swing.SwingHelper.ComponentVisitor, boolean)
     */
    public static void visitChildren(final Container container, final ComponentVisitor visitor, final boolean includeRoot) {
        if (includeRoot) {
            visitor.visit(container);
        }
        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                visitChildren((Container) component, visitor, true);
            } else {
                visitor.visit(component);
            }
        }
    }

    /**
     * Visits all ancestors in the component hierarchy <em>above</em> the specified container, optionally including the root itself.
     *
     * @param container   container to visit ancestors of.
     * @param visitor     visitor callback.
     * @param includeRoot when <code>true</code>, the root container is also visited.
     * @see SwingHelper#visitChildren(java.awt.Container, no.java.swing.SwingHelper.ComponentVisitor, boolean)
     */
    public static void visitParents(final Component container, final ComponentVisitor visitor, final boolean includeRoot) {
        if (includeRoot) {
            visitor.visit(container);
        }
        if (container.getParent() != null) {
            visitParents(container.getParent(), visitor, true);
        }
    }

    public static void displayErrorMessage(final Throwable throwable, final Component parentComponent) {
        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));
        JTextArea textArea = new JTextArea(stackTrace.toString(), 25, 100);
        textArea.setEditable(false);
        textArea.setTabSize(4);
        textArea.setForeground(Color.RED);
        textArea.setFont(textArea.getFont().deriveFont(12f));
        JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(parentComponent),
                new JScrollPane(textArea),
                "Unhandled Error",
                JOptionPane.PLAIN_MESSAGE,
                null
        );
    }

    public static int getPreferredTableWidth(final JTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        int width = 0;
        for (int col = 0; col < columnModel.getColumnCount(); col++) {
            width += columnModel.getColumn(col).getPreferredWidth();
            if (col != 0) {
                width += columnModel.getColumnMargin();
            }
        }
        return width;
    }

    public static void setMinimumColumnWidths(final JTable table, final int... widths) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < widths.length; col++) {
            columnModel.getColumn(col).setMinWidth(widths[col]);
        }
    }

    public static void setPreferredColumnWidths(final JTable table, final int... widths) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < widths.length; col++) {
            columnModel.getColumn(col).setPreferredWidth(widths[col]);
        }
    }

    public static void setMaxnimumColumnWidths(final JTable table, final int... widths) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < widths.length; col++) {
            columnModel.getColumn(col).setMaxWidth(widths[col]);
        }
    }

    public static void enableAA(final Graphics graphics) {
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static void setAlpha(final Graphics graphics, final float alpha) {
        ((Graphics2D) graphics).setComposite(AlphaComposite.SrcOver.derive(alpha));
    }

    public static String getDocumentText(final Document document) {
        try {
            return document.getText(0, document.getLength());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Reads an icon resource from the class path.
     * <p/>
     * The icon is read using the default <tt>ClassLoader</tt>. Only absolute
     * paths are used.
     *
     * @param pPath the path to the icon resource.
     * @return an <tt>Icon</tt> containing the image at the given path
     * @throws IllegalArgumentException if <tt>pPath</tt> is empty or no image is
     *                                  found at the path.
     * @see #readIcon(String, Class)
     */
    public static Icon readIcon(final String pPath) {
        return readIcon(pPath, null);
    }

    /**
     * Reads an icon resource from the class path.
     * <p/>
     * The icon is read using the <tt>ClassLoader</tt> of the <tt>pRelativeTo</tt>
     * parameter if given. The path may be relative to (in the same package as)
     * the given class, or absolute (relative to the class path root). The path is
     * considered absolute if it starts with a '/'. If <tt>pRelativeTo</tt> is
     * <tt>null</tt>, only absolute paths are used.
     *
     * @param pPath       the path to the icon resource, may be absolute or
     *                    relative to the <tt>pRelativeTo</tt> parameter.
     * @param pRelativeTo the <tt>Class</tt> to get the resource from, or
     *                    <tt>null</tt>.
     * @return an <tt>Icon</tt> containing the image at the given path
     * @throws IllegalArgumentException if <tt>pPath</tt> is empty or no image is
     *                                  found at the path.
     */
    public static <T> Icon readIcon(final String pPath, final Class<T> pRelativeTo) {
        Validate.notEmpty(pPath, "Path can not be empty.");

        URL resource;

        // TODO: Either be lenient (like now), or disallow relative paths if relativeTo == null
        // TODO: Consider defaulting to the context classloader?

        if (pRelativeTo != null) {
            // Relative to the class object passed in. Leading '/' allowed
            resource = pRelativeTo.getResource(pPath);
        } else {
            // This is always relative to root, so we can safely strip off leading '/'
            String path = pPath.startsWith("/") ? pPath.substring(1) : pPath;
            resource = SwingHelper.class.getClassLoader().getResource(path);
        }

        Validate.notNull(resource, "No icon found for path: \"" + pPath + "\"");

        return new ImageIcon(resource);
    }

    public static JLabel createLabel(final String textWithMnemonic, final JComponent labelForComponent) {
        TextWithMnemonic parsedTextWithMnemonic = new TextWithMnemonic(textWithMnemonic);
        JLabel label = new JLabel(parsedTextWithMnemonic.getTextWithoutMnemonic());
        if (parsedTextWithMnemonic.getMnemonic() != null) {
            label.setDisplayedMnemonic(parsedTextWithMnemonic.getMnemonic());
            label.setDisplayedMnemonicIndex(parsedTextWithMnemonic.getMnemonicIndex());
        }
        if (labelForComponent != null) {
            label.setLabelFor(labelForComponent);
        }
        return label;
    }

    /**
     * Visitor callback used by the <em>visitor</em> methods.
     *
     * @see SwingHelper#visitParents(java.awt.Component, no.java.swing.SwingHelper.ComponentVisitor, boolean)
     * @see SwingHelper#visitChildren(java.awt.Container, no.java.swing.SwingHelper.ComponentVisitor, boolean)
     */
    public interface ComponentVisitor {
        void visit(final Component component);
    }

    public static void installSimpleRenderesAndEditors(JTable table) {
        Map<Class, Class> map = new HashMap<Class, Class>();
        map.put(boolean.class, Boolean.class);
        map.put(byte.class, Number.class);
        map.put(short.class, Number.class);
        map.put(int.class, Number.class);
        map.put(long.class, Number.class);
        map.put(float.class, Number.class);
        map.put(double.class, Number.class);
        for (Class newType : map.keySet()) {
            Class existingType = map.get(newType);
            table.setDefaultRenderer(newType, table.getDefaultRenderer(existingType));
            table.setDefaultEditor(newType, table.getDefaultEditor(existingType));
        }
    }
}

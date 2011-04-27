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

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.PropertyHelper;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * todo: pressing "," should add a space at the end if a value was selected
 * todo: make reusable: remove text field from constructor and add install() method
 *
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class AutoCompleter<T> {

    private static final Pattern PATTERN = Pattern.compile("([^,;]+)[,;]*");
    private final JTextField textField;
    private final SuggestionTable suggestionTable;
    private final JPopupMenu popupMenu;
    private final Timer timer;
    private final ObservableList<T> model;

    public AutoCompleter(final JTextField textField, final ObservableList<T> model) {
        Validate.notNull(textField, "Component may not be null");
        Validate.notNull(model, "Model may not be null");
        this.textField = textField;
        this.model = model;
        suggestionTable = new SuggestionTable();
        JTableBinding<T, List<T>, JTable> tableBinding = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ, model, suggestionTable);
        tableBinding.addColumnBinding(new ColumnBinding()).setColumnClass(String.class).setEditable(false);
        tableBinding.bind();
        suggestionTable.setAutoCreateRowSorter(true);
        suggestionTable.getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        JScrollPane scrollPane = new JScrollPane(suggestionTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width = textField.getWidth();
                return size;
            }
        };
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.LIGHT_GRAY));
        popupMenu = new JPopupMenu();
        popupMenu.setBorder(BorderFactory.createEmptyBorder());
        popupMenu.add(scrollPane);
        popupMenu.setFocusable(false);
        timer = new Timer(150, new Suggestor());
        timer.setRepeats(false);
        SwingHelper.bindAction(
                new AbstractAction() {
                    public void actionPerformed(final ActionEvent event) {
                        suggestionTable.getActionMap().get("selectPreviousRow").actionPerformed(
                                new ActionEvent(suggestionTable, ActionEvent.ACTION_PERFORMED, null)
                        );
                    }
                },
                textField,
                KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)
        );
        SwingHelper.bindAction(
                new AbstractAction() {
                    public void actionPerformed(final ActionEvent event) {
                        suggestionTable.getActionMap().get("selectNextRow").actionPerformed(
                                new ActionEvent(suggestionTable, ActionEvent.ACTION_PERFORMED, null)
                        );
                    }
                },
                textField,
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)
        );
        SwingHelper.bindAction(
                new AbstractAction() {
                    public void actionPerformed(final ActionEvent event) {
                        if (popupMenu.isVisible()) {
                            popupMenu.setVisible(false);
                        } else {
                            escapePressed();
                        }
                    }
                },
                textField,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)
        );
        textField.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent event) {
                        if (!selectValue()) {
                            JButton defaultButton = textField.getRootPane().getDefaultButton();
                            if (defaultButton != null) {
                                defaultButton.doClick();
                            }
                        }
                    }
                }
        );
        textField.addFocusListener(
                new FocusAdapter() {
                    @Override
                    public void focusLost(final FocusEvent e) {
                        popupMenu.setVisible(false);
                    }
                }
        );
        textField.getDocument().addDocumentListener(
                new DocumentAdapter() {
                    protected void documentChanged(final DocumentEvent event) {
                        if (timer.isRunning()) {
                            timer.stop();
                        }
                        timer.start();
                    }
                }
        );
        SwingHelper.bindAction(
                new AbstractAction() {
                    public void actionPerformed(final ActionEvent event) {
                        selectValue();
                    }
                },
                textField,
                KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0)
        );
    }

    protected void escapePressed() {
    }

    public JTextField getTextField() {
        return textField;
    }

    protected String displayValue(final T value) {
        return value.toString();
    }

    protected String suggestPart() {
        String text = getTextField().getText();
        Matcher matcher = PATTERN.matcher(text);
        int caret = getTextField().getCaretPosition();
        while (matcher.find()) {
            if (caret >= matcher.start(1) && caret <= matcher.end(1)) {
                return matcher.group(1).trim();
            }
        }
        return text;
    }

    protected void valueSelected(final T value) {
        String text = getTextField().getText();
        Matcher matcher = PATTERN.matcher(text);
        int caret = getTextField().getCaretPosition();
        while (matcher.find()) {
            if (caret >= matcher.start(1) && caret <= matcher.end(1)) {
                getTextField().select(matcher.start(), matcher.end(1));
                String replacement = displayValue(value);
                if (getTextField().getSelectionStart() != 0) {
                    replacement = " " + replacement;
                }
                getTextField().replaceSelection(replacement);
                return;
            }
        }
        getTextField().setText(displayValue(value));
    }

    private boolean selectValue() {
        if (popupMenu.isVisible()) {
            popupMenu.setVisible(false);
            int index = suggestionTable.getSelectionModel().getMinSelectionIndex();
            if (index != -1) {
                valueSelected(model.get(suggestionTable.getRowSorter().convertRowIndexToModel(index)));
            }
            timer.stop();
            return true;
        }
        return false;
    }

    private class SuggestionTable extends JTable {

        public SuggestionTable() {
            setIntercellSpacing(new Dimension());
            setShowGrid(false);
            setFocusable(false);
            addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseReleased(final MouseEvent event) {
                            if (getVisibleRect().contains(event.getPoint())) {
                                selectValue();
                            }
                        }
                    }
            );
            setFont(textField.getFont());
            if (!SystemUtils.IS_OS_UNIX || SystemUtils.IS_OS_MAC) {
                HighlightingCellRenderer cellRenderer = new HighlightingCellRenderer(new Color(0xffff99));
                setDefaultRenderer(String.class, cellRenderer);
                setRowHeight(cellRenderer.getPreferredRowHeight(this));
            }
            getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            CycleSelectionAction.install(this);
        }

        @Override
        protected void configureEnclosingScrollPane() {
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            Dimension size = super.getPreferredScrollableViewportSize();
            size.height = Math.min(getRowHeight() * 10, getRowHeight() * getRowSorter().getViewRowCount());
            return size;
        }

    }

    private class Suggestor implements ActionListener {

        public void actionPerformed(final ActionEvent event) {
            if (textField.hasFocus()) {
                String filter = suggestPart();
                String regexp = "(?i)" + Pattern.quote(filter);
                suggestionTable.putClientProperty(
                        HighlightingCellRenderer.HIGHLIGHT_PATTERN_PROPERTY,
                        filter.length() > 0 ? Pattern.compile(regexp) : null
                );
                ((TableRowSorter<TableModel>)suggestionTable.getRowSorter()).setRowFilter(RowFilter.regexFilter(regexp));
                popupMenu.pack();
                if (suggestionTable.getRowSorter().getViewRowCount() > 0 && filter.length() > 0) {
                    if (!popupMenu.isVisible()) {
                        popupMenu.show(textField, 0, textField.getHeight());
                    }
                    int index = suggestionTable.getSelectionModel().getMinSelectionIndex();
                    if (index == -1) {
                        suggestionTable.getSelectionModel().setSelectionInterval(0, 0);
                    }
                    suggestionTable.scrollRectToVisible(suggestionTable.getCellRect(index, 0, true));
                } else {
                    popupMenu.setVisible(false);
                }
            }
        }

    }

    private class ColumnBinding extends PropertyHelper<T, String> {

        public Class<? extends String> getWriteType(final T source) {
            return String.class;
        }

        public String getValue(final T source) {
            return displayValue(source);
        }

        public void setValue(final T source, final String value) {
        }

        public boolean isReadable(final T source) {
            return true;
        }

        public boolean isWriteable(final T source) {
            return false;
        }

    }

}

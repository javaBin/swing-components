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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

/**
 * @author <a href="mailto:kristian.nordal@arktekk.no">Kristian Nordal</a>.
 * @author <a href="mailto:yngvars@gmail.no">Yngvar S&oslash;rensen</a>
 */
public class SingleDock extends JPanel {

    public static final String EXPANDED_PROPERTY = "expanded";

    public enum Position {

        Top(BorderLayout.SOUTH, Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR), true, 0, 2),
        Bottom(BorderLayout.NORTH, Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR), true, 2, 0),
        Left(BorderLayout.EAST, Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR), false, -1, 1),
        Right(BorderLayout.WEST, Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR), false, 1, -1);

        final Object resizeBarConstraints;
        final Cursor resizeBarCursor;
        final boolean horizontal;
        final int sq;
        final int hq;

        Position(final Object resizeBarConstraints, final Cursor resizeBarCursor, final boolean horizontal, final int sq, final int hq) {
            this.resizeBarConstraints = resizeBarConstraints;
            this.resizeBarCursor = resizeBarCursor;
            this.horizontal = horizontal;
            this.sq = sq;
            this.hq = hq;
        }

    }

    private final Position position;
    private final JPanel contents;
    private final JPanel resizeBar;
    private final JButton showHideButton;
    private Icon showIcon;
    private Icon showIconRollover;
    private Icon hideIcon;
    private Icon hideIconRollover;
    private boolean resizing;
    private boolean respectMinimumSize;

    /**
     * Creates a new single dock.
     *
     * @param name     identifier of the dock (used for session storage). Must be unique within the application.
     * @param position intended placement of the dock.
     * @param contents dock content.
     */
    public SingleDock(final String name, final Position position, final JComponent contents) {
        this.position = position;
        this.contents = new JPanel(new BorderLayout()) {
            @Override
            public void setVisible(boolean visible) {
                if (!resizing && visible && !isVisible()) {
                    if (position.horizontal && getPreferredSize().height < getLayout().preferredLayoutSize(this).height) {
                        setPreferredSize(null);
                    }
                    if (!position.horizontal && getPreferredSize().width < getLayout().preferredLayoutSize(this).width) {
                        setPreferredSize(null);
                    }
                }
                super.setVisible(visible);
            }
        };
        this.contents.add(contents, BorderLayout.CENTER);
        showHideButton = new JButton();
        showHideButton.setBorder(BorderFactory.createEmptyBorder());
        showHideButton.setContentAreaFilled(false);
        showHideButton.setFocusable(false);
        showHideButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showHideButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent event) {
                        setExpanded(!isExpanded());
                    }
                }
        );
        int arrowSize = 9;
        showIcon = new ArrowIcon(arrowSize, getBackground().darker(), position.sq);
        hideIcon = new ArrowIcon(arrowSize, getBackground().darker(), position.hq);
        showIconRollover = new ArrowIcon(arrowSize, Color.ORANGE, position.sq);
        hideIconRollover = new ArrowIcon(arrowSize, Color.ORANGE, position.hq);
        ResizeHandler resizeHandler = new ResizeHandler();
        resizeBar = new JPanel(position.horizontal ? new FormLayout("0:g,p,0:g", "max(4;p)") : new FormLayout("max(4;p)", "0:g,p,0:g"));
        resizeBar.add(showHideButton, position.horizontal ? new CellConstraints(2, 1) : new CellConstraints(1, 2));
        resizeBar.setCursor(this.position.resizeBarCursor);
        resizeBar.addMouseListener(resizeHandler);
        resizeBar.addMouseMotionListener(resizeHandler);
        setLayout(new BorderLayout());
        add(this.contents, BorderLayout.CENTER);
        add(resizeBar, position.resizeBarConstraints);
        setName(name);
        syncToggleButtonState();
    }

    public boolean isExpanded() {
        return contents.isVisible();
    }

    public void setExpanded(final boolean expanded) {
        boolean wasExpanded = isExpanded();
        contents.setVisible(expanded);
        firePropertyChange(EXPANDED_PROPERTY, wasExpanded, isExpanded());
        syncToggleButtonState();
    }

    public Icon getShowIcon() {
        return showIcon;
    }

    public void setShowIcon(final Icon showIcon) {
        this.showIcon = showIcon;
        syncToggleButtonState();
    }

    public Icon getShowIconRollover() {
        return showIconRollover;
    }

    public void setShowIconRollover(final Icon showIconRollover) {
        this.showIconRollover = showIconRollover;
        syncToggleButtonState();
    }

    public Icon getHideIcon() {
        return hideIcon;
    }

    public void setHideIcon(final Icon hideIcon) {
        this.hideIcon = hideIcon;
        syncToggleButtonState();
    }

    public Icon getHideIconRollover() {
        return hideIconRollover;
    }

    public void setHideIconRollover(final Icon hideIconRollover) {
        this.hideIconRollover = hideIconRollover;
        syncToggleButtonState();
    }

    /**
     * Returns the single dock's content panel. This is useful if you want to customize it; e.g. set a border.
     *
     * @return the single dock's content panel.
     */
    public JComponent getContents() {
        return contents;
    }

    /**
     * Returns the single dock's resize bar. This is useful if you want to customize it; e.g. change color.
     *
     * @return the single dock's resize bar.
     */
    public JComponent getResizeBar() {
        return resizeBar;
    }

    /**
     * Returns {@code false} if the dock allows resizing bellow the content's {@link javax.swing.JComponent#getMinimumSize() minimum size}.
     *
     * @return {@code false} (default) if the doc is not respecting the content's {@link javax.swing.JComponent#getMinimumSize() minimum size}.
     */
    public boolean isRespectMinimumSize() {
        return respectMinimumSize;
    }

    /**
     * Specifies wether the dock allows resizing bellow the content's {@link javax.swing.JComponent#getMinimumSize() minimum size}.
     *
     * @param respectMinimumSize when {@code true} the content can not be resized smaller the content's {@link javax.swing.JComponent#getMinimumSize() minimum size}.
     */
    public void setRespectMinimumSize(final boolean respectMinimumSize) {
        this.respectMinimumSize = respectMinimumSize;
    }

    private void syncToggleButtonState() {
        showHideButton.setIcon(contents.isVisible() ? hideIcon : showIcon);
        showHideButton.setRolloverIcon(
                contents.isVisible() ?
                (hideIconRollover == null ? hideIcon : hideIconRollover) :
                (showIconRollover == null ? showIcon : showIconRollover)
        );
    }

    private class ResizeHandler extends MouseAdapter {

        private Point startingPoint;
        private int width;
        private int height;

        @Override
        public void mousePressed(final MouseEvent event) {
            resizing = true;
            startingPoint = event.getLocationOnScreen();
            width = contents.getWidth();
            height = contents.getHeight();
            if (position.horizontal && !contents.isVisible()) {
                height = 0;
            }
            if (!position.horizontal && !contents.isVisible()) {
                width = 0;
            }
        }

        @Override
        public void mouseReleased(final MouseEvent event) {
            resizing = false;
        }

        @Override
        public void mouseDragged(final MouseEvent event) {
            Point currentPoint = event.getLocationOnScreen();
            int dx = currentPoint.x - startingPoint.x;
            int dy = currentPoint.y - startingPoint.y;
            Dimension minimumSize = contents.getMinimumSize();
            int minWidth = respectMinimumSize ? minimumSize.width : 0;
            int minHeight = respectMinimumSize ? minimumSize.height : 0;
            int width = position.horizontal ? this.width : Math.max(minWidth, this.width + dx * (position == Position.Right ? -1 : 1));
            int height = position.horizontal ? Math.max(minHeight, this.height + dy * (position == Position.Bottom ? -1 : 1)) : this.height;
            contents.setPreferredSize(new Dimension(width, height));
            contents.revalidate();
            setExpanded(position.horizontal ? height > 0 : width > 0);
        }

        @Override
        public void mouseClicked(final MouseEvent event) {
            if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
                contents.setPreferredSize(null);
                if (position.horizontal) {
                    contents.setPreferredSize(new Dimension(0, contents.getPreferredSize().height));
                } else {
                    contents.setPreferredSize(new Dimension(contents.getPreferredSize().width, 0));
                }
                contents.revalidate();
            }
        }
    }

    public static class ArrowIcon implements Icon {

        private final int size;
        private final Color color;
        private final Shape arrow;

        public ArrowIcon(final int size, final Color color, final int numquadrants) {
            this.size = size;
            this.color = color;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(0.0, size * 0.2);
            path.lineTo(size, size * 0.2);
            path.lineTo(size / 2.0, size * 0.8);
            path.lineTo(0.0, size * 0.2);
            arrow = path.createTransformedShape(AffineTransform.getQuadrantRotateInstance(numquadrants, size / 2.0, size / 2.0));
        }

        public void paintIcon(final Component component, final Graphics graphics, final int x, final int y) {
            Graphics2D g = (Graphics2D)graphics;
            RenderingHints hints = g.getRenderingHints();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.translate(x, y);
            g.fill(arrow);
            g.translate(-x, -y);
            g.setRenderingHints(hints);
        }

        public int getIconWidth() {
            return size;
        }

        public int getIconHeight() {
            return size;
        }

    }
}

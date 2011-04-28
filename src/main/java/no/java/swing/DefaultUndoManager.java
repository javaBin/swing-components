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


import no.java.swing.resource.ResourceBundleLoader;
import no.java.swing.resource.ResourceMap;
import no.java.swing.resource.ResourceMapLoader;

import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

/**
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 */
public final class DefaultUndoManager extends UndoManager {

    private static final Map<Object, DefaultUndoManager> INSTANCES = new WeakHashMap<Object, DefaultUndoManager>() {
        @Override
        public synchronized DefaultUndoManager get(final Object key) {
            DefaultUndoManager undoManager = super.get(key);
            if (undoManager == null) {
                undoManager = new DefaultUndoManager();
                put(key, undoManager);
            }
            return undoManager;
        }
    };
    private final Action undoAction;
    private final Action redoAction;
    private boolean undoingOrRedoing;

    private DefaultUndoManager() {
        ResourceMap resourceMap = ResourceMapLoader.getDefault();
        undoAction = new UndoAction(resourceMap);
        redoAction = new RedoAction(resourceMap);
        updateUndoRedoActionStates();
    }

    public static DefaultUndoManager getInstance(final Object context) {
        return INSTANCES.get(context);
    }

    public static DefaultUndoManager getComponentUndoManager(final Component component) {
        Component parent = component;
        while (parent != null && !INSTANCES.containsKey(parent)) {
            parent = parent.getParent();
        }
        return parent == null ? getInstance(null) : INSTANCES.get(parent);
    }

    public boolean isUndoingOrRedoing() {
        return undoingOrRedoing;
    }

    public Action getUndoAction() {
        return undoAction;
    }

    public Action getRedoAction() {
        return redoAction;
    }

    @Override
    public boolean addEdit(final UndoableEdit edit) {
        if (undoingOrRedoing) {
            System.err.println("Call to DefaultUndoManager.addEdit() ignored while undoing or redoing: " + edit);
            return false;
        }
        boolean success = super.addEdit(edit);
        updateUndoRedoActionStates();
        return success;
    }

    @Override
    public void discardAllEdits() {
        super.discardAllEdits();
        updateUndoRedoActionStates();
    }

    @Override
    public synchronized void undo() throws CannotUndoException {
        try {
            undoingOrRedoing = true;
            super.undo();
        } finally {
            undoingOrRedoing = false;
        }
    }

    @Override
    public synchronized void redo() throws CannotRedoException {
        try {
            undoingOrRedoing = true;
            super.redo();
        } finally {
            undoingOrRedoing = false;
        }
    }

    private void updateUndoRedoActionStates() {
        undoAction.setEnabled(canUndo());
        undoAction.putValue(Action.SHORT_DESCRIPTION, (canUndo() ? editToBeUndone().getUndoPresentationName() : null));
        redoAction.setEnabled(canRedo());
        redoAction.putValue(Action.SHORT_DESCRIPTION, (canRedo() ? editToBeRedone().getRedoPresentationName() : null));
    }

    private class UndoAction extends ConfiguredAction {

        public UndoAction(ResourceMap resourceMap) {
            super("undo", resourceMap);
        }

        public void actionPerformed(final ActionEvent event) {
            undo();
            updateUndoRedoActionStates();

        }

    }

    private class RedoAction extends ConfiguredAction {

        public RedoAction(ResourceMap resourceMap) {
            super("redo", resourceMap);
        }

        public void actionPerformed(final ActionEvent event) {
            redo();
            updateUndoRedoActionStates();
        }

    }

}

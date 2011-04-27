/*
 * Copyright 2011 javaBin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.java.swing;

import org.apache.commons.lang.SystemUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class SingleSelectionFileDialog {
    private static File previousDirectory = SystemUtils.getUserDir();

    private File selected;
    private FileFilter filter;
    private boolean rememberPreviousLocation = true;

    public File getSelectedFile() {
        return selected;
    }

    public void setFileFilter(FileFilter filter) {
        this.filter = filter;
    }

    public FileFilter getFilter() {
        return filter;
    }

    public boolean isRememberPreviousLocation() {
        return rememberPreviousLocation;
    }

    public void setRememberPreviousLocation(boolean rememberPreviousLocation) {
        this.rememberPreviousLocation = rememberPreviousLocation;
    }

    public Result showOpenDialog(Component target) {
        if (SystemUtils.IS_OS_MAC_OSX) {
            return showNativeDialog(target, true);
        }
        return showJFileChooser(target, true);
    }

    public Result showSaveDialog(Component target) {
        if (SystemUtils.IS_OS_MAC_OSX) {
            return showNativeDialog(target, false);
        }
        return showJFileChooser(target, false);
    }

    private Result showJFileChooser(Component target, boolean open) {
        JFileChooser chooser = new JFileChooser(previousDirectory);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(filter == null);
        chooser.setDialogType(open ? JFileChooser.OPEN_DIALOG : JFileChooser.SAVE_DIALOG);
        int selection = chooser.showDialog(target, null);

        switch (selection) {
            case JFileChooser.APPROVE_OPTION:
                this.selected = chooser.getSelectedFile();
                if (rememberPreviousLocation) {
                    previousDirectory = chooser.getCurrentDirectory();
                }
                return Result.APPROVE;

            case JFileChooser.CANCEL_OPTION:
            case JFileChooser.ERROR_OPTION:
            default:
                this.selected = null;
                return Result.ERROR;
        }
    }

    private Result showNativeDialog(Component target, boolean open) {
        Window window = target == null ? null : (Frame) SwingUtilities.getWindowAncestor(target);
        FileDialog dialog;
        if (window instanceof Frame) {
            dialog = new FileDialog((Frame) window);
        } else if (window instanceof Dialog) {
            dialog = new FileDialog((Dialog) window);
        } else {
            throw new IllegalStateException("Unknown window type");
        }
        dialog.setDirectory(previousDirectory.getAbsolutePath());
        dialog.setMode(open ? FileDialog.LOAD : FileDialog.SAVE);
        dialog.setVisible(true);
        if (rememberPreviousLocation) {
            previousDirectory = new File(dialog.getDirectory());
        }
        if (dialog.getFile() == null) {
            return Result.CANCEL;
        }
        selected = new File(dialog.getFile());
        return Result.APPROVE;
    }


    public enum Result {
        APPROVE,
        CANCEL,
        ERROR
    }
}

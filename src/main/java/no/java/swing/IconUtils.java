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
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IconUtils {

    private static final Map<String, Icon> FILE_EXTENSION_ICON_CACHE = Collections.synchronizedMap(new HashMap<String, Icon>());

    /**
     * Tries to find the native system icon for a file extension.
     *
     * @param extension file extension to find an icon for. For example <code>".jpg"</code>. May not be {@code null}.
     * @param large     when {@code true} an attempt to fetch a large icon is made.
     * @return the system spesific icon or {@code null} if no icon is found.
     */
    public static Icon getIconForExtension(final String extension, final boolean large) {
        Validate.notNull(extension, "Extension may not be null");
        synchronized (FILE_EXTENSION_ICON_CACHE) {
            String key = large + ":" + extension;
            Icon icon = FILE_EXTENSION_ICON_CACHE.get(key);
            if (icon != null) {
                return icon;
            }
            File tempFile = null;
            try {
                tempFile = File.createTempFile("icon", extension);
                icon = getIconForFile(tempFile, large);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (tempFile != null) {
                    if (!tempFile.delete()) {
                        tempFile.deleteOnExit();
                    }
                }
            }
            if (icon != null) {
                FILE_EXTENSION_ICON_CACHE.put(key, icon);
            }
            return icon;
        }
    }

    /**
     * Tries to find the native system icon for a file.
     *
     * @param file  file to find an icon for. May not be {@code null}.
     * @param large when {@code true} an attempt to fetch a large icon is made.
     * @return the system spesific icon or {@code null} if no icon is found.
     */
    public static Icon getIconForFile(final File file, final boolean large) {
        Validate.notNull(file, "File may not be null");
        if (!file.exists()) {
            return getIconForExtension(getExtension(file), large);
        }
        Icon icon = null;
        try {
            Method getShellFolderMethod = Class.forName("sun.awt.shell.ShellFolder").getMethod("getShellFolder", File.class);
            getShellFolderMethod.setAccessible(true);
            Object shellFolder = getShellFolderMethod.invoke(null, file);
            Method getIconMethod = shellFolder.getClass().getMethod("getIcon", boolean.class);
            getIconMethod.setAccessible(true);
            Object iconObject = getIconMethod.invoke(shellFolder, large);
            if (iconObject instanceof Image) {
                icon = new ImageIcon((Image)iconObject);
            }
        } catch (Exception ignore) {
            // shell folder not supported
        }
        if (icon == null) {
            icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        }
        return icon;
    }

    /**
     * Returns the file extension for a file. If the file has no extension or is a directory, then an empty string is returned.
     * For example would <code>getExtension(new File("picture.jpg"))</code> return <code>".jpg"</code> and
     * <code>getExtension(new File("."))</code> would return <code>""</code>.
     *
     * @param file file to get the extension for. May not be {@code null}.
     * @return the file extension including "." or an empty string if the file has no extension or is a directory.
     * @throws IllegalArgumentException if the file is {@code null}.
     */
    public static String getExtension(final File file) {
        Validate.notNull(file, "File may not be null");
        String name = file.isDirectory() ? "" : file.getName();
        int startOfExtension = name.lastIndexOf('.');
        if (startOfExtension != -1) {
            return name.substring(startOfExtension);
        } else {
            return name;
        }
    }

}

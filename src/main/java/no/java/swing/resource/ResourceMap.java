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

package no.java.swing.resource;

import no.java.swing.TextWithMnemonic;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.Set;

/**
 * @author Erlend Hamnaberg, Bouvet ASA
 */
public interface ResourceMap {

    Set<String> keySet();

    boolean containsKey(String key);

    <T> T getObject(String key, Class<T> type);

    String getString(String key, Object... args);

    TextWithMnemonic getTextWithMnemonic(String key);

    Boolean getBoolean(String key);

    Integer getInteger(String key);

    Long getLong(String key);

    Icon getIcon(String key);

    ImageIcon getImageIcon(String key);

    Font getFont(String key);

    Color getColor(String key);

    KeyStroke getKeyStroke(String key);

    Integer getKeyCode(String key);
}

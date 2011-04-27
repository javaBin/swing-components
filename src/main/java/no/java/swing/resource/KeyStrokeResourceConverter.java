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

import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

/**
 * @author Erlend Hamnaberg, Bouvet ASA
 */
public class KeyStrokeResourceConverter implements ResourceConverter<KeyStroke> {
    @Override
    public KeyStroke convert(String value) {
        int shortcut = value.indexOf("shortcut");
        if (shortcut != -1) {
            value = value.replace("shortcut", getPlatformSpecificShortCutKey());
        }
        return KeyStroke.getKeyStroke(value);
    }

    private String getPlatformSpecificShortCutKey() {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        return mask == KeyEvent.META_MASK ? "meta" : "ctrl";
    }
}

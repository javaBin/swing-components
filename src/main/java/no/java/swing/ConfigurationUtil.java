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
import java.util.ResourceBundle;

/**
 * Utility methods for configuring
 *
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>
 * @author <a href="mailto:harald@escenic.com">Harald Kuhr</a>
 */
public class ConfigurationUtil {

    private ConfigurationUtil() {
    }

    /**
     * Configures the action with name, icon etc, from the the given resource bundle.
     *
     * @param pAction         the pAction to be configured.
     * @param pActionId       root name used for looking up properties in the resource bundles.
     * @param pResourceBundle resource bundle use to look up values.
     */
    public static void configureAction(Action pAction, String pActionId, ResourceBundle pResourceBundle) {

        Validate.notNull(pAction, "Action can not be null");
        Validate.notEmpty(pActionId, "ActionId can not be empty");
        Validate.notNull(pResourceBundle, "ResourceBundle can not be null");

        String textWithMnemonicKey = pActionId + ".name";
        String acceleratorKey = pActionId + ".accelerator";
        String descriptionKey = pActionId + ".description";
        String iconKey = pActionId + ".icon";

        if (pResourceBundle.containsKey(textWithMnemonicKey)) {
            String textWithMnemonicString = pResourceBundle.getString(textWithMnemonicKey).trim();
            if (!textWithMnemonicString.isEmpty()) {
                TextWithMnemonic textWithMnemonic = new TextWithMnemonic(textWithMnemonicString);
                configureAction(pAction, textWithMnemonic);
            }
        }

        if (pResourceBundle.containsKey(acceleratorKey)) {
            String acceleratorString = pResourceBundle.getString(acceleratorKey).trim();
            if (!acceleratorString.isEmpty()) {
                KeyStroke keyStroke = KeyStroke.getKeyStroke(acceleratorString);
                if (keyStroke == null) {
                    throw new IllegalArgumentException("Illegal accelerator: " + acceleratorString);
                }
                pAction.putValue(Action.ACCELERATOR_KEY, keyStroke);
            }
        }

        if (pResourceBundle.containsKey(descriptionKey)) {
            String descriptionValue = pResourceBundle.getString(descriptionKey).trim();
            if (!descriptionValue.isEmpty()) {
                pAction.putValue(Action.SHORT_DESCRIPTION, descriptionValue);
            }
        }

        if (pResourceBundle.containsKey(iconKey)) {
            String iconPath = pResourceBundle.getString(iconKey).trim();
            if (!iconPath.isEmpty()) {
                Icon icon = SwingHelper.readIcon(iconPath);
                pAction.putValue(Action.SMALL_ICON, icon);
            }
        }

    }

    /**
     * Sets the text and mnemonic of an action as specified by the provided {@link no.java.swing.TextWithMnemonic}.
     *
     * @param pAction           the action to be configured. May not be {@code null}.
     * @param pTextWithMnemonic the text with mnemonic. May not be {@code null}.
     */
    public static void configureAction(Action pAction, TextWithMnemonic pTextWithMnemonic) {
        Validate.notNull(pAction, "Action may not be null");
        Validate.notNull(pTextWithMnemonic, "TextWithMnemonic may not be null");
        pAction.putValue(Action.NAME, pTextWithMnemonic.getTextWithoutMnemonic());
        if (pTextWithMnemonic.getMnemonic() != null) {
            pAction.putValue(Action.MNEMONIC_KEY, pTextWithMnemonic.getMnemonic());
            pAction.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, pTextWithMnemonic.getMnemonicIndex());
        }
    }
}

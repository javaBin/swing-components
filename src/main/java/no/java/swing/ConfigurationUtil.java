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

import no.java.swing.resource.ResourceMap;
import org.apache.commons.lang.Validate;

import javax.swing.*;

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
     * @param action      the pAction to be configured.
     * @param actionId    root name used for looking up properties in the resource bundles.
     * @param resourceMap resource bundle use to look up values.
     */
    public static void configureAction(Action action, String actionId, ResourceMap resourceMap) {

        Validate.notNull(action, "Action can not be null");
        Validate.notEmpty(actionId, "ActionId can not be empty");
        Validate.notNull(resourceMap, "ResourceBundle can not be null");

        String textWithMnemonicKey = actionId + ".name";
        String acceleratorKey = actionId + ".accelerator";
        String descriptionKey = actionId + ".description";
        String iconKey = actionId + ".icon";

        if (resourceMap.containsKey(textWithMnemonicKey)) {
            String textWithMnemonicString = resourceMap.getString(textWithMnemonicKey).trim();
            if (!textWithMnemonicString.isEmpty()) {
                TextWithMnemonic textWithMnemonic = new TextWithMnemonic(textWithMnemonicString);
                configureAction(action, textWithMnemonic);
            }
        }

        if (resourceMap.containsKey(acceleratorKey)) {
            KeyStroke keyStroke = resourceMap.getKeyStroke(acceleratorKey);
            action.putValue(Action.ACCELERATOR_KEY, keyStroke);
        }

        if (resourceMap.containsKey(descriptionKey)) {
            String descriptionValue = resourceMap.getString(descriptionKey).trim();
            if (!descriptionValue.isEmpty()) {
                action.putValue(Action.SHORT_DESCRIPTION, descriptionValue);
            }
        }

        if (resourceMap.containsKey(iconKey)) {
            Icon icon = resourceMap.getIcon(iconKey);
            action.putValue(Action.SMALL_ICON, icon);
        }

    }

    /**
     * Sets the text and mnemonic of an action as specified by the provided {@link no.java.swing.TextWithMnemonic}.
     *
     * @param action           the action to be configured. May not be {@code null}.
     * @param textWithMnemonic the text with mnemonic. May not be {@code null}.
     */
    public static void configureAction(Action action, TextWithMnemonic textWithMnemonic) {
        Validate.notNull(action, "Action may not be null");
        Validate.notNull(textWithMnemonic, "TextWithMnemonic may not be null");
        action.putValue(Action.NAME, textWithMnemonic.getTextWithoutMnemonic());
        if (textWithMnemonic.getMnemonic() != null) {
            action.putValue(Action.MNEMONIC_KEY, textWithMnemonic.getMnemonic());
            action.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, textWithMnemonic.getMnemonicIndex());
        }
    }
}

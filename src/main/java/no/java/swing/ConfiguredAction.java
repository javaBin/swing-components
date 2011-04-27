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

import javax.swing.AbstractAction;
import java.util.ResourceBundle;

/**
 * Creates a configured command. Save some lines of code compared to using {@link no.java.swing.ConfigurationUtil#configureAction(javax.swing.Action, String, ResourceBundle) configureAction()} directly.
 * <pre>
 * Action action = new ConfiguredAction("no.java.ExampleAction") {
 *   public void actionPerformed(final ActionEvent event) { }
 * };
 * </pre>
 *
 * @author <a href="mailto:yngvars@gmail.com">Yngvar S&oslash;rensen</a>.
 */
public abstract class ConfiguredAction extends AbstractAction {

    public static final String EXECUTING = "executing";


    /**
     * Creates and configures a new action using the provided resource map.
     *
     * @param basename       base name used for resource map lookups. May not be {@code null}.
     * @param resourceBundle resource map used to look up values. May not be {@code null}.
     * @throws IllegalArgumentException if pBaseName or pResourceMap == {@code null}.
     */
    protected ConfiguredAction(final String basename, final ResourceBundle resourceBundle) {
        ConfigurationUtil.configureAction(this, basename, resourceBundle);
    }
}

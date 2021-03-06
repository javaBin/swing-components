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

import no.java.swing.SwingHelper;

import javax.swing.ImageIcon;

/**
 * @author Erlend Hamnaberg, Bouvet ASA
 */
public class IconResourceConverter implements ResourceConverter<ImageIcon> {
    @Override
    public ImageIcon convert(String value) {
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        return (ImageIcon) SwingHelper.readIcon(value);
    }
}

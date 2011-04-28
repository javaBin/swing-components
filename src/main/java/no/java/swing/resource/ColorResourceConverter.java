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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.awt.Color;

/**
 * @author Erlend Hamnaberg, Bouvet ASA
 */
public class ColorResourceConverter implements ResourceConverter<Color> {
    @Override
    public Color convert(String value) {
        if (value.startsWith("#") || StringUtils.isNumeric(value)) {
            try {
                return Color.decode(value);
            } catch (NumberFormatException e) {
                throw new ResourceConversionException(e);
            }
        } else if (value.contains(",")) {
            String[] split = value.split(",");
            if (split.length != 3) {
                throw new ResourceConversionException("Cannot convert from a None RGB color, value was " + value);
            }
            try {
                return new Color(NumberUtils.toInt(split[0], -1), NumberUtils.toInt(split[1], -1), NumberUtils.toInt(split[2], -1));
            } catch (IllegalArgumentException e) {
                throw new ResourceConversionException(e);
            }
        }
        throw new ResourceConversionException("Unrecognized Color format " + value);
    }
}

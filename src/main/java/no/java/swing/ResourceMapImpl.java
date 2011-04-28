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

import no.java.swing.resource.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.*;

/**
 * @author Erlend Hamnaberg, Bouvet ASA
 */
class ResourceMapImpl implements ResourceMap {

    private static final Map<Class, ResourceConverter> converterMap = new HashMap<Class, ResourceConverter>();
    private final ResourceBundle bundle;
    private Set<String> keys;

    static {
        converterMap.put(Color.class, new ColorResourceConverter());
        converterMap.put(KeyStroke.class, new KeyStrokeResourceConverter());
        converterMap.put(String.class, new StringResourceConverter());
        converterMap.put(Icon.class, new IconResourceConverter());
        converterMap.put(ImageIcon.class, new IconResourceConverter());
    }

    ResourceMapImpl(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public Set<String> keySet() {
        if (keys == null) {
            keys = Collections.unmodifiableSet(new HashSet<String>(Collections.list(bundle.getKeys())));
        }
        return keys;
    }

    @Override
    public boolean containsKey(String key) {
        return bundle.containsKey(key);
    }

    @Override
    public <T> T getObject(String key, Class<T> type) {
        Validate.notNull(key, "Key may not be null");
        Validate.notNull(type, "Type may not be null");
        if (containsKey(key)) {
            ResourceConverter<T> converter = findConverter(type);
            if (converter == null) {
                throw new ResourceConversionException("No Converter found for " + type.getName());
            }
            String value = bundle.getString(key);
            if (!StringUtils.isBlank(value)) {
                return converter.convert(evaluateStringExpression(value.trim()));
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    private <T> ResourceConverter<T> findConverter(Class<T> type) {
        return converterMap.get(type);
    }

    @Override
    public final String getString(String key, Object... args) {
        String value = getObject(key, String.class);
        return String.format(value, args);
    }

    @Override
    public final Boolean getBoolean(String key) {
        return getObject(key, Boolean.class);
    }

    @Override
    public final Integer getInteger(String key) {
        return getObject(key, Integer.class);
    }

    @Override
    public final Long getLong(String key) {
        return getObject(key, Long.class);
    }

    @Override
    public final Icon getIcon(String key) {
        return getObject(key, Icon.class);
    }

    @Override
    public final ImageIcon getImageIcon(String key) {
        return getObject(key, ImageIcon.class);
    }

    @Override
    public final Font getFont(String key) {
        return getObject(key, Font.class);
    }

    @Override
    public final Color getColor(String key) {
        return getObject(key, Color.class);
    }

    @Override
    public final KeyStroke getKeyStroke(String key) {
        return getObject(key, KeyStroke.class);
    }

    @Override
    public final Integer getKeyCode(String key) {
        KeyStroke stroke = getKeyStroke(key);
        return stroke != null ? stroke.getKeyCode() : null;
    }

    /* Given the following resources:
    *
    * hello = Hello
    * world = World
    * place = ${world}
    *
    * The value of evaluateStringExpression("${hello} ${place}")
    * would be "Hello World".  The value of ${null} is null.
    */
    private String evaluateStringExpression(String expression) {
        if (expression.trim().equals("${null}")) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        int startIndex = 0;
        int endIndex;
        while ((endIndex = expression.indexOf("${", startIndex)) != -1) {
            if ((endIndex == 0) || ((endIndex > 0) && (expression.charAt(endIndex - 1) != '\\'))) {
                int expressionEnd = expression.indexOf("}", endIndex);
                if ((expressionEnd != -1) && (expressionEnd > endIndex + 2)) {
                    String key = expression.substring(endIndex + 2, expressionEnd);
                    String value = getString(key);
                    result.append(expression.substring(startIndex, endIndex));
                    if (value != null) {
                        result.append(value);
                    } else {
                        String msg = String.format("no value for \"%s\" in \"%s\"", key, expression);
                        throw new LookupException(msg, key, String.class);
                    }
                    startIndex = expressionEnd + 1;  // skip trailing "}"
                } else {
                    String msg = String.format("no closing brace in \"%s\"", expression);
                    throw new LookupException(msg, "<not found>", String.class);
                }
            } else {  // we've found an escaped variable - "\${"
                result.append(expression.substring(startIndex, endIndex - 1));
                result.append("${");
                startIndex = endIndex + 2; // skip past "${"
            }
        }
        result.append(expression.substring(startIndex));
        return result.toString();
    }
}

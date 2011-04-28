package no.java.swing.resource;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: maedhros
 * Date: 4/28/11
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */
public final class ResourceMapLoader {
    protected static final Locale NO_LOCALE = new Locale("", "");
    private static Map<String, Map<Locale, ResourceMap>> resources = Collections.synchronizedMap(new HashMap<String, Map<Locale, ResourceMap>>());

    public static ResourceMap getDefault() {
        return getResourceMap(ResourceBundleLoader.getBundleName(), Locale.getDefault());
    }

    public static ResourceMap getResourceMap(Class clazz, Locale locale) {
        return getResourceMap(clazz.getName(), locale);
    }

    public static ResourceMap getResourceMap(String name, Locale locale) {
        if (locale == null) {
            locale = NO_LOCALE;
        }
        Map<Locale, ResourceMap> values = resources.get(name);
        if (values != null) {
            ResourceMap resourceMap = values.get(locale);
            if (resourceMap != null) {
                return resourceMap;
            }
        } else {
            values = new HashMap<Locale, ResourceMap>();
        }
        return loadFromBundle(name, locale, values);
    }

    private static ResourceMap loadFromBundle(String name, Locale locale, Map<Locale, ResourceMap> values) {
        ResourceMap map = null;
        if (locale == NO_LOCALE) {
            ResourceBundle bundle = ResourceBundle.getBundle(name);
            if (bundle != null) {
                map = new ResourceMapImpl(bundle);
            }
        } else {
            ResourceBundle bundle = ResourceBundle.getBundle(name, locale);
            if (bundle != null) {
                map = new ResourceMapImpl(bundle);
            } else {
                return getResourceMap(name, NO_LOCALE);
            }
        }
        if (map != null) {
            values.put(locale, map);
            resources.put(name, values);
        }
        return map;
    }
}

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

/**
 * @author Erlend Hamnaberg, Bouvet ASA
 */
public class LookupException extends RuntimeException {
    private final String key;
    private final Class type;

    public LookupException(String message, String key, Class type) {
        super(String.format("%s: resource %s, type %s", message, key, type));
        this.key = key;
        this.type = type;
    }

    /**
     * Returns the type of the resource for which lookup failed.
     *
     * @return the resource type
     */
    public Class getType() {
        return type;
    }

    /**
     * Returns the type of the name of resource for which lookup failed.
     *
     * @return the resource name
     */
    public String getKey() {
        return key;
    }
}

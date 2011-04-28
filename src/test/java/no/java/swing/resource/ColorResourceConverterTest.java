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

import org.junit.Assert;
import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Erlend Hamnaberg, Bouvet ASA
 */
public class ColorResourceConverterTest {
    private ColorResourceConverter converter = new ColorResourceConverter();

    @Test
    public void verifyThatCommaSeparatedZerosAreParsedCorrectly() throws Exception {
        Color color = converter.convert("0,0,0");
        assertNotNull(color);
        assertEquals(color, Color.BLACK);
    }

    @Test(expected = ResourceConversionException.class)
    public void verifyThatCommaSeparatedGarbageThrowsException() throws Exception {
        converter.convert("abce3,dsf,34tg");
    }

    @Test(expected = ResourceConversionException.class)
    public void verifyThatCommaSeparatedGarbageThrowsExceptionOnlyTwoValues() throws Exception {
        converter.convert("abce3,dsf");
    }

    @Test(expected = ResourceConversionException.class)
    public void verifyThatCommaSeparatedZerosThrowsExceptionOnlyTwoValues() throws Exception {
        converter.convert("0,0");
    }

    @Test
    public void parseBlack() throws Exception {
        Color color = converter.convert("#000");
        assertNotNull(color);
        assertEquals(color, Color.BLACK);
        color = converter.convert("0");
        assertNotNull(color);
        assertEquals(color, Color.BLACK);
        color = converter.convert("000");
        assertNotNull(color);
        assertEquals(color, Color.BLACK);

    }

    @Test
    public void tryALongNumber() throws Exception {
        Color color = converter.convert("#045863456");
        assertNotNull(color);
    }


}

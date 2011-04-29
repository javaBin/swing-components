package no.java.swing.resource;

import no.java.swing.TextWithMnemonic;

/**
 * Created by IntelliJ IDEA.
 * User: maedhros
 * Date: 4/28/11
 * Time: 4:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class TextWithMnemonicResourceConverter implements ResourceConverter<TextWithMnemonic> {
    @Override
    public TextWithMnemonic convert(String value) {
        return new TextWithMnemonic(value);
    }
}

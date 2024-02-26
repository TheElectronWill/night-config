package com.electronwill.nightconfig.core.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Additional charsets to help parse configurations in an unknown encoding. */
public final class AdditionalCharsets {
    private AdditionalCharsets() {
        assert false;
    }

    /**
     * UTF-8 charset with an optional Byte-Order Mark at the beginning.
     * 
     * @see StandardCharsets.UTF_8
     */
    public static final Charset UTF_8_BOM = new CharsetUnicodeBom(true);

    /**
     * A charset that decodes UTF-8 (with or without BOM) or UTF-16 (with BOM), and encodes UTF-8.
     * 
     * @see StandardCharsets.UTF_8
     * @see StandardCharsets.UTF_16
     */
    public static final Charset UTF_8_OR_16 = new CharsetUnicodeBom(false);
}

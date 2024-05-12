package com.electronwill.nightconfig.core.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

/**
 * A special charset that supports reading UTF-8 with an optional BOM, and UTF-16 with a BOM.
 * Encoding is always done with UTF-8, without BOM.
 */
class CharsetUnicodeBom extends Charset {
    private boolean utf8Only;

    protected CharsetUnicodeBom(boolean utf8Only) {
        super(utf8Only ? "UTF-8" : "UTF-8-autodetect", new String[] { "UTF-8" });
    }

    @Override
    public boolean canEncode() {
        return true;
    }

    @Override
    public boolean contains(Charset cs) {
        return StandardCharsets.UTF_8.contains(cs);
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }

    private static final class Decoder extends CharsetDecoder {
        private boolean utf8Only;
        private CharsetDecoder decoder = null;

        Decoder(CharsetUnicodeBom cs) {
            super(cs, 0.5f, 1.0f);
            this.utf8Only = cs.utf8Only;
        }

        private void setupDecoder(Charset detectedCharset) {
            this.decoder = detectedCharset.newDecoder()
                    .onMalformedInput(this.malformedInputAction())
                    .onUnmappableCharacter(this.unmappableCharacterAction())
                    .replaceWith(this.replacement());
        }

        @Override
        public boolean isAutoDetecting() {
            return true;
        }

        @Override
        public boolean isCharsetDetected() {
            return decoder != null;
        }

        @Override
        public Charset detectedCharset() {
            return decoder.charset();
        }

        @Override
        protected CoderResult implFlush(CharBuffer out) {
            return decoder.flush(out);
        }

        @Override
        protected void implReset() {
            decoder.reset();
        }

        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            // Byte-Order Mark detection
            if (decoder == null) {
                int newPosition = in.position();
                try {
                    if (in.remaining() >= 2) {
                        int b1 = in.get() & 0xff;
                        int b2 = in.get() & 0xff;
                        // detect UTF-16 BE and LE BOMs: wrong encoding!
                        if (b1 == 0xFE && b2 == 0xFF) {
                            if (utf8Only) {
                                throw new ParsingException(null,
                                        "Invalid input: it begins with an UTF-16 BE byte-order mark, but it should be plain UTF-8.");
                            }
                            setupDecoder(StandardCharsets.UTF_16BE);
                            newPosition += 2;
                        } else if (b1 == 0xFF && b2 == 0xFE) {
                            if (utf8Only) {
                                throw new ParsingException(null,
                                        "Invalid input: it begins with an UTF-16 LE byte-order mark, but it should be plain UTF-8.");
                            }
                            setupDecoder(StandardCharsets.UTF_16LE);
                            newPosition += 2;
                        } else if (b1 == 0xEF && b2 == 0xBB) {
                            if (in.hasRemaining()) {
                                int b3 = in.get() & 0xff;
                                if (b3 == 0xBF) {
                                    // UTF-8 BOM "EF BB BF" detected! skip it
                                    newPosition += 3;
                                }
                                setupDecoder(StandardCharsets.UTF_8);
                            } else {
                                return CoderResult.UNDERFLOW;
                            }
                        } else {
                            setupDecoder(StandardCharsets.UTF_8);
                        }
                    } else {
                        return CoderResult.UNDERFLOW;
                    }
                } finally {
                    in.position(newPosition);
                }
            }
            // normal decoding
            return decoder.decode(in, out, false);
        }
    }

    private static final class Encoder extends CharsetEncoder {
        private final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();

        Encoder(CharsetUnicodeBom cs) {
            super(cs, 1.1f, 3.0f);
        }

        @Override
        public boolean canEncode(char c) {
            return encoder.canEncode(c);
        }

        @Override
        public boolean canEncode(CharSequence cs) {
            return encoder.canEncode(cs);
        }

        @Override
        public boolean isLegalReplacement(byte[] repl) {
            return encoder.isLegalReplacement(repl);
        }

        @Override
        protected void implReset() {
            encoder.reset();
        }

        @Override
        protected CoderResult implFlush(ByteBuffer out) {
            return encoder.flush(out);
        }

        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            return encoder.encode(in, out, false);
        }
    }
}

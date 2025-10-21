/*
 * Based on code originally licensed under the MIT License:
 * QR Code generator library (Java)
 * 
 * Copyright (c) Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/qr-code-generator-library
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 *
 * Modifications Copyright (c) 2025 Maciej Procyk. (Apache License, Version 2.0)
 * https://github.com/avan1235/qrcodegen-kotlin
 */
package `in`.procyk.qrcodegen

import kotlin.math.min

/**
 * A segment of character/binary/control data in a QR Code symbol.
 * Instances of this class are immutable.
 *
 * The mid-level way to create a segment is to take the payload data and call a
 * static factory function such as [QrSegment.makeNumeric]. The low-level
 * way to create a segment is to custom-make the bit buffer and call the [ ][QrSegment] with appropriate values.
 *
 * This segment class imposes no length restrictions, but QR Codes have restrictions.
 * Even in the most favorable conditions, a QR Code can only hold 7089 characters of data.
 * Any segment longer than this is meaningless for the purpose of generating QR Codes.
 * This class can represent kanji mode segments, but provides no help in encoding them
 * - see [QrSegmentAdvanced] for full kanji support.
 *
 * @constructor Constructs a QR Code segment with the specified attributes and data.
 * The character count (numCh) must agree with the mode and the bit buffer length,
 * but the constraint isn't checked. The specified bit buffer is cloned and stored.
 * @param mode mode indicator of this segment (not `null`)
 * @param numChars The length of this segment's unencoded data. Measured in characters for
 * numeric/alphanumeric/kanji mode, bytes for byte mode, and 0 for ECI mode.
 * Always zero or positive. Not the same as the data's bit length.
 * @param data the data bits (not `null`)
 * @throws NullPointerException if the mode or data is `null`
 * @throws IllegalArgumentException if the character count is negative
 */
class QrSegment(
    val mode: Mode,
    val numChars: Int,
    data: BitBuffer,
) {

    /**
     * The data bits of this segment. Not null. Accessed through getData().
     */
    private val _data: BitBuffer

    /**
     * Returns the data bits of this segment.
     * @return a new copy of the data bits (not `null`)
     */
    val data: BitBuffer
        get() = _data.clone() // Make defensive copy

    init {
        require(numChars >= 0) { "Invalid value" }
        this._data = data.clone() // Make defensive copy
    }

    /**
     * Describes how a segment's data bits are interpreted.
     *
     * @param modeBits The mode indicator bits, which is a uint4 value (range 0 to 15).
     * @param numBitsCharCount Number of character count bits for three different version ranges.
     */
    enum class Mode(
        val modeBits: Int,
        private vararg val numBitsCharCount: Int
    ) {
        NUMERIC(0x1, 10, 12, 14),
        ALPHANUMERIC(0x2, 9, 11, 13),
        BYTE(0x4, 8, 16, 16),
        KANJI(0x8, 8, 10, 12),
        ECI(0x7, 0, 0, 0);
        /**
         * Returns the bit width of the character count field for a segment in this mode
         * in a QR Code at the given version number. The result is in the range [0, 16].
         */
        fun numCharCountBits(ver: Int): Int {
            require(QrCode.MIN_VERSION <= ver && ver <= QrCode.MAX_VERSION)
            return numBitsCharCount[(ver + 7) / 17]
        }
    }

    companion object {
        /**
         * Returns a segment representing the specified binary data
         * encoded in byte mode. All input byte arrays are acceptable.
         *
         * Any text string can be converted to UTF-8 bytes (`s.getBytes(StandardCharsets.UTF_8)`) and encoded as a byte mode segment.
         * @param data the binary data (not `null`)
         * @return a segment (not `null`) containing the data
         * @throws NullPointerException if the array is `null`
         */
        fun makeBytes(data: ByteArray): QrSegment {
            val bb = BitBuffer()
            for (b in data) bb.appendBits(b.toInt() and 0xFF, 8)
            return QrSegment(Mode.BYTE, data.size, bb)
        }

        /**
         * Returns a segment representing the specified string of decimal digits encoded in numeric mode.
         * @param digits the text (not `null`), with only digits from 0 to 9 allowed
         * @return a segment (not `null`) containing the text
         * @throws NullPointerException if the string is `null`
         * @throws IllegalArgumentException if the string contains non-digit characters
         */
        fun makeNumeric(digits: CharSequence): QrSegment {
            require(isNumeric(digits)) { "String contains non-numeric characters" }

            val bb = BitBuffer()
            var i = 0
            while (i < digits.length) {
                // Consume up to 3 digits per iteration
                val n = min(digits.length - i, 3)
                bb.appendBits(digits.subSequence(i, i + n).toString().toInt(), n * 3 + 1)
                i += n
            }
            return QrSegment(Mode.NUMERIC, digits.length, bb)
        }

        /**
         * Returns a segment representing the specified text string encoded in alphanumeric mode.
         * The characters allowed are: 0 to 9, A to Z (uppercase only), space,
         * dollar, percent, asterisk, plus, hyphen, period, slash, colon.
         * @param text the text (not `null`), with only certain characters allowed
         * @return a segment (not `null`) containing the text
         * @throws NullPointerException if the string is `null`
         * @throws IllegalArgumentException if the string contains non-encodable characters
         */
        fun makeAlphanumeric(text: CharSequence): QrSegment {
            require(isAlphanumeric(text)) { "String contains unencodable characters in alphanumeric mode" }

            val bb = BitBuffer()
            var i = 0
            while (i <= text.length - 2) {
                // Process groups of 2
                var temp: Int = ALPHANUMERIC_CHARSET.indexOf(text[i]) * 45
                temp += ALPHANUMERIC_CHARSET.indexOf(text[i + 1])
                bb.appendBits(temp, 11)
                i += 2
            }
            if (i < text.length)  // 1 character remaining
                bb.appendBits(ALPHANUMERIC_CHARSET.indexOf(text[i]), 6)
            return QrSegment(Mode.ALPHANUMERIC, text.length, bb)
        }

        /**
         * Returns a list of zero or more segments to represent the specified Unicode text string.
         * The result may use various segment modes and switch modes to optimize the length of the bit stream.
         * @param text the text to be encoded, which can be any Unicode string
         * @return a new mutable list (not `null`) of segments (not `null`) containing the text
         * @throws NullPointerException if the text is `null`
         */
        fun makeSegments(text: CharSequence): MutableList<QrSegment> {
            // Select the most efficient segment encoding automatically
            val result: MutableList<QrSegment> = ArrayList()
            when {
                text == "" -> {}
                isNumeric(text) -> result.add(makeNumeric(text))
                isAlphanumeric(text) -> result.add(makeAlphanumeric(text))
                else -> result.add(makeBytes(text.toString().encodeToByteArray()))
            }
            return result
        }

        /**
         * Returns a segment representing an Extended Channel Interpretation
         * (ECI) designator with the specified assignment value.
         * @param assignVal the ECI assignment number (see the AIM ECI specification)
         * @return a segment (not `null`) containing the data
         * @throws IllegalArgumentException if the value is outside the range [0, 10<sup>6</sup>)
         */
        fun makeEci(assignVal: Int): QrSegment {
            val bb = BitBuffer()
            require(assignVal >= 0) { "ECI assignment value out of range" }
            if (assignVal < (1 shl 7)) bb.appendBits(assignVal, 8)
            else if (assignVal < (1 shl 14)) {
                bb.appendBits(2, 2)
                bb.appendBits(assignVal, 14)
            } else if (assignVal < 1000000) {
                bb.appendBits(6, 3)
                bb.appendBits(assignVal, 21)
            } else throw IllegalArgumentException("ECI assignment value out of range")
            return QrSegment(Mode.ECI, 0, bb)
        }

        /**
         * Tests whether the specified string can be encoded as a segment in numeric mode.
         * A string is encodable iff each character is in the range 0 to 9.
         * @param text the string to test for encodability (not `null`)
         * @return `true` iff each character is in the range 0 to 9.
         * @throws NullPointerException if the string is `null`
         * @see .makeNumeric
         */
        fun isNumeric(text: CharSequence): Boolean {
            return NUMERIC_REGEX.matches(text)
        }

        /**
         * Tests whether the specified string can be encoded as a segment in alphanumeric mode.
         * A string is encodable iff each character is in the following set: 0 to 9, A to Z
         * (uppercase only), space, dollar, percent, asterisk, plus, hyphen, period, slash, colon.
         * @param text the string to test for encodability (not `null`)
         * @return `true` iff each character is in the alphanumeric mode character set
         * @throws NullPointerException if the string is `null`
         * @see .makeAlphanumeric
         */
        fun isAlphanumeric(text: CharSequence): Boolean {
            return ALPHANUMERIC_REGEX.matches(text)
        }

        /**
         * Calculates the number of bits needed to encode the given segments at the given version.
         * Returns a non-negative number if successful. Otherwise returns -1 if a segment has too
         * many characters to fit its length field, or the total bits exceeds Integer.MAX_VALUE.
         */
        fun getTotalBits(segs: MutableList<QrSegment>, version: Int): Int {
            var result: Long = 0
            for (seg in segs) {
                val ccbits = seg.mode.numCharCountBits(version)
                if (seg.numChars >= (1 shl ccbits)) return -1 // The segment's length doesn't fit the field's bit width

                result += 4L + ccbits + seg._data.bitLength()
                if (result > Int.MAX_VALUE) return -1 // The sum will overflow an int type
            }
            return result.toInt()
        }

        /** Describes precisely all strings that are encodable in numeric mode. */
        private val NUMERIC_REGEX: Regex = Regex("[0-9]*")

        /** Describes precisely all strings that are encodable in alphanumeric mode. */
        private val ALPHANUMERIC_REGEX: Regex = Regex("[A-Z0-9 $%*+./:-]*")

        /**
         * The set of all legal characters in alphanumeric mode, where
         * each character value maps to the index in the string.
         */
        const val ALPHANUMERIC_CHARSET: String = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"
    }
}

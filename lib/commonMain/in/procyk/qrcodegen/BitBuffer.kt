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

import dev.dokky.bitvector.MutableBitVector

/**
 * An appendable sequence of bits (0s and 1s). Mainly used by [QrSegment].
 */
class BitBuffer {
    private var data: MutableBitVector = MutableBitVector()

    private var bitLength = 0 // Non-negative


    /**
     * Returns the length of this sequence, which is a non-negative value.
     * @return the length of this sequence
     */
    fun bitLength(): Int {
        require(bitLength >= 0)
        return bitLength
    }


    /**
     * Returns the bit at the specified index, yielding 0 or 1.
     * @param index the index to get the bit at
     * @return the bit at the specified index
     * @throws IndexOutOfBoundsException if index &lt; 0 or index &#x2265; bitLength
     */
    fun getBit(index: Int): Int {
        if (index !in 0..<bitLength) throw IndexOutOfBoundsException()
        return if (data[index]) 1 else 0
    }


    /**
     * Appends the specified number of low-order bits of the specified value to this
     * buffer. Requires 0 &#x2264; len &#x2264; 31 and 0 &#x2264; val &lt; 2<sup>len</sup>.
     * @param val the value to append
     * @param len the number of low-order bits in the value to take
     * @throws IllegalArgumentException if the value or number of bits is out of range
     * @throws IllegalStateException if appending the data
     * would make bitLength exceed Integer.MAX_VALUE
     */
    fun appendBits(`val`: Int, len: Int) {
        require(!(len !in 0..31 || `val` ushr len != 0)) { "Value out of range" }
        check(Int.MAX_VALUE - bitLength >= len) { "Maximum length reached" }
        var i = len - 1
        while (i >= 0) {
            // Append bit by bit
            data[bitLength] = QrCode.getBit(`val`, i)
            i--
            bitLength++
        }
    }


    /**
     * Appends the content of the specified bit buffer to this buffer.
     * @param bb the bit buffer whose data to append (not `null`)
     * @throws NullPointerException if the bit buffer is `null`
     * @throws IllegalStateException if appending the data
     * would make bitLength exceed Integer.MAX_VALUE
     */
    fun appendData(bb: BitBuffer) {
        check(Int.MAX_VALUE - bitLength >= bb.bitLength) { "Maximum length reached" }
        var i = 0
        while (i < bb.bitLength) {
            // Append bit by bit
            data[bitLength] = bb.data[i]
            i++
            bitLength++
        }
    }


    /**
     * Returns a new copy of this buffer.
     * @return a new copy of this buffer (not `null`)
     */
    fun clone(): BitBuffer = BitBuffer().also {
        it.bitLength = bitLength
        it.data = data.copy()
    }
}

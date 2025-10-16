/* 
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
 */
package `in`.procyk.qrcodegen

import `in`.procyk.qrcodegen.QrSegment.Companion.makeBytes
import `in`.procyk.qrcodegen.QrSegment.Companion.makeSegments
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * A QR Code symbol, which is a type of two-dimension barcode.
 * Invented by Denso Wave and described in the ISO/IEC 18004 standard.
 *
 * Instances of this class represent an immutable square grid of dark and light cells.
 * The class provides static factory functions to create a QR Code from text or binary data.
 * The class covers the QR Code Model 2 specification, supporting all versions (sizes)
 * from 1 to 40, all 4 error correction levels, and 4 character encoding modes.
 *
 * Ways to create a QR Code object:
 *
 *  *
 *
 *High level: Take the payload data and call [QrCode.encodeText]
 * or [QrCode.encodeBinary].
 *  *
 *
 *Mid level: Custom-make the list of [segments][QrSegment]
 * and call [QrCode.encodeSegments] or
 * [QrCode.encodeSegments]
 *  *
 *
 *Low level: Custom-make the array of data codeword bytes (including segment headers and
 * final padding, excluding error correction codewords), supply the appropriate version number,
 * and call the [constructor][QrCode.QrCode].
 *
 *
 * (Note that all ways require supplying the desired error correction level.)
 * @see QrSegment
 */
class QrCode(ver: Int, ecl: Ecc, dataCodewords: ByteArray, msk: Int) {
    /*---- Instance fields ----*/ // Public immutable scalar parameters:
    /** The version number of this QR Code, which is between 1 and 40 (inclusive).
     * This determines the size of this barcode.  */
    val version: Int

    /** The width and height of this QR Code, measured in modules, between
     * 21 and 177 (inclusive). This is equal to version &#xD7; 4 + 17.  */
    val size: Int

    /** The error correction level used in this QR Code, which is not `null`.  */
    val errorCorrectionLevel: Ecc

    /** The index of the mask pattern used in this QR Code, which is between 0 and 7 (inclusive).
     *
     * Even if a QR Code is created with automatic masking requested (mask =
     * &#x2212;1), the resulting object still has a mask value between 0 and 7.  */
    val mask: Int

    // Private grids of modules/pixels, with dimensions of size*size:
    // The modules of this QR Code (false = light, true = dark).
    // Immutable after constructor finishes. Accessed through getModule().
    private val modules: Array<BooleanArray>

    // Indicates function modules that are not subjected to masking. Discarded when constructor finishes.
    private var isFunction: Array<BooleanArray?>?


    /*---- Public instance methods ----*/
    /**
     * Returns the color of the module (pixel) at the specified coordinates, which is `false`
     * for light or `true` for dark. The top left corner has the coordinates (x=0, y=0).
     * If the specified coordinates are out of bounds, then `false` (light) is returned.
     * @param x the x coordinate, where 0 is the left edge and size&#x2212;1 is the right edge
     * @param y the y coordinate, where 0 is the top edge and size&#x2212;1 is the bottom edge
     * @return `true` if the coordinates are in bounds and the module
     * at that location is dark, or `false` (light) otherwise
     */
    fun getModule(x: Int, y: Int): Boolean {
        return 0 <= x && x < size && 0 <= y && y < size && modules[y][x]
    }


    /*---- Private helper methods for constructor: Drawing function modules ----*/ // Reads this object's version field, and draws and marks all function modules.
    private fun drawFunctionPatterns() {
        // Draw horizontal and vertical timing patterns
        for (i in 0..<size) {
            setFunctionModule(6, i, i % 2 == 0)
            setFunctionModule(i, 6, i % 2 == 0)
        }


        // Draw 3 finder patterns (all corners except bottom right; overwrites some timing modules)
        drawFinderPattern(3, 3)
        drawFinderPattern(size - 4, 3)
        drawFinderPattern(3, size - 4)


        // Draw numerous alignment patterns
        val alignPatPos = this.alignmentPatternPositions
        val numAlign = alignPatPos.size
        for (i in 0..<numAlign) {
            for (j in 0..<numAlign) {
                // Don't draw on the three finder corners
                if (!(i == 0 && j == 0 || i == 0 && j == numAlign - 1 || i == numAlign - 1 && j == 0)) drawAlignmentPattern(
                    alignPatPos[i],
                    alignPatPos[j]
                )
            }
        }


        // Draw configuration data
        drawFormatBits(0) // Dummy mask value; overwritten later in the constructor
        drawVersion()
    }


    // Draws two copies of the format bits (with its own error correction code)
    // based on the given mask and this object's error correction level field.
    private fun drawFormatBits(msk: Int) {
        // Calculate error correction code and pack bits
        val data = errorCorrectionLevel.formatBits shl 3 or msk // errCorrLvl is uint2, mask is uint3
        var rem = data
        for (i in 0..9) rem = (rem shl 1) xor ((rem ushr 9) * 0x537)
        val bits = (data shl 10 or rem) xor 0x5412 // uint15
        require(bits ushr 15 == 0)


        // Draw first copy
        for (i in 0..5) setFunctionModule(8, i, getBit(bits, i))
        setFunctionModule(8, 7, getBit(bits, 6))
        setFunctionModule(8, 8, getBit(bits, 7))
        setFunctionModule(7, 8, getBit(bits, 8))
        for (i in 9..14) setFunctionModule(14 - i, 8, getBit(bits, i))


        // Draw second copy
        for (i in 0..7) setFunctionModule(size - 1 - i, 8, getBit(bits, i))
        for (i in 8..14) setFunctionModule(8, size - 15 + i, getBit(bits, i))
        setFunctionModule(8, size - 8, true) // Always dark
    }


    // Draws two copies of the version bits (with its own error correction code),
    // based on this object's version field, iff 7 <= version <= 40.
    private fun drawVersion() {
        if (version < 7) return


        // Calculate error correction code and pack bits
        var rem = version // version is uint6, in the range [7, 40]
        for (i in 0..11) rem = (rem shl 1) xor ((rem ushr 11) * 0x1F25)
        val bits = version shl 12 or rem // uint18
        require(bits ushr 18 == 0)


        // Draw two copies
        for (i in 0..17) {
            val bit: Boolean = getBit(bits, i)
            val a = size - 11 + i % 3
            val b = i / 3
            setFunctionModule(a, b, bit)
            setFunctionModule(b, a, bit)
        }
    }


    // Draws a 9*9 finder pattern including the border separator,
    // with the center module at (x, y). Modules can be out of bounds.
    private fun drawFinderPattern(x: Int, y: Int) {
        for (dy in -4..4) {
            for (dx in -4..4) {
                val dist = max(abs(dx), abs(dy)) // Chebyshev/infinity norm
                val xx = x + dx
                val yy = y + dy
                if (0 <= xx && xx < size && 0 <= yy && yy < size) setFunctionModule(xx, yy, dist != 2 && dist != 4)
            }
        }
    }


    // Draws a 5*5 alignment pattern, with the center module
    // at (x, y). All modules must be in bounds.
    private fun drawAlignmentPattern(x: Int, y: Int) {
        for (dy in -2..2) {
            for (dx in -2..2) setFunctionModule(x + dx, y + dy, max(abs(dx), abs(dy)) != 1)
        }
    }


    // Sets the color of a module and marks it as a function module.
    // Only used by the constructor. Coordinates must be in bounds.
    private fun setFunctionModule(x: Int, y: Int, isDark: Boolean) {
        modules[y][x] = isDark
        isFunction!![y]!![x] = true
    }


    /*---- Private helper methods for constructor: Codewords and masking ----*/ // Returns a new byte string representing the given data with the appropriate error correction
    // codewords appended to it, based on this object's version and error correction level.
    private fun addEccAndInterleave(data: ByteArray): ByteArray {
        require(data!!.size == getNumDataCodewords(version, errorCorrectionLevel))


        // Calculate parameter numbers
        val numBlocks = NUM_ERROR_CORRECTION_BLOCKS[errorCorrectionLevel.ordinal]!![version].toInt()
        val blockEccLen = ECC_CODEWORDS_PER_BLOCK[errorCorrectionLevel.ordinal]!![version].toInt()
        val rawCodewords: Int = getNumRawDataModules(version) / 8
        val numShortBlocks = numBlocks - rawCodewords % numBlocks
        val shortBlockLen = rawCodewords / numBlocks


        // Split data into blocks and append ECC to each block
        val blocks = arrayOfNulls<ByteArray>(numBlocks)
        val rsDiv: ByteArray = reedSolomonComputeDivisor(blockEccLen)
        run {
            var i = 0
            var k = 0
            while (i < numBlocks) {
                val dat =
                    data.copyOfRange(k, k + shortBlockLen - blockEccLen + (if (i < numShortBlocks) 0 else 1))
                k += dat.size
                val block = dat.copyOf(shortBlockLen + 1)
                val ecc: ByteArray = reedSolomonComputeRemainder(dat, rsDiv)
                ecc.copyInto(
                    destination = block,
                    destinationOffset = block.size - blockEccLen,
                    startIndex = 0,
                    endIndex = ecc.size
                )
                blocks[i] = block
                i++
            }
        }


        // Interleave (not concatenate) the bytes from every block into a single sequence
        val result = ByteArray(rawCodewords)
        var i = 0
        var k = 0
        while (i < blocks[0]!!.size) {
            for (j in blocks.indices) {
                // Skip the padding byte in short blocks
                if (i != shortBlockLen - blockEccLen || j >= numShortBlocks) {
                    result[k] = blocks[j]!![i]
                    k++
                }
            }
            i++
        }
        return result
    }


    // Draws the given sequence of 8-bit codewords (data and error correction) onto the entire
    // data area of this QR Code. Function modules need to be marked off before this is called.
    private fun drawCodewords(data: ByteArray) {
        require(data!!.size == getNumRawDataModules(version) / 8)

        var i = 0 // Bit index into the data
        // Do the funny zigzag scan
        var right = size - 1
        while (right >= 1) {
            // Index of right column in each column pair
            if (right == 6) right = 5
            for (vert in 0..<size) {  // Vertical counter
                for (j in 0..1) {
                    val x = right - j // Actual x coordinate
                    val upward = ((right + 1) and 2) == 0
                    val y = if (upward) size - 1 - vert else vert // Actual y coordinate
                    if (!isFunction!![y]!![x] && i < data.size * 8) {
                        modules[y][x] = getBit(data[i ushr 3].toInt(), 7 - (i and 7))
                        i++
                    }
                    // If this QR Code has any remainder bits (0 to 7), they were assigned as
                    // 0/false/light by the constructor and are left unchanged by this method
                }
            }
            right -= 2
        }
        require(i == data.size * 8)
    }


    // XORs the codeword modules in this QR Code with the given mask pattern.
    // The function modules must be marked and the codeword bits must be drawn
    // before masking. Due to the arithmetic of XOR, calling applyMask() with
    // the same mask value a second time will undo the mask. A final well-formed
    // QR Code needs exactly one (not zero, two, etc.) mask applied.
    private fun applyMask(msk: Int) {
        require(!(msk < 0 || msk > 7)) { "Mask value out of range" }
        for (y in 0..<size) {
            for (x in 0..<size) {
                val invert: Boolean
                when (msk) {
                    0 -> invert = (x + y) % 2 == 0
                    1 -> invert = y % 2 == 0
                    2 -> invert = x % 3 == 0
                    3 -> invert = (x + y) % 3 == 0
                    4 -> invert = (x / 3 + y / 2) % 2 == 0
                    5 -> invert = x * y % 2 + x * y % 3 == 0
                    6 -> invert = (x * y % 2 + x * y % 3) % 2 == 0
                    7 -> invert = ((x + y) % 2 + x * y % 3) % 2 == 0
                    else -> throw AssertionError()
                }
                modules[y][x] = modules[y][x] xor (invert and !isFunction!![y]!![x])
            }
        }
    }


    private val penaltyScore: Int
        // Calculates and returns the penalty score based on state of this QR Code's current modules.
        get() {
            var result = 0


            // Adjacent modules in row having same color, and finder-like patterns
            for (y in 0..<size) {
                var runColor = false
                var runX = 0
                val runHistory = IntArray(7)
                for (x in 0..<size) {
                    if (modules[y][x] == runColor) {
                        runX++
                        if (runX == 5) result += PENALTY_N1
                        else if (runX > 5) result++
                    } else {
                        finderPenaltyAddHistory(runX, runHistory)
                        if (!runColor) result += finderPenaltyCountPatterns(runHistory) * PENALTY_N3
                        runColor = modules[y][x]
                        runX = 1
                    }
                }
                result += finderPenaltyTerminateAndCount(runColor, runX, runHistory) * PENALTY_N3
            }
            // Adjacent modules in column having same color, and finder-like patterns
            for (x in 0..<size) {
                var runColor = false
                var runY = 0
                val runHistory = IntArray(7)
                for (y in 0..<size) {
                    if (modules[y][x] == runColor) {
                        runY++
                        if (runY == 5) result += PENALTY_N1
                        else if (runY > 5) result++
                    } else {
                        finderPenaltyAddHistory(runY, runHistory)
                        if (!runColor) result += finderPenaltyCountPatterns(runHistory) * PENALTY_N3
                        runColor = modules[y][x]
                        runY = 1
                    }
                }
                result += finderPenaltyTerminateAndCount(runColor, runY, runHistory) * PENALTY_N3
            }


            // 2*2 blocks of modules having same color
            for (y in 0..<size - 1) {
                for (x in 0..<size - 1) {
                    val color = modules[y][x]
                    if (color == modules[y][x + 1] && color == modules[y + 1][x] && color == modules[y + 1][x + 1]) result += PENALTY_N2
                }
            }


            // Balance of dark and light modules
            var dark = 0
            for (row in modules) {
                for (color in row) {
                    if (color) dark++
                }
            }
            val total = size * size // Note that size is odd, so dark/total != 1/2
            // Compute the smallest integer k >= 0 such that (45-5k)% <= dark/total <= (55+5k)%
            val k = (abs(dark * 20 - total * 10) + total - 1) / total - 1
            require(0 <= k && k <= 9)
            result += k * PENALTY_N4
            require(
                0 <= result && result <= 2568888 // Non-tight upper bound based on default values of PENALTY_N1, ..., N4
            )
            return result
        }


    private val alignmentPatternPositions: IntArray
        /*---- Private helper functions ----*/
        get() {
            if (version == 1) return intArrayOf()
            else {
                val numAlign = version / 7 + 2
                val step = (version * 8 + numAlign * 3 + 5) / (numAlign * 4 - 4) * 2
                val result = IntArray(numAlign)
                result[0] = 6
                var i = result.size - 1
                var pos = size - 7
                while (i >= 1) {
                    result[i] = pos
                    i--
                    pos -= step
                }
                return result
            }
        }


    // Can only be called immediately after a light run is added, and
    // returns either 0, 1, or 2. A helper function for getPenaltyScore().
    private fun finderPenaltyCountPatterns(runHistory: IntArray): Int {
        val n = runHistory[1]
        require(n <= size * 3)
        val core = n > 0 && runHistory[2] == n && runHistory[3] == n * 3 && runHistory[4] == n && runHistory[5] == n
        return ((if (core && runHistory[0] >= n * 4 && runHistory[6] >= n) 1 else 0)
                + (if (core && runHistory[6] >= n * 4 && runHistory[0] >= n) 1 else 0))
    }


    // Must be called at the end of a line (row or column) of modules. A helper function for getPenaltyScore().
    private fun finderPenaltyTerminateAndCount(
        currentRunColor: Boolean,
        currentRunLength: Int,
        runHistory: IntArray
    ): Int {
        var currentRunLength = currentRunLength
        if (currentRunColor) {  // Terminate dark run
            finderPenaltyAddHistory(currentRunLength, runHistory)
            currentRunLength = 0
        }
        currentRunLength += size // Add light border to final run
        finderPenaltyAddHistory(currentRunLength, runHistory)
        return finderPenaltyCountPatterns(runHistory)
    }


    // Pushes the given value to the front and drops the last value. A helper function for getPenaltyScore().
    private fun finderPenaltyAddHistory(currentRunLength: Int, runHistory: IntArray) {
        var currentRunLength = currentRunLength
        if (runHistory[0] == 0) currentRunLength += size // Add light border to initial run

        runHistory.copyInto(
            destination = runHistory,
            destinationOffset = 1,
            startIndex = 0,
            endIndex = runHistory.size - 1
        )
        runHistory[0] = currentRunLength
    }


    /*---- Constructor (low level) ----*/ /**
     * Constructs a QR Code with the specified version number,
     * error correction level, data codeword bytes, and mask number.
     *
     * This is a low-level API that most users should not use directly. A mid-level
     * API is the [.encodeSegments] function.
     * @param ver the version number to use, which must be in the range 1 to 40 (inclusive)
     * @param ecl the error correction level to use
     * @param dataCodewords the bytes representing segments to encode (without ECC)
     * @param msk the mask pattern to use, which is either &#x2212;1 for automatic choice or from 0 to 7 for fixed choice
     * @throws NullPointerException if the byte array or error correction level is `null`
     * @throws IllegalArgumentException if the version or mask value is out of range,
     * or if the data is the wrong length for the specified version and error correction level
     */
    init {
        // Check arguments and initialize fields
        var msk = msk
        require(!(ver < MIN_VERSION || ver > MAX_VERSION)) { "Version value out of range" }
        require(!(msk < -1 || msk > 7)) { "Mask value out of range" }
        version = ver
        size = ver * 4 + 17
        errorCorrectionLevel = ecl
        modules = Array<BooleanArray>(size) { BooleanArray(size) }  // Initially all light
        isFunction = Array<BooleanArray?>(size) { BooleanArray(size) }


        // Compute ECC, draw modules, do masking
        drawFunctionPatterns()
        val allCodewords = addEccAndInterleave(dataCodewords)
        drawCodewords(allCodewords)


        // Do masking
        if (msk == -1) {  // Automatically choose best mask
            var minPenalty = Int.Companion.MAX_VALUE
            for (i in 0..7) {
                applyMask(i)
                drawFormatBits(i)
                val penalty = this.penaltyScore
                if (penalty < minPenalty) {
                    msk = i
                    minPenalty = penalty
                }
                applyMask(i) // Undoes the mask due to XOR
            }
        }
        require(0 <= msk && msk <= 7)
        mask = msk
        applyMask(msk) // Apply the final choice of mask
        drawFormatBits(msk) // Overwrite old format bits

        isFunction = null
    }


    /*---- Public helper enumeration ----*/
    /**
     * The error correction level in a QR Code symbol.
     */
    enum class Ecc // Constructor.
        (// In the range 0 to 3 (unsigned 2-bit integer).
        val formatBits: Int
    ) {
        // Must be declared in ascending order of error protection
        // so that the implicit ordinal() and values() work properly
        /** The QR Code can tolerate about  7% erroneous codewords.  */
        LOW(1),

        /** The QR Code can tolerate about 15% erroneous codewords.  */
        MEDIUM(0),

        /** The QR Code can tolerate about 25% erroneous codewords.  */
        QUARTILE(3),

        /** The QR Code can tolerate about 30% erroneous codewords.  */
        HIGH(2)
    }

    companion object {
        /*---- Static factory functions (high level) ----*/
        /**
         * Returns a QR Code representing the specified Unicode text string at the specified error correction level.
         * As a conservative upper bound, this function is guaranteed to succeed for strings that have 738 or fewer
         * Unicode code points (not UTF-16 code units) if the low error correction level is used. The smallest possible
         * QR Code version is automatically chosen for the output. The ECC level of the result may be higher than the
         * ecl argument if it can be done without increasing the version.
         * @param text the text to be encoded (not `null`), which can be any Unicode string
         * @param ecl the error correction level to use (not `null`) (boostable)
         * @return a QR Code (not `null`) representing the text
         * @throws NullPointerException if the text or error correction level is `null`
         * @throws DataTooLongException if the text fails to fit in the
         * largest version QR Code at the ECL, which means it is too long
         */
        fun encodeText(text: CharSequence, ecl: Ecc): QrCode {
            val segs: MutableList<QrSegment> = makeSegments(text)
            return encodeSegments(segs, ecl)
        }


        /**
         * Returns a QR Code representing the specified binary data at the specified error correction level.
         * This function always encodes using the binary segment mode, not any text mode. The maximum number of
         * bytes allowed is 2953. The smallest possible QR Code version is automatically chosen for the output.
         * The ECC level of the result may be higher than the ecl argument if it can be done without increasing the version.
         * @param data the binary data to encode (not `null`)
         * @param ecl the error correction level to use (not `null`) (boostable)
         * @return a QR Code (not `null`) representing the data
         * @throws NullPointerException if the data or error correction level is `null`
         * @throws DataTooLongException if the data fails to fit in the
         * largest version QR Code at the ECL, which means it is too long
         */
        fun encodeBinary(data: ByteArray, ecl: Ecc): QrCode {
            val seg = makeBytes(data)
            return encodeSegments(mutableListOf(seg), ecl)
        }


        /**
         * Returns a QR Code representing the specified segments with the specified encoding parameters.
         * The smallest possible QR Code version within the specified range is automatically
         * chosen for the output. Iff boostEcl is `true`, then the ECC level of the
         * result may be higher than the ecl argument if it can be done without increasing
         * the version. The mask number is either between 0 to 7 (inclusive) to force that
         * mask, or &#x2212;1 to automatically choose an appropriate mask (which may be slow).
         *
         * This function allows the user to create a custom sequence of segments that switches
         * between modes (such as alphanumeric and byte) to encode text in less space.
         * This is a mid-level API; the high-level API is [.encodeText]
         * and [.encodeBinary].
         * @param segs the segments to encode
         * @param ecl the error correction level to use (not `null`) (boostable)
         * @param minVersion the minimum allowed version of the QR Code (at least 1)
         * @param maxVersion the maximum allowed version of the QR Code (at most 40)
         * @param mask the mask number to use (between 0 and 7 (inclusive)), or &#x2212;1 for automatic mask
         * @param boostEcl increases the ECC level as long as it doesn't increase the version number
         * @return a QR Code (not `null`) representing the segments
         * @throws NullPointerException if the list of segments, any segment, or the error correction level is `null`
         * @throws IllegalArgumentException if 1 &#x2264; minVersion &#x2264; maxVersion &#x2264; 40
         * or &#x2212;1 &#x2264; mask &#x2264; 7 is violated
         * @throws DataTooLongException if the segments fail to fit in
         * the maxVersion QR Code at the ECL, which means they are too long
         */
        /*---- Static factory functions (mid level) ----*/
        /**
         * Returns a QR Code representing the specified segments at the specified error correction
         * level. The smallest possible QR Code version is automatically chosen for the output. The ECC level
         * of the result may be higher than the ecl argument if it can be done without increasing the version.
         *
         * This function allows the user to create a custom sequence of segments that switches
         * between modes (such as alphanumeric and byte) to encode text in less space.
         * This is a mid-level API; the high-level API is [.encodeText]
         * and [.encodeBinary].
         * @param segs the segments to encode
         * @param ecl the error correction level to use (not `null`) (boostable)
         * @return a QR Code (not `null`) representing the segments
         * @throws NullPointerException if the list of segments, any segment, or the error correction level is `null`
         * @throws DataTooLongException if the segments fail to fit in the
         * largest version QR Code at the ECL, which means they are too long
         */
        fun encodeSegments(
            segs: MutableList<QrSegment>,
            ecl: Ecc,
            minVersion: Int = MIN_VERSION,
            maxVersion: Int = MAX_VERSION,
            mask: Int = -1,
            boostEcl: Boolean = true
        ): QrCode {
            var ecl = ecl
            require(!(!(MIN_VERSION <= minVersion && minVersion <= maxVersion && maxVersion <= MAX_VERSION) || mask < -1 || mask > 7)) { "Invalid value" }


            // Find the minimal version number to use
            var version: Int
            var dataUsedBits: Int
            version = minVersion
            while (true) {
                val dataCapacityBits: Int =
                    Companion.getNumDataCodewords(version, ecl!!) * 8 // Number of data bits available
                dataUsedBits = QrSegment.getTotalBits(segs, version)
                if (dataUsedBits != -1 && dataUsedBits <= dataCapacityBits) break // This version number is found to be suitable

                if (version >= maxVersion) {  // All versions in the range could not fit the given data
                    var msg = "Segment too long"
                    if (dataUsedBits != -1) msg =
                        "Data length = $dataUsedBits bits, Max capacity = $dataCapacityBits bits"
                    throw DataTooLongException(msg)
                }
                version++
            }
            require(dataUsedBits != -1)


            // Increase the error correction level while the data still fits in the current version number
            for (newEcl in Ecc.entries) {  // From low to high
                if (boostEcl && dataUsedBits <= getNumDataCodewords(version, newEcl) * 8) ecl = newEcl
            }


            // Concatenate all segments to create the data bit string
            val bb = BitBuffer()
            for (seg in segs!!) {
                bb.appendBits(seg.mode.modeBits, 4)
                bb.appendBits(seg.numChars, seg.mode.numCharCountBits(version))
                bb.appendData(seg._data)
            }
            require(bb.bitLength() == dataUsedBits)


            // Add terminator and pad up to a byte if applicable
            val dataCapacityBits: Int = Companion.getNumDataCodewords(version, ecl!!) * 8
            require(bb.bitLength() <= dataCapacityBits)
            bb.appendBits(0, min(4, dataCapacityBits - bb.bitLength()))
            bb.appendBits(0, (8 - bb.bitLength() % 8) % 8)
            require(bb.bitLength() % 8 == 0)


            // Pad with alternating bytes until data capacity is reached
            var padByte = 0xEC
            while (bb.bitLength() < dataCapacityBits) {
                bb.appendBits(padByte, 8)
                padByte = padByte xor (0xEC xor 0x11)
            }


            // Pack bits into bytes in big endian
            val dataCodewords = ByteArray(bb.bitLength() / 8)
            for (i in 0..<bb.bitLength()) dataCodewords[i ushr 3] =
                (dataCodewords[i ushr 3].toInt() or (bb.getBit(i) shl (7 - (i and 7)))).toByte()


            // Create the QR Code object
            return QrCode(version, ecl, dataCodewords, mask)
        }


        // Returns the number of data bits that can be stored in a QR Code of the given version number, after
        // all function modules are excluded. This includes remainder bits, so it might not be a multiple of 8.
        // The result is in the range [208, 29648]. This could be implemented as a 40-entry lookup table.
        private fun getNumRawDataModules(ver: Int): Int {
            require(!(ver < MIN_VERSION || ver > MAX_VERSION)) { "Version number out of range" }

            val size = ver * 4 + 17
            var result = size * size // Number of modules in the whole QR Code square
            result -= 8 * 8 * 3 // Subtract the three finders with separators
            result -= 15 * 2 + 1 // Subtract the format information and dark module
            result -= (size - 16) * 2 // Subtract the timing patterns (excluding finders)
            // The five lines above are equivalent to: int result = (16 * ver + 128) * ver + 64;
            if (ver >= 2) {
                val numAlign = ver / 7 + 2
                result -= (numAlign - 1) * (numAlign - 1) * 25 // Subtract alignment patterns not overlapping with timing patterns
                result -= (numAlign - 2) * 2 * 20 // Subtract alignment patterns that overlap with timing patterns
                // The two lines above are equivalent to: result -= (25 * numAlign - 10) * numAlign - 55;
                if (ver >= 7) result -= 6 * 3 * 2 // Subtract version information
            }
            require(208 <= result && result <= 29648)
            return result
        }


        // Returns a Reed-Solomon ECC generator polynomial for the given degree. This could be
        // implemented as a lookup table over all possible parameter values, instead of as an algorithm.
        private fun reedSolomonComputeDivisor(degree: Int): ByteArray {
            require(!(degree < 1 || degree > 255)) { "Degree out of range" }
            // Polynomial coefficients are stored from highest to lowest power, excluding the leading term which is always 1.
            // For example the polynomial x^3 + 255x^2 + 8x + 93 is stored as the uint8 array {255, 8, 93}.
            val result = ByteArray(degree)
            result[degree - 1] = 1 // Start off with the monomial x^0


            // Compute the product polynomial (x - r^0) * (x - r^1) * (x - r^2) * ... * (x - r^{degree-1}),
            // and drop the highest monomial term which is always 1x^degree.
            // Note that r = 0x02, which is a generator element of this field GF(2^8/0x11D).
            var root = 1
            for (i in 0..<degree) {
                // Multiply the current product by (x - r^i)
                for (j in result.indices) {
                    result[j] = reedSolomonMultiply(result[j].toInt() and 0xFF, root).toByte()
                    if (j + 1 < result.size) result[j] = (result[j].toInt() xor result[j + 1].toInt()).toByte()
                }
                root = reedSolomonMultiply(root, 0x02)
            }
            return result
        }


        // Returns the Reed-Solomon error correction codeword for the given data and divisor polynomials.
        private fun reedSolomonComputeRemainder(data: ByteArray, divisor: ByteArray): ByteArray {
            val result = ByteArray(divisor!!.size)
            for (b in data!!) {  // Polynomial division
                val factor = (b.toInt() xor result[0].toInt()) and 0xFF
                result.copyInto(
                    destination = result,
                    destinationOffset = 0,
                    startIndex = 1,
                    endIndex = result.size
                )
                result[result.size - 1] = 0
                for (i in result.indices) result[i] =
                    (result[i].toInt() xor reedSolomonMultiply(divisor[i].toInt() and 0xFF, factor)).toByte()
            }
            return result
        }


        // Returns the product of the two given field elements modulo GF(2^8/0x11D). The arguments and result
        // are unsigned 8-bit integers. This could be implemented as a lookup table of 256*256 entries of uint8.
        private fun reedSolomonMultiply(x: Int, y: Int): Int {
            require(x shr 8 == 0 && y shr 8 == 0)
            // Russian peasant multiplication
            var z = 0
            for (i in 7 downTo 0) {
                z = (z shl 1) xor ((z ushr 7) * 0x11D)
                z = z xor ((y ushr i) and 1) * x
            }
            require(z ushr 8 == 0)
            return z
        }


        // Returns the number of 8-bit data (i.e. not error correction) codewords contained in any
        // QR Code of the given version number and error correction level, with remainder bits discarded.
        // This stateless pure function could be implemented as a (40*4)-cell lookup table.
        fun getNumDataCodewords(ver: Int, ecl: Ecc): Int {
            return (getNumRawDataModules(ver) / 8
                    - ECC_CODEWORDS_PER_BLOCK[ecl.ordinal]!![ver]
                    * NUM_ERROR_CORRECTION_BLOCKS[ecl.ordinal]!![ver])
        }


        // Returns true iff the i'th bit of x is set to 1.
        fun getBit(x: Int, i: Int): Boolean {
            return ((x ushr i) and 1) != 0
        }


        /*---- Constants and tables ----*/
        /** The minimum version number  (1) supported in the QR Code Model 2 standard.  */
        const val MIN_VERSION: Int = 1

        /** The maximum version number (40) supported in the QR Code Model 2 standard.  */
        const val MAX_VERSION: Int = 40


        // For use in getPenaltyScore(), when evaluating which mask is best.
        private const val PENALTY_N1 = 3
        private const val PENALTY_N2 = 3
        private const val PENALTY_N3 = 40
        private const val PENALTY_N4 = 10


        private val ECC_CODEWORDS_PER_BLOCK = arrayOf<ByteArray?>(
            // Version: (note that index 0 is for padding, and is set to an illegal value)
            //0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40    Error correction level
            byteArrayOf(
                -1,
                7,
                10,
                15,
                20,
                26,
                18,
                20,
                24,
                30,
                18,
                20,
                24,
                26,
                30,
                22,
                24,
                28,
                30,
                28,
                28,
                28,
                28,
                30,
                30,
                26,
                28,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30
            ),  // Low
            byteArrayOf(
                -1,
                10,
                16,
                26,
                18,
                24,
                16,
                18,
                22,
                22,
                26,
                30,
                22,
                22,
                24,
                24,
                28,
                28,
                26,
                26,
                26,
                26,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28,
                28
            ),  // Medium
            byteArrayOf(
                -1,
                13,
                22,
                18,
                26,
                18,
                24,
                18,
                22,
                20,
                24,
                28,
                26,
                24,
                20,
                30,
                24,
                28,
                28,
                26,
                30,
                28,
                30,
                30,
                30,
                30,
                28,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30
            ),  // Quartile
            byteArrayOf(
                -1,
                17,
                28,
                22,
                16,
                22,
                28,
                26,
                26,
                24,
                28,
                24,
                28,
                22,
                24,
                24,
                30,
                28,
                28,
                26,
                28,
                30,
                24,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30,
                30
            ),  // High
        )

        private val NUM_ERROR_CORRECTION_BLOCKS = arrayOf<ByteArray?>(
            // Version: (note that index 0 is for padding, and is set to an illegal value)
            //0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40    Error correction level
            byteArrayOf(
                -1,
                1,
                1,
                1,
                1,
                1,
                2,
                2,
                2,
                2,
                4,
                4,
                4,
                4,
                4,
                6,
                6,
                6,
                6,
                7,
                8,
                8,
                9,
                9,
                10,
                12,
                12,
                12,
                13,
                14,
                15,
                16,
                17,
                18,
                19,
                19,
                20,
                21,
                22,
                24,
                25
            ),  // Low
            byteArrayOf(
                -1,
                1,
                1,
                1,
                2,
                2,
                4,
                4,
                4,
                5,
                5,
                5,
                8,
                9,
                9,
                10,
                10,
                11,
                13,
                14,
                16,
                17,
                17,
                18,
                20,
                21,
                23,
                25,
                26,
                28,
                29,
                31,
                33,
                35,
                37,
                38,
                40,
                43,
                45,
                47,
                49
            ),  // Medium
            byteArrayOf(
                -1,
                1,
                1,
                2,
                2,
                4,
                4,
                6,
                6,
                8,
                8,
                8,
                10,
                12,
                16,
                12,
                17,
                16,
                18,
                21,
                20,
                23,
                23,
                25,
                27,
                29,
                34,
                34,
                35,
                38,
                40,
                43,
                45,
                48,
                51,
                53,
                56,
                59,
                62,
                65,
                68
            ),  // Quartile
            byteArrayOf(
                -1,
                1,
                1,
                2,
                4,
                4,
                4,
                5,
                6,
                8,
                8,
                11,
                11,
                16,
                16,
                18,
                16,
                19,
                21,
                25,
                25,
                25,
                34,
                30,
                32,
                35,
                37,
                40,
                42,
                45,
                48,
                51,
                54,
                57,
                60,
                63,
                66,
                70,
                74,
                77,
                81
            ),  // High
        )
    }
}

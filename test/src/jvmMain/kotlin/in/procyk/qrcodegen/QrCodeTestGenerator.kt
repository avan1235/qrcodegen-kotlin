package `in`.procyk.qrcodegen

import io.nayuki.qrcodegen.QrCode
import kotlinx.serialization.json.Json
import kotlin.io.path.*
import kotlin.random.Random


fun main() {
    deleteExistingTestData()
    generateTextTestData()
    generateBinaryTestData()
    generateTests()
}

@OptIn(ExperimentalPathApi::class)
private fun deleteExistingTestData() {
    Path("data").takeIf { it.exists() }?.deleteRecursively()
}

private fun generateTextTestData(): Int = generateTestData(
    dir = "text",
    data = TEXT_INPUTS,
    f = { input, ecc -> QrCodeTestData.Input.Text(input, ecc.name) },
    g = { text, ecc -> QrCode.encodeText(text, ecc) },
)

private fun generateBinaryTestData(): Int = generateTestData(
    dir = "binary",
    data = BINARY_INPUTS,
    f = { input, ecc -> QrCodeTestData.Input.Binary(input.asList(), ecc.name) },
    g = { bytes, ecc -> QrCode.encodeBinary(bytes, ecc) },
)

private fun <T> generateTestData(
    dir: String,
    data: List<T>,
    f: (T, QrCode.Ecc) -> QrCodeTestData.Input,
    g: (T, QrCode.Ecc) -> QrCode,
): Int {
    var idx = 0
    data.forEach { data ->
        QrCode.Ecc.entries.forEach { ecc ->
            val input = f(data, ecc)
            val data = runCatching {
                g(data, ecc).run {
                    QrCodeTestData.Success(
                        input = input,
                        expectedSize = size,
                        expectedMask = mask,
                        expectedErrorCorrectionLevel = errorCorrectionLevel.name,
                        expectedData = List(size * size) { getModule(it % size, it / size) },
                    )
                }
            }.fold(
                onSuccess = { it },
                onFailure = {
                    QrCodeTestData.Failure(
                        input = input,
                        message = it.message,
                    )
                }
            )
            val text = PrettyPrintJson.encodeToString<QrCodeTestData>(data)
            Path("data/$dir/test${idx++}.json")
                .apply { parent.createDirectories() }
                .writeText(text)
        }
    }
    return idx
}

private fun generateTests() {
    for (dir in listOf("text", "binary")) {
        val uppercaseDir = dir.replaceFirstChar { it.uppercaseChar() }
        buildString {
            """
           |package `in`.procyk.qrcodegen
           |
           |import kotlin.test.Test
           |
           |class QRCode${uppercaseDir}Test {
            """.trimMargin().let(::appendLine)

            val data = Path("data/$dir").listDirectoryEntries("test[0-9]*.json")
                .sortedBy { it.nameWithoutExtension.removePrefix("test").toInt() }

            data.forEach { path ->
                """
           |
           |    @Test
           |    fun ${path.nameWithoutExtension}() = assertTestData("$dir", "${path.name}")
                """.trimMargin().let(::appendLine)
            }

            appendLine("}")
        }.let(Path("src/commonTest/kotlin/in/procyk/qrcodegen/QrCode${uppercaseDir}Test.kt")::writeText)
    }
}

private val PrettyPrintJson = Json { prettyPrint = true }

private val TEXT_RANDOM = Random(42)

private fun randomText(length: Int): String = generateSequence {
    Char(TEXT_RANDOM.nextInt(Char.MIN_VALUE.code, Char.MAX_VALUE.code + 1))
}
    .take(length)
    .joinToString("")

private val TEXT_INPUTS: List<String> = listOf(
    // Basic scenarios
    "",                                          // Empty string
    "Hello World",                               // Simple ASCII text
    "12345",                                     // Numeric only
    "HELLO",                                     // Uppercase alphabetic
    "https://example.com",                       // URL
    "test@example.com",                          // Email

    // Unicode and special characters
    "Hello 世界",                                 // Mixed ASCII and CJK
    "🎉🎊🎈",                                     // Emojis (surrogate pairs)
    "café",                                      // Accented characters
    "Привет мир",                                // Cyrillic
    "مرحبا",                                     // Arabic (RTL)
    "שלום",                                      // Hebrew (RTL)
    "こんにちは",                                   // Japanese Hiragana
    "안녕하세요",                                   // Korean
    "🔥💯✨",                                     // Multiple emojis
    "👨‍👩‍👧‍👦",                                     // Family emoji (ZWJ sequence)

    // Whitespace variations
    " ",                                         // Single space
    "   ",                                       // Multiple spaces
    "\n",                                        // Newline
    "\r\n",                                      // Windows line ending
    "\t",                                        // Tab
    "Hello\nWorld",                              // Text with newline
    "  Hello  World  ",                          // Leading/trailing spaces

    // Special characters and symbols
    "!@#$%^&*()",                               // Special characters
    "{}[]<>",                                    // Brackets
    "\\",                                        // Backslash
    "\"'`",                                      // Quotes
    "|~",                                        // Pipe and tilde
    "a\u0000b",                                  // Null character

    // Edge cases for length
    "a",                                         // Single character
    "ab",                                        // Two characters
    "a".repeat(100),                             // 100 characters
    "a".repeat(500),                             // 500 characters
    "a".repeat(738),                             // Maximum guaranteed (738 code points)
    "a".repeat(3391),                             // Maximum working
    "a".repeat(3392),                             // minimum for failure
    "世".repeat(369),                             // 369 CJK characters (738 bytes in UTF-8)

    // Numeric strings (QR Code has numeric mode)
    "0",                                         // Single digit
    "0123456789",                                // All digits
    "9".repeat(100),                             // Long numeric string

    // Alphanumeric strings (QR Code has alphanumeric mode)
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 $%*+-./:", // All alphanumeric chars
    "ABC123",                                    // Mixed alphanumeric

    // Mixed content types (forcing byte mode)
    "abc123ABC",                                 // Mixed case alphanumeric
    "Test123!@#",                                // Alphanumeric with symbols
    "12345 HELLO world 世界",                    // Mixed: numeric, alpha, unicode

    // JSON-like structures
    "{\"key\":\"value\"}",                       // JSON string
    "[1,2,3]",                                   // JSON array

    // XML-like structures
    "<tag>content</tag>",                        // XML/HTML tag

    // URLs with various schemes
    "http://example.com",                        // HTTP URL
    "https://example.com/path?query=1&foo=bar",  // HTTPS with query params
    "ftp://files.example.com",                   // FTP URL
    "mailto:test@example.com",                   // Mailto URL

    // Phone numbers and codes
    "+1-555-123-4567",                           // International phone
    "TEL:+15551234567",                          // Tel URI
    "WIFI:S:MyNetwork;T:WPA;P:password;;",       // WiFi QR format

    // Version control patterns
    "vCard",                                     // vCard keyword
    "BEGIN:VCARD\nVERSION:3.0\nFN:John Doe\nEND:VCARD", // Minimal vCard

    // Tricky Unicode cases
    "🏴󠁧󠁢󠁳󠁣󠁴󠁿",                                     // Flag emoji (multiple code points)
    "e\u0301",                                   // é as combining characters (e + acute)
    "A\u0308",                                   // Ä as combining characters
    "\uD83D\uDE00",                              // 😀 as surrogate pair
    "test\uFEFF",                                // Zero-width no-break space (BOM)
    "\u200B\u200C\u200D",                        // Zero-width joiners/non-joiners

    // Control characters
    "\u0001\u0002\u0003",                        // Control characters
    "text\u007F",                                // DEL character

    // Repetitive patterns
    "ababababab",                                // Repeating pattern
    "1234567890".repeat(10),                     // Repeating numeric

    // Real-world use cases
    "SMSTO:+15551234567:Hello",                  // SMS QR format
    "geo:37.7749,-122.4194",                     // Geo coordinates
    "bitcoin:1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", // Bitcoin address
    "MATMSG:TO:test@example.com;SUB:Subject;BODY:Message;;", // Email format

    // Boundary cases
    "\uFFFF",                                    // Highest BMP character
    "\uD800\uDC00",                              // First supplementary character (U+10000)
    "🀀",                                         // Mahjong tile (U+1F000)

    // Mixed scripts
    "Hello мир 世界 🌍",                          // Multiple scripts and emoji
    "test123テストשלום",                          // Latin, numeric, Japanese, Hebrew
).let { static ->
    buildList {
        addAll(static)
        repeat(1_000) { add(randomText(it + 1)) }
    }
}

private val BINARY_RANDOM = Random(42)

private val BINARY_INPUTS: List<ByteArray> = listOf(
    byteArrayOf(),
    byteArrayOf(0),
    byteArrayOf(127),
    byteArrayOf(-128),
    ByteArray(2953) { it.toByte() },
    ByteArray(2954) { it.toByte() },
).let { static ->
    buildList {
        addAll(static)
        repeat(1_000) { add(BINARY_RANDOM.nextBytes(it + 1)) }
    }
}

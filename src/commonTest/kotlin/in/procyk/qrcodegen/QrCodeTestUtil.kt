package `in`.procyk.qrcodegen

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal fun assertTestData(name: String) {
    val testData = readQrCodeTestData("src/testData/$name")
    val result = runCatching {
        QrCode.encodeText(testData.inputText, QrCode.Ecc.valueOf(testData.inputEccName))
    }
    when (testData) {
        is QrCodeTestData.Failure -> result.run {
            assertTrue(isFailure)

            val message = exceptionOrNull()?.message
            assertEquals(testData.message, message)
        }

        is QrCodeTestData.Success -> result.run {
            assertTrue(isSuccess)

            val code = getOrNull()
            assertNotNull(code)

            assertEquals(testData.expectedSize, code.size)
            assertEquals(testData.expectedMask, code.mask)
            assertEquals(testData.expectedErrorCorrectionLevel, code.errorCorrectionLevel.name)
            val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
            assertEquals(testData.expectedData, data.joinToString("") { if (it) "1" else "0" })
        }
    }
}


private fun readQrCodeTestData(path: String): QrCodeTestData =
    Json.decodeFromString(readString(path))

private fun readString(path: String): String =
    SystemFileSystem.source(Path(path)).buffered().buffered().use { it.readString() }

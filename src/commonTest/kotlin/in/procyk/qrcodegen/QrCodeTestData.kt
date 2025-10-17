package `in`.procyk.qrcodegen

import kotlinx.serialization.Serializable

@Serializable
sealed class QrCodeTestData {

    abstract val inputText: String
    abstract val inputEccName: String

    @Serializable
    data class Success(
        override val inputText: String,
        override val inputEccName: String,
        val expectedSize: Int,
        val expectedMask: Int,
        val expectedErrorCorrectionLevel: String,
        val expectedData: String,
    ) : QrCodeTestData()

    @Serializable
    data class Failure(
        override val inputText: String,
        override val inputEccName: String,
        val message: String?,
    ) : QrCodeTestData()
}

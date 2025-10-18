package `in`.procyk.qrcodegen

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
sealed class QrCodeTestData {

    @Serializable
    sealed class Input {
        @Serializable
        data class Text(val text: String, val eccName: String) : Input()

        @Serializable
        data class Binary(val bytes: List<Byte>, val eccName: String) : Input()
    }

    abstract val input: Input

    @Serializable
    data class Success(
        override val input: Input,
        val expectedSize: Int,
        val expectedMask: Int,
        val expectedErrorCorrectionLevel: String,
        @Serializable(with = BooleanListSerializer::class)
        val expectedData: List<Boolean>,
    ) : QrCodeTestData()

    @Serializable
    data class Failure(
        override val input: Input,
        val message: String?,
    ) : QrCodeTestData()
}

private class BooleanListSerializer : KSerializer<List<Boolean>> {
    private val delegate = String.serializer()

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(
        encoder: Encoder,
        value: List<Boolean>
    ) {
        val string = value.joinToString("") { if (it) "1" else "0" }
        delegate.serialize(encoder, string)
    }

    override fun deserialize(decoder: Decoder): List<Boolean> {
        val string = delegate.deserialize(decoder)
        return List(string.length) {
            when (string[it]) {
                '0' -> false
                '1' -> true
                else -> throw IllegalArgumentException("Invalid boolean value: $string")
            }
        }
    }

}
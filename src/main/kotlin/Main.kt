package `in`.procyk

import `in`.procyk.qrcodegen.QrCode

fun main() {
    val code = QrCode.encodeText("Hello World", QrCode.Ecc.HIGH)
    object : QRMatrix {
        override val size: Int = code.size
        override fun get(x: Int, y: Int): Boolean = code.getModule(x, y)
    }.toQRString().also(::println)
}
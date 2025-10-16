import `in`.procyk.qrcodegen.QrCode
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readLine
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun readLine(path: String): String? = 
    SystemFileSystem.source(Path(path)).buffered().buffered().use { it.readLine() }

class QRCodeTest {

//    @Test
//    fun test() {
//        var i = 0
//        listOf(
//            "S",
//            "Hello, world!",
//            "Super duper longer string that might be done there",
//            "now it's even longer one that can't be that easily split as it doesn't have that many words".repeat(10),
//        ).forEach { string ->
//            QrCode.Ecc.entries.forEach { ecc ->
//                val code = QrCode.encodeText(string, ecc)
//                val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
//                Path("test${i}.data").writeText(data.joinToString("") { if (it) "1" else "0" })
//                println(
//                    """
//    @Test
//    fun test${i}() {
//        val code = QrCode.encodeText("$string", QrCode.Ecc.${ecc.name})
//        assertEquals(${code.size}, code.size)
//        assertTrue(QrCode.Ecc.${ecc.name} <= code.errorCorrectionLevel)
//        assertEquals(${code.version}, code.version)
//        assertEquals(${code.mask}, code.mask)
//
//        val expectedData = readLine("test${i}.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
//        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
//        assertContentEquals(expectedData, data)
//    }
//               """.trimIndent()
//                )
//                i += 1
//            }
//        }
//    }

    @Test
    fun test0() {
        val code = QrCode.encodeText("S", QrCode.Ecc.LOW)
        assertEquals(21, code.size)
        assertTrue(QrCode.Ecc.LOW <= code.errorCorrectionLevel)
        assertEquals(1, code.version)
        assertEquals(1, code.mask)

        val expectedData = readLine("test0.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test1() {
        val code = QrCode.encodeText("S", QrCode.Ecc.MEDIUM)
        assertEquals(21, code.size)
        assertTrue(QrCode.Ecc.MEDIUM <= code.errorCorrectionLevel)
        assertEquals(1, code.version)
        assertEquals(1, code.mask)

        val expectedData = readLine("test1.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test2() {
        val code = QrCode.encodeText("S", QrCode.Ecc.QUARTILE)
        assertEquals(21, code.size)
        assertTrue(QrCode.Ecc.QUARTILE <= code.errorCorrectionLevel)
        assertEquals(1, code.version)
        assertEquals(1, code.mask)

        val expectedData = readLine("test2.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test3() {
        val code = QrCode.encodeText("S", QrCode.Ecc.HIGH)
        assertEquals(21, code.size)
        assertTrue(QrCode.Ecc.HIGH <= code.errorCorrectionLevel)
        assertEquals(1, code.version)
        assertEquals(1, code.mask)

        val expectedData = readLine("test3.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test4() {
        val code = QrCode.encodeText("Hello, world!", QrCode.Ecc.LOW)
        assertEquals(21, code.size)
        assertTrue(QrCode.Ecc.LOW <= code.errorCorrectionLevel)
        assertEquals(1, code.version)
        assertEquals(2, code.mask)

        val expectedData = readLine("test4.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test5() {
        val code = QrCode.encodeText("Hello, world!", QrCode.Ecc.MEDIUM)
        assertEquals(21, code.size)
        assertTrue(QrCode.Ecc.MEDIUM <= code.errorCorrectionLevel)
        assertEquals(1, code.version)
        assertEquals(2, code.mask)

        val expectedData = readLine("test5.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test6() {
        val code = QrCode.encodeText("Hello, world!", QrCode.Ecc.QUARTILE)
        assertEquals(25, code.size)
        assertTrue(QrCode.Ecc.QUARTILE <= code.errorCorrectionLevel)
        assertEquals(2, code.version)
        assertEquals(2, code.mask)

        val expectedData = readLine("test6.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test7() {
        val code = QrCode.encodeText("Hello, world!", QrCode.Ecc.HIGH)
        assertEquals(25, code.size)
        assertTrue(QrCode.Ecc.HIGH <= code.errorCorrectionLevel)
        assertEquals(2, code.version)
        assertEquals(2, code.mask)

        val expectedData = readLine("test7.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test8() {
        val code = QrCode.encodeText("Super duper longer string that might be done there", QrCode.Ecc.LOW)
        assertEquals(29, code.size)
        assertTrue(QrCode.Ecc.LOW <= code.errorCorrectionLevel)
        assertEquals(3, code.version)
        assertEquals(3, code.mask)

        val expectedData = readLine("test8.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test9() {
        val code = QrCode.encodeText("Super duper longer string that might be done there", QrCode.Ecc.MEDIUM)
        assertEquals(33, code.size)
        assertTrue(QrCode.Ecc.MEDIUM <= code.errorCorrectionLevel)
        assertEquals(4, code.version)
        assertEquals(0, code.mask)

        val expectedData = readLine("test9.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test10() {
        val code = QrCode.encodeText("Super duper longer string that might be done there", QrCode.Ecc.QUARTILE)
        assertEquals(37, code.size)
        assertTrue(QrCode.Ecc.QUARTILE <= code.errorCorrectionLevel)
        assertEquals(5, code.version)
        assertEquals(0, code.mask)

        val expectedData = readLine("test10.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test11() {
        val code = QrCode.encodeText("Super duper longer string that might be done there", QrCode.Ecc.HIGH)
        assertEquals(41, code.size)
        assertTrue(QrCode.Ecc.HIGH <= code.errorCorrectionLevel)
        assertEquals(6, code.version)
        assertEquals(2, code.mask)

        val expectedData = readLine("test11.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test12() {
        val code = QrCode.encodeText(
            "now it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many words",
            QrCode.Ecc.LOW
        )
        assertEquals(101, code.size)
        assertTrue(QrCode.Ecc.LOW <= code.errorCorrectionLevel)
        assertEquals(21, code.version)
        assertEquals(2, code.mask)

        val expectedData = readLine("test12.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test13() {
        val code = QrCode.encodeText(
            "now it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many words",
            QrCode.Ecc.MEDIUM
        )
        assertEquals(113, code.size)
        assertTrue(QrCode.Ecc.MEDIUM <= code.errorCorrectionLevel)
        assertEquals(24, code.version)
        assertEquals(2, code.mask)

        val expectedData = readLine("test13.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test14() {
        val code = QrCode.encodeText(
            "now it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many words",
            QrCode.Ecc.QUARTILE
        )
        assertEquals(137, code.size)
        assertTrue(QrCode.Ecc.QUARTILE <= code.errorCorrectionLevel)
        assertEquals(30, code.version)
        assertEquals(2, code.mask)

        val expectedData = readLine("test14.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

    @Test
    fun test15() {
        val code = QrCode.encodeText(
            "now it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many words",
            QrCode.Ecc.HIGH
        )
        assertEquals(153, code.size)
        assertTrue(QrCode.Ecc.HIGH <= code.errorCorrectionLevel)
        assertEquals(34, code.version)
        assertEquals(2, code.mask)

        val expectedData = readLine("test15.data")!!.map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

}
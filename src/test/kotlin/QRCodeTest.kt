import `in`.procyk.qrcodegen.QrCode
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
//        val expectedData = Path("test${i}.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test0.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test1.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test2.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test3.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test4.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test5.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test6.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test7.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test8.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test9.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test10.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
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

        val expectedData = Path("test11.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }
    @Test
    fun test12() {
        val code = QrCode.encodeText("now it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many words", QrCode.Ecc.LOW)
        assertEquals(101, code.size)
        assertTrue(QrCode.Ecc.LOW <= code.errorCorrectionLevel)
        assertEquals(21, code.version)
        assertEquals(2, code.mask)

        val expectedData = Path("test12.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }
    @Test
    fun test13() {
        val code = QrCode.encodeText("now it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many words", QrCode.Ecc.MEDIUM)
        assertEquals(113, code.size)
        assertTrue(QrCode.Ecc.MEDIUM <= code.errorCorrectionLevel)
        assertEquals(24, code.version)
        assertEquals(2, code.mask)

        val expectedData = Path("test13.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }
    @Test
    fun test14() {
        val code = QrCode.encodeText("now it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many words", QrCode.Ecc.QUARTILE)
        assertEquals(137, code.size)
        assertTrue(QrCode.Ecc.QUARTILE <= code.errorCorrectionLevel)
        assertEquals(30, code.version)
        assertEquals(2, code.mask)

        val expectedData = Path("test14.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }
    @Test
    fun test15() {
        val code = QrCode.encodeText("now it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many wordsnow it's even longer one that can't be that easily split as it doesn't have that many words", QrCode.Ecc.HIGH)
        assertEquals(153, code.size)
        assertTrue(QrCode.Ecc.HIGH <= code.errorCorrectionLevel)
        assertEquals(34, code.version)
        assertEquals(2, code.mask)

        val expectedData = Path("test15.data").readLines().single().map { it == '1' }.toTypedArray().toBooleanArray()
        val data = BooleanArray(code.size * code.size) { code.getModule(it % code.size, it / code.size) }
        assertContentEquals(expectedData, data)
    }

}
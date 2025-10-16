package `in`.procyk

private const val WHITE_WHITE = "█"
private const val BLACK_BLACK = " "
private const val WHITE_BLACK = "▀"
private const val BLACK_WHITE = "▄"
private const val QUIET_ZONE = 2

interface QRMatrix {
    val size: Int
    operator fun get(x: Int, y: Int): Boolean
}

fun QRMatrix.toQRString(): String = let { matrix ->
    val writer = StringBuilder()

    val header = WHITE_WHITE.repeat(matrix.size + QUIET_ZONE * 2)
    writer.append((header + "\n").repeat(QUIET_ZONE / 2))

    var i = 0
    while (i <= matrix.size) {
        writer.append(WHITE_WHITE.repeat(QUIET_ZONE))
        for (j in 0..<matrix.size) {
            val nextBlack = i + 1 < matrix.size && matrix[j, i + 1]
            val currentBlack = matrix[j, i]
            if (currentBlack && nextBlack) {
                writer.append(BLACK_BLACK)
            } else if (currentBlack) {
                writer.append(BLACK_WHITE)
            } else if (!nextBlack) {
                writer.append(WHITE_WHITE)
            } else {
                writer.append(WHITE_BLACK)
            }
        }

        writer.append(WHITE_WHITE.repeat(QUIET_ZONE - 1))
        writer.append("\n")
        i += QUIET_ZONE
    }

    val trailing = WHITE_BLACK.repeat(matrix.size + QUIET_ZONE * 2)
    writer.append(trailing.repeat(QUIET_ZONE / 2 - 1))
    writer.append("\n")
    return writer.toString()
}

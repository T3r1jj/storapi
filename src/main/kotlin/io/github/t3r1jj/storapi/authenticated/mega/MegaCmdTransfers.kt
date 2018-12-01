package io.github.t3r1jj.storapi.authenticated.mega

import com.github.eliux.mega.MegaUtils
import com.github.eliux.mega.cmd.AbstractMegaCmdCaller
import com.github.eliux.mega.error.MegaIOException
import java.io.IOException
import kotlin.streams.toList

internal class MegaCmdTransfers : AbstractMegaCmdCaller<List<MegaTransfer>>() {
    override fun getCmd(): String {
        return "transfers --path-display-size=2048"
    }

    override fun call(): List<MegaTransfer> {
        try {
            return MegaUtils.execCmdWithOutput(executableCommand())
                    .stream()
                    .skip(1)
                    .map { result ->
                        val results = trimSplit(result)
                        MegaTransfer(results[0], results[1].toInt(), results[2],
                                results[3], results[4].removeSuffix("%").toFloat() / 100f, results[6].toFloat(),
                                results[7], results[8])
                    }.toList()
        } catch (e: IOException) {
            throw MegaIOException("MegauploadError while executing transfers")
        }

    }

    private fun trimSplit(fileInfoStr: String): Array<out String> {
        return java.lang.String(java.lang.String(java.lang.String(fileInfoStr)
                .trim())
                .replace("\\t", "\\s"))
                .split("\\s+")
    }
}

internal data class MegaTransfer(val dirSync: String, val tag: Int, val sourcePath: String,
                                 val destinyPath: String, val progress: Float, val totalSize: Float,
                                 val unit: String, val state: String) {
    val bytesTotal: Long

    init {
        val fileSizeUnits = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
        val unitIndex = if (fileSizeUnits.contains(unit)) fileSizeUnits.indexOf(unit) else 0
        bytesTotal = (totalSize * Math.pow(1024.toDouble(), unitIndex.toDouble())).toLong()
    }

    val bytesWritten: Long
        get() = (progress * bytesTotal + 0.5).toLong()
}
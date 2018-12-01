package io.github.t3r1jj.storapi.authenticated.mega

import com.github.eliux.mega.MegaUtils
import com.github.eliux.mega.cmd.FileInfo
import com.github.eliux.mega.cmd.MegaCmdList
import com.github.eliux.mega.error.MegaIOException
import io.github.t3r1jj.storapi.data.RecordMeta
import java.io.IOException
import java.util.*

internal class MegaCmdRecursiveList(private val remotePath: String) : MegaCmdList("-r $remotePath") {
    fun recursiveCall(): MutableList<RecordMeta> {
        try {
            val meta = ArrayList<RecordMeta>()
            val results = MegaUtils.execCmdWithOutput(executableCommand())
            var relativePath = remotePath
            for (i in 1..results.lastIndex) {
                when (fileInfoType(results[i])) {
                    FileInfoType.PATH -> relativePath = concatPath(results[i].substringBefore(":"), "")
                    FileInfoType.FILE -> {
                        val fileInfo = FileInfo.parseInfo(results[i])
                        if (fileInfo.isFile) {
                            val recordMeta = RecordMeta(fileInfo.name, concatPath(relativePath, fileInfo.name), fileInfo.size.orElse(0))
                            meta.add(recordMeta)
                        }
                    }
                    else -> {
                    }
                }
            }
            return meta
        } catch (e: IOException) {
            throw MegaIOException("MegauploadError while listing $remotePath")
        }
    }

    private fun fileInfoType(fileInfoStr: String): FileInfoType {
        val tokens = trimSplit(fileInfoStr)
        return when (tokens.size) {
            1 -> FileInfoType.PATH
            6 -> FileInfoType.FILE
            else -> FileInfoType.INVALID
        }
    }

    private fun trimSplit(fileInfoStr: String): Array<out String> {
        return java.lang.String(java.lang.String(fileInfoStr)
                .replace("\\t", "\\s"))
                .split("\\s+")
    }

    enum class FileInfoType {
        FILE, PATH, INVALID
    }

    private fun concatPath(filePath: String, name: String) =
            if (filePath.endsWith('/')) (filePath + name) else ("$filePath/$name")
}
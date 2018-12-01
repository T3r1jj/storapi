package io.github.t3r1jj.storapi

import org.apache.commons.io.IOUtils
import java.io.FileOutputStream
import java.io.InputStream

abstract class NamedStorage {
    override fun toString(): String {
        return this::class.java.simpleName
    }

    protected fun stream2file(`in`: InputStream): java.io.File {
        val tempFile = java.io.File.createTempFile(System.currentTimeMillis().toString(), null)
        tempFile.deleteOnExit()
        FileOutputStream(tempFile).use { out -> IOUtils.copy(`in`, out) }
        return tempFile
    }
}
package io.github.t3r1jj.storapi.data

import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.util.*

data class Record(val name: String, val path: String, val data: InputStream) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Record
        return Objects.equals(name, other.name) &&
                Objects.equals(path, other.path) &&
                IOUtils.contentEquals(data, other.data)
    }

    override fun hashCode() = path.hashCode()
}
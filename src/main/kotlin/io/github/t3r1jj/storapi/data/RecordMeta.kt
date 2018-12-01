package io.github.t3r1jj.storapi.data

data class RecordMeta(val name: String, val path: String, val size: Long) {
    var id: String? = null
    var publicPath: String? = null
}
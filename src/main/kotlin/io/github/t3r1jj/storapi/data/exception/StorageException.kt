package io.github.t3r1jj.storapi.data.exception

open class StorageException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
    constructor(message: String?) : this(message, null)
}
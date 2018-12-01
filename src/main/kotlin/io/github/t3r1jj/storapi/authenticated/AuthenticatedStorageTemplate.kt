package io.github.t3r1jj.storapi.authenticated

import io.github.t3r1jj.storapi.NamedStorage
import io.github.t3r1jj.storapi.data.Record
import io.github.t3r1jj.storapi.data.RecordMeta
import io.github.t3r1jj.storapi.data.StorageInfo
import io.github.t3r1jj.storapi.data.exception.StorageUnauthenticatedException
import java.util.function.Consumer

abstract class AuthenticatedStorageTemplate : NamedStorage(), AuthenticatedStorage {
    private fun throwIfNotAuthenticated() {
        if (!isLogged()) {
            throw StorageUnauthenticatedException("This action requires storage authentication (login).", this)
        }
    }

    final override fun upload(record: Record): RecordMeta {
        throwIfNotAuthenticated()
        return doAuthenticatedUpload(record)
    }

    final override fun upload(record: Record, bytesWrittenConsumer: Consumer<Long>?): RecordMeta {
        throwIfNotAuthenticated()
        return doAuthenticatedUpload(record, bytesWrittenConsumer)
    }

    final override fun download(filePath: String): Record {
        throwIfNotAuthenticated()
        return doAuthenticatedDownload(filePath)
    }

    final override fun download(filePath: String, bytesWrittenConsumer: Consumer<Long>?): Record {
        throwIfNotAuthenticated()
        return doAuthenticatedDownload(filePath, bytesWrittenConsumer)
    }

    final override fun findAll(filePath: String): List<RecordMeta> {
        throwIfNotAuthenticated()
        return doAuthenticatedFindAll(filePath)
    }

    final override fun getInfo(): StorageInfo {
        throwIfNotAuthenticated()
        return doAuthenticatedGetInfo()
    }

    final override fun delete(meta: RecordMeta) {
        throwIfNotAuthenticated()
        return doAuthenticatedDelete(meta)
    }

    abstract fun doAuthenticatedUpload(record: Record): RecordMeta
    abstract fun doAuthenticatedUpload(record: Record, bytesWrittenConsumer: Consumer<Long>?): RecordMeta
    abstract fun doAuthenticatedDownload(filePath: String): Record
    abstract fun doAuthenticatedDownload(filePath: String, bytesWrittenConsumer: Consumer<Long>?): Record
    abstract fun doAuthenticatedFindAll(filePath: String): List<RecordMeta>
    abstract fun doAuthenticatedGetInfo(): StorageInfo
    abstract fun doAuthenticatedDelete(meta: RecordMeta)

}
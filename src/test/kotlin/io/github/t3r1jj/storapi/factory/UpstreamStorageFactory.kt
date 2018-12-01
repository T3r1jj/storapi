package io.github.t3r1jj.storapi.factory

import io.github.t3r1jj.storapi.upstream.CleanableStorage
import io.github.t3r1jj.storapi.upstream.UpstreamStorage

interface UpstreamStorageFactory<S : UpstreamStorage> {
    fun createStorage(): S
    fun asCleanable(storage: S): CleanableStorage? = if (storage is CleanableStorage) storage else null
}
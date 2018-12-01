package io.github.t3r1jj.storapi.factory

import io.github.t3r1jj.storapi.upstream.UpstreamStorage

interface StorageFactory<S : UpstreamStorage> : UpstreamStorageFactory<S> {
    fun createStorageWithoutAccess(): S
}

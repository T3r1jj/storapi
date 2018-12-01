package io.github.t3r1jj.storapi.factory

import io.github.t3r1jj.storapi.upstream.UpstreamStorage

abstract class NamedByStorageFactory<S : UpstreamStorage> : UpstreamStorageFactory<S> {
    override fun toString(): String {
        return createStorage().toString()
    }
}
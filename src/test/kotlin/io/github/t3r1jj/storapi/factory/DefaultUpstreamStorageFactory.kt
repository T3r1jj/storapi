package io.github.t3r1jj.storapi.factory

import io.github.t3r1jj.storapi.upstream.UpstreamStorage
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class DefaultUpstreamStorageFactory(private val clazz: KClass<out UpstreamStorage>) :
        NamedByStorageFactory<UpstreamStorage>(), UpstreamStorageFactory<UpstreamStorage> {
    companion object {
        fun listOf(vararg clazz: KClass<out UpstreamStorage>): List<UpstreamStorageFactory<UpstreamStorage>> {
            return clazz.map { DefaultUpstreamStorageFactory(it) }
        }
    }

    override fun createStorage(): UpstreamStorage {
        return clazz.createInstance()
    }
}
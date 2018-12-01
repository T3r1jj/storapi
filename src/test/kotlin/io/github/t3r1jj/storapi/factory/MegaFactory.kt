package io.github.t3r1jj.storapi.factory

import io.github.t3r1jj.storapi.authenticated.mega.Mega
import io.github.t3r1jj.storapi.authenticated.AuthenticatedStorage

class MegaFactory : NamedByStorageFactory<AuthenticatedStorage>(), StorageFactory<AuthenticatedStorage> {
    companion object {
        private const val MEGA_USERNAME_TEST_KEY = "FCMS_TEST_MEGA_USERNAME"
        private const val MEGA_PASSWORD_TEST_KEY = "FCMS_TEST_MEGA_PASSWORD"
        private val userName = System.getenv(MEGA_USERNAME_TEST_KEY)
        private val password = System.getenv(MEGA_PASSWORD_TEST_KEY)
    }

    override fun createStorage(): AuthenticatedStorage {
        return Mega(userName, password)
    }

    override fun createStorageWithoutAccess(): AuthenticatedStorage {
        return Mega("", "")
    }
}
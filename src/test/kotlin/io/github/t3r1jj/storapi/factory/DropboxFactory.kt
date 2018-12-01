package io.github.t3r1jj.storapi.factory

import io.github.t3r1jj.storapi.authenticated.Dropbox
import io.github.t3r1jj.storapi.authenticated.AuthenticatedStorage

class DropboxFactory : NamedByStorageFactory<AuthenticatedStorage>(), StorageFactory<AuthenticatedStorage> {
    companion object {
        private const val DROPBOX_TOKEN_TEST_KEY = "FCMS_TEST_DROPBOX_ACCESS_TOKEN"
        private val accessToken = System.getenv(DROPBOX_TOKEN_TEST_KEY)
    }

    override fun createStorage(): AuthenticatedStorage {
        return Dropbox(accessToken)
    }

    override fun createStorageWithoutAccess(): AuthenticatedStorage {
        return Dropbox("")
    }
}
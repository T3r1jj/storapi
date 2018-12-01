package io.github.t3r1jj.storapi.data.exception

import io.github.t3r1jj.storapi.authenticated.AuthenticatedStorage

open class StorageUnauthenticatedException(message: String?, val storage: AuthenticatedStorage)
    : StorageException(message)
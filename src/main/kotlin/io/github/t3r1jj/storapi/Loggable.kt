package io.github.t3r1jj.storapi

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Use as extension to get logger for a class
 */
interface Loggable {
    fun Loggable.logger(): Logger {
        if (this::class.isCompanion) {
            return LoggerFactory.getLogger(this::class.java.enclosingClass)
        }
        return LoggerFactory.getLogger(this::class.java)
    }
}
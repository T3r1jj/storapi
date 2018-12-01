package io.github.t3r1jj.storapi.upstream

import io.github.t3r1jj.storapi.Storage
import io.github.t3r1jj.storapi.data.Record
import io.github.t3r1jj.storapi.data.RecordMeta
import java.util.function.Consumer

interface UpstreamStorage : Storage {
    /**
     * Returns [RecordMeta] for uploaded [record], filePath may be different than provided one.
     */
    fun upload(record: Record): RecordMeta


    fun upload(record: Record, bytesWrittenConsumer: Consumer<Long>?): RecordMeta

    /**
     * Returns [Record] for [filePath] from received [RecordMeta] from upload.
     */
    fun download(filePath: String): Record

    /**
     * Returns true if file at [filePath] received from [RecordMeta] from after upload is present, else returns false.
     */
    fun isPresent(filePath: String): Boolean
}
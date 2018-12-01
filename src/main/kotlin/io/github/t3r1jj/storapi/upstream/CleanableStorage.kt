package io.github.t3r1jj.storapi.upstream

import io.github.t3r1jj.storapi.Storage
import io.github.t3r1jj.storapi.data.RecordMeta

interface CleanableStorage: Storage {
    /**
     * Deletes [io.github.t3r1jj.fcms.external.data.Record] associated with [meta]. May require other info in [meta] than only path - original [meta] from [UpstreamStorage.upload] should be used.
     */
    fun delete(meta: RecordMeta)
}
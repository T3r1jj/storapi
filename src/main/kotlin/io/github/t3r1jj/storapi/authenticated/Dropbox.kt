package io.github.t3r1jj.storapi.authenticated

import com.dropbox.core.BadRequestException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.util.ProgressOutputStream
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.GetMetadataErrorException
import com.dropbox.core.v2.files.WriteMode
import com.dropbox.core.v2.users.FullAccount
import io.github.t3r1jj.storapi.Loggable
import io.github.t3r1jj.storapi.data.Record
import io.github.t3r1jj.storapi.data.RecordMeta
import io.github.t3r1jj.storapi.data.exception.StorageException
import io.github.t3r1jj.storapi.data.StorageInfo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.function.Consumer


open class Dropbox(private val accessToken: String) : AuthenticatedStorageTemplate() {
    companion object : Loggable {
        private val logger = logger()
    }

    private var client: DbxClientV2? = null
    private var account: FullAccount? = null

    override fun login() {
        try {
            val config = DbxRequestConfig.newBuilder("fcms").build()
            client = DbxClientV2(config, accessToken)
            account = client!!.users().currentAccount
        } catch (e: BadRequestException) {
            throw StorageException("Exception during login", e)
        }
    }

    override fun isLogged(): Boolean {
        return account?.name?.displayName?.isNotBlank() ?: false
    }

    override fun doAuthenticatedUpload(record: Record): RecordMeta {
        return doAuthenticatedUpload(record, null)
    }

    override fun doAuthenticatedUpload(record: Record, bytesWrittenConsumer: Consumer<Long>?): RecordMeta {
        val uploadBuilder = client!!.files()
                .uploadBuilder(record.path)
                .withMode(WriteMode.OVERWRITE)
        val result = if (bytesWrittenConsumer != null) {
            uploadBuilder.uploadAndFinish(record.data) { bytesWritten ->  bytesWrittenConsumer.accept(bytesWritten)}
        } else {
            uploadBuilder.uploadAndFinish(record.data)
        }
        return RecordMeta(record.name, record.path, result.size)
                .apply { id = result.id }
    }

    override fun doAuthenticatedDownload(filePath: String): Record {
        val os = ByteArrayOutputStream()
        val meta = client!!.files().downloadBuilder(filePath)
                .download(os)
        return Record(meta.name, meta.pathLower, ByteArrayInputStream(os.toByteArray()))
    }

    override fun doAuthenticatedDownload(filePath: String, bytesWrittenConsumer: Consumer<Long>?): Record {
        val os = ByteArrayOutputStream()
        val downloadBuilder = client!!.files().downloadBuilder(filePath)
        val meta = if (bytesWrittenConsumer != null) {
            downloadBuilder.download(ProgressOutputStream(os) { bytesWritten ->  bytesWrittenConsumer.accept(bytesWritten)})
        } else {
            downloadBuilder.download(os)
        }
        return Record(meta.name, meta.pathLower, ByteArrayInputStream(os.toByteArray()))
    }

    override fun doAuthenticatedFindAll(filePath: String): List<RecordMeta> {
        val meta = ArrayList<RecordMeta>()
        var result = client!!.files()
                .listFolderBuilder(filePath)
                .withRecursive(true)
                .start()
        do {
            for (metadata in result.entries) {
                if (metadata is FileMetadata) {
                    meta.add(RecordMeta(metadata.name, metadata.pathLower, metadata.size))
                }
            }
            result = client!!.files().listFolderContinue(result.cursor)
        } while (result.hasMore)
        return meta
    }

    override fun doAuthenticatedDelete(meta: RecordMeta) {
        client!!.files().deleteV2(meta.path)
    }

    override fun isPresent(filePath: String): Boolean {
        return try {
            !client!!.files().getMetadata(filePath).name.isEmpty()
        } catch (ex: GetMetadataErrorException) {
            if (ex.errorValue.isPath) {
                logger.info("File {} not present in storage ", filePath, ex)
                false
            } else {
                throw ex
            }
        } catch (il: IllegalArgumentException) {
            logger.info("File {} not present in storage ", filePath, il)
            false
        }
    }

    override fun doAuthenticatedGetInfo(): StorageInfo {
        val spaceUsage = client!!.users().spaceUsage
        return StorageInfo(this.toString(),
                BigInteger.valueOf(spaceUsage.allocation.individualValue.allocated),
                BigInteger.valueOf(spaceUsage.used)
        )
    }

    override fun logout() {
        client = null
        account = null
    }

}
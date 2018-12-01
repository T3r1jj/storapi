package io.github.t3r1jj.storapi.authenticated

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.TokenResponseException
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import io.github.t3r1jj.storapi.data.Record
import io.github.t3r1jj.storapi.data.RecordMeta
import io.github.t3r1jj.storapi.data.StorageInfo
import io.github.t3r1jj.storapi.data.exception.StorageException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.function.Consumer

open class GoogleDrive(private val clientId: String,
                       private val clientSecret: String,
                       private val refreshToken: String) : AuthenticatedStorageTemplate() {
    companion object {
        private const val FILE_FIELDS_DEFAULT_DESCRIPTION = "kind,incompleteSearch,files(kind,id,name,mimeType,description)"
        private const val FILE_FIELDS_DEFAULT_DESCRIPTION_SIZE = "kind,incompleteSearch,files(kind,id,name,mimeType,description,size)"
        private const val FILE_FIELDS_DEFAULT_UPLOAD_SIZE = "kind,id,name,mimeType,size"
        private const val ABOUT_FIELDS_QUOTA = "storageQuota(limit,usage)"
    }

    private var credential: Credential? = null
    private var drive: Drive? = null

    override fun login() {
        val httpTransport = NetHttpTransport()
        val jsonFactory = JacksonFactory()
        credential = GoogleCredential.Builder()
                .setJsonFactory(jsonFactory)
                .setTransport(httpTransport)
                .setClientSecrets(clientId, clientSecret)
                .build()
        credential!!.refreshToken = refreshToken
        drive = Drive.Builder(
                httpTransport, jsonFactory, credential)
                .setApplicationName("mydriveapp")
                .build()
        try {
            drive?.files()?.list()
                    ?.setPageSize(1)
                    ?.execute()
        } catch (e: TokenResponseException) {
            throw StorageException("Exception during login", e)
        }
    }

    override fun isLogged(): Boolean {
        return credential != null && drive != null && credential!!.accessToken != null
    }

    override fun doAuthenticatedUpload(record: Record): RecordMeta {
        return doAuthenticatedUpload(record, null)
    }

    override fun doAuthenticatedUpload(record: Record, bytesWrittenConsumer: Consumer<Long>?): RecordMeta {
        deleteBasedOnDescription(record.path)
        val fileMeta = File()
        fileMeta.name = record.name
        fileMeta.description = record.path
        val fileContent = FileContent("application/octet-stream", stream2file(record.data))
        val uploadBuilder = drive!!.files()
                .create(fileMeta, fileContent)
                .setFields(FILE_FIELDS_DEFAULT_UPLOAD_SIZE)
        if (bytesWrittenConsumer != null) {
            val httpUploader = uploadBuilder.mediaHttpUploader
            httpUploader.isDirectUploadEnabled = false
            httpUploader.chunkSize = MediaHttpUploader.MINIMUM_CHUNK_SIZE
            httpUploader.progressListener = MediaHttpUploaderProgressListener { uploader -> bytesWrittenConsumer.accept(uploader.numBytesUploaded) }
        }
        val result = uploadBuilder.execute()
        return RecordMeta(record.name, record.path, result.getSize())
    }

    override fun doAuthenticatedDownload(filePath: String): Record {
        return doAuthenticatedDownload(filePath, null)
    }

    override fun doAuthenticatedDownload(filePath: String, bytesWrittenConsumer: Consumer<Long>?): Record {
        val fileMeta = drive!!.files()
                .list()
                .setFields(FILE_FIELDS_DEFAULT_DESCRIPTION)
                .execute().files.last { it.description == filePath }
        val get = drive!!.files().get(fileMeta.id)
        if (bytesWrittenConsumer != null) {
            val httpDownloader = get.mediaHttpDownloader
            httpDownloader.isDirectDownloadEnabled = false
            httpDownloader.chunkSize = MediaHttpUploader.MINIMUM_CHUNK_SIZE
            httpDownloader.progressListener = MediaHttpDownloaderProgressListener { downloader -> bytesWrittenConsumer.accept(downloader.numBytesDownloaded) }
        }
        val data = ByteArrayOutputStream()
        get.executeMediaAndDownloadTo(data)
        return Record(fileMeta.name, filePath, ByteArrayInputStream(data.toByteArray()))
    }

    override fun doAuthenticatedFindAll(filePath: String): List<RecordMeta> {
        return drive!!.files()
                .list()
                .setFields(FILE_FIELDS_DEFAULT_DESCRIPTION_SIZE)
                .execute()
                .files
                .map {
                    RecordMeta(it.name,
                            if (it.description != null) it.description else "",
                            if (it.getSize() != null) it.getSize() else 0)
                }
    }

    override fun doAuthenticatedDelete(meta: RecordMeta) {
        if (meta.id != null) {
            drive!!.files().delete(meta.id)
        } else {
            deleteBasedOnDescription(meta.path)
        }
    }

    private fun deleteBasedOnDescription(path: String) {
        drive!!.files()
                .list()
                .setFields(FILE_FIELDS_DEFAULT_DESCRIPTION)
                .execute().files
                .filter { it.description == path }
                .map { it.id }
                .forEach { drive!!.files().delete(it).execute() }
    }

    override fun isPresent(filePath: String): Boolean {
        return drive!!.files()
                .list()
                .setFields(FILE_FIELDS_DEFAULT_DESCRIPTION)
                .execute().files
                .any { it.description == filePath }
    }

    override fun doAuthenticatedGetInfo(): StorageInfo {
        val about = drive!!.about()
                .get()
                .setFields(ABOUT_FIELDS_QUOTA)
                .execute()
        return StorageInfo(this.toString(),
                BigInteger.valueOf(about.storageQuota.limit),
                BigInteger.valueOf(about.storageQuota.usage))
    }

    override fun logout() {
        credential = null
        drive = null
    }
}
package io.github.t3r1jj.storapi.upstream

import io.github.t3r1jj.storapi.data.Record
import io.github.t3r1jj.storapi.data.RecordMeta
import io.github.t3r1jj.storapi.data.exception.StorageException
import io.github.t3r1jj.storapi.upstream.api.MegauploadErrorResponse
import io.github.t3r1jj.storapi.upstream.api.PutApi
import org.apache.commons.io.FileUtils
import java.net.HttpURLConnection
import java.net.URL
import java.util.function.Consumer

open class Put(baseUrl: String) : StorageClient<PutApi>(baseUrl, PutApi::class.java), UpstreamStorage, CleanableStorage {
    constructor() : this("https://api.put.re")

    override fun upload(record: Record): RecordMeta {
        return this.upload(record, null)
    }

    override fun upload(record: Record, bytesWrittenConsumer: Consumer<Long>?): RecordMeta {
        val (size, body) = createFileForm(record, bytesWrittenConsumer)
        val response = client.upload(body).execute()
        if (response.isSuccessful) {
            return RecordMeta(record.name, response.body()!!.data.link, size)
                    .apply { id = response.body()!!.data.deleteToken }
                    .apply { publicPath = path }
        } else {
            val error = gson.fromJson(response.errorBody()!!.charStream(), MegauploadErrorResponse::class.java)
            throw StorageException(error.error.message)
        }
    }

    override fun download(filePath: String): Record {
        val url = URL(filePath)
        val connection = url.openConnection()
        val fieldValue = connection.getHeaderField("Content-Disposition")
        try {
            val filename = fieldValue.substring(fieldValue.indexOf("filename=\"") + 10, fieldValue.length - 1)
            val tempFile = java.io.File.createTempFile(System.currentTimeMillis().toString(), null)
            tempFile.deleteOnExit()
            val inputStream = connection.getInputStream()
            FileUtils.copyInputStreamToFile(inputStream, tempFile)
            return Record(filename, filePath, tempFile.inputStream())
        } catch (e: RuntimeException) {
            throw StorageException("File not found", e)
        }
    }

    override fun isPresent(filePath: String): Boolean {
        val url = URL(filePath)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()
        val code = connection.responseCode
        return code == 200
    }

    override fun delete(meta: RecordMeta) {
        val response = client.delete(getIdFromPath(meta.path), meta.id!!).execute()
        if (!response.isSuccessful) {
            throw StorageException(response.errorBody()!!.string())
        }
    }

}
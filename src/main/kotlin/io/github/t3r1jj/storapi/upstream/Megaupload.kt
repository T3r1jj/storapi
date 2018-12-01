package io.github.t3r1jj.storapi.upstream

import io.github.t3r1jj.storapi.data.Record
import io.github.t3r1jj.storapi.data.RecordMeta
import io.github.t3r1jj.storapi.data.exception.StorageException
import io.github.t3r1jj.storapi.upstream.api.MegauploadApi
import io.github.t3r1jj.storapi.upstream.api.MegauploadErrorResponse
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import java.net.URL
import java.util.function.Consumer


open class Megaupload(baseUrl: String) : StorageInfoClient<MegauploadApi>(baseUrl, MegauploadApi::class.java), UpstreamStorage {
    constructor() : this("https://megaupload.nz")

    override fun upload(record: Record): RecordMeta {
        return upload(record, null)
    }

    override fun upload(record: Record, bytesWrittenConsumer: Consumer<Long>?): RecordMeta {
        val (size, body) = createFileForm(record, bytesWrittenConsumer)
        val response = client.upload(body).execute()
        if (response.isSuccessful) {
            return RecordMeta(record.name, response.body()!!.data.file.url.full, size)
                    .apply { publicPath = path }
        } else {
            val error = gson.fromJson(response.errorBody()!!.charStream(), MegauploadErrorResponse::class.java)
            throw StorageException(error.error.message)
        }
    }

    override fun download(filePath: String): Record {
        val doc = Jsoup.connect(filePath).get()
        val link = doc.select("#download-url")
        val header = doc.select("h1")
        val downloadPath = link.attr("href")
        val tempFile = java.io.File.createTempFile(System.currentTimeMillis().toString(), null)
        tempFile.deleteOnExit()
        FileUtils.copyURLToFile(URL(downloadPath), tempFile)
        return Record(header.text(), filePath, tempFile.inputStream())
    }

    override fun getInfo(filePath: String): RecordMeta {
        val response = client.getInfo(getIdFromPath(filePath)).execute()
        if (response.isSuccessful) {
            val info = response.body()!!
            return RecordMeta(info.data.file.metadata.name, filePath, info.data.file.metadata.size.bytes)
                    .apply { publicPath = path }
        } else {
            val error = gson.fromJson(response.errorBody()!!.charStream(), MegauploadErrorResponse::class.java)
            throw StorageException(error.error.message)
        }
    }

}


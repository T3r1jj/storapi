package io.github.t3r1jj.storapi.upstream

import com.google.gson.Gson
import io.github.t3r1jj.storapi.NamedStorage
import io.github.t3r1jj.storapi.data.Record
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.function.Consumer


abstract class StorageClient<T>(private val baseUrl: String, service: Class<T>) : NamedStorage() {
    protected val client = getRetrofit().create(service)!!
    protected val gson = Gson()

    private fun getRetrofit(): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
    }

    /**
     * @return pair of <size in bytes, file form data>
     */
    protected fun createFileForm(record: Record): Pair<Long, MultipartBody.Part> {
        val bytes = record.data.readBytes()
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), bytes)
        val body = MultipartBody.Part.createFormData("file", record.name, requestFile)
        return Pair(bytes.size.toLong(), body)
    }

    /**
     * @return pair of <size in bytes, file form data>
     */
    protected fun createFileForm(record: Record, bytesWrittenConsumer: Consumer<Long>?): Pair<Long, MultipartBody.Part> {
        val bytes = record.data.readBytes()
        val size = bytes.size.toLong()
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), bytes)
        if (bytesWrittenConsumer != null) {
            CountingRequestBody(requestFile, object : CountingRequestBody.Listener {
                override fun onRequestProgress(bytesWritten: Long, contentLength: Long) {
                    val progress = bytesWritten.toDouble() / contentLength
                    bytesWrittenConsumer.accept((progress * size).toLong())
                }
            })
        }
        val body = MultipartBody.Part.createFormData("file", record.name, requestFile)
        return Pair(size, body)
    }

    /**
     * @return id of file (second part of url from end after splitting by '/') or "null"string if invalid url
     */
    protected open fun getIdFromPath(filePath: String): String {
        val pathParts = java.lang.String(filePath).split("/")
        return if (pathParts.size > 1) {
            pathParts[pathParts.size - 2]
        } else {
            "null"
        }
    }

}
package io.github.t3r1jj.storapi.upstream.api

import com.google.gson.JsonObject

data class MegauploadSuccessfulResponse(val status: Boolean, val data: MegauploadData)
data class MegauploadData(val file: MegauploadFile)
data class MegauploadFile(val url: MegauploadUrl, val metadata: MegauploadMetadata)
data class MegauploadUrl(val full: String, val short: String)
data class MegauploadMetadata(val id: String,
                              val name: String,
                              val size: MegauploadSize)

data class MegauploadSize(val bytes: Long, val readable: String)
data class MegauploadErrorResponse(val status: Boolean, val error: MegauploadError)
data class MegauploadError(val message: String, val type: String, val code: Int)


data class OpenloadSuccessfulResponse(val status: Int, val message: String, val result: OpenloadResult)
data class OpenloadDefaultResponse(val status: Int, val message: String, val result: JsonObject)
data class OpenloadResult(val url: String, val valid_until: String)
data class OpenloadErrorResponse(val status: Int, val message: String)
data class OpenloadFileInfo(val id: String, val status: Int, val name: String, val size: Long)
data class OpenloadFileError(val id: String, val status: Int)

data class PutSuccessfulResponse(val status: String, val data: PutData)
data class PutData(val originalName: String, val name: String, val extension: String,
                   val deleteToken: String, val size: Long, val thumbnailLink: String,
                   val link: String, val deleteLink: String)
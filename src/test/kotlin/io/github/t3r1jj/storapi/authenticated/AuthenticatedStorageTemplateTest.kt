package io.github.t3r1jj.storapi.authenticated

import io.github.t3r1jj.storapi.data.Record
import io.github.t3r1jj.storapi.data.RecordMeta
import io.github.t3r1jj.storapi.data.exception.StorageUnauthenticatedException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import java.util.function.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


@ExtendWith(MockitoExtension::class)
@RunWith(JUnitPlatform::class)
class AuthenticatedStorageTemplateTest {

    private val filePath = "filepath"
    private val record = Record("name", filePath, "some text".byteInputStream())
    private val meta = RecordMeta("name", filePath, "some text".length.toLong())
    @Spy
    lateinit var storage: AuthenticatedStorageTemplate

    @Test
    fun testUploadFail() {
        doReturn(false).`when`(storage).isLogged()
        val sue = assertFailsWith(StorageUnauthenticatedException::class) {
            storage.upload(record)
        }
        assertEquals(storage, sue.storage)
    }

    @Test
    fun testDownloadFail() {
        doReturn(false).`when`(storage).isLogged()
        val sue = assertFailsWith(StorageUnauthenticatedException::class) {
            storage.download(filePath)
        }
        assertEquals(storage, sue.storage)
    }

    @Test
    fun testFindAllFail() {
        doReturn(false).`when`(storage).isLogged()
        val sue = assertFailsWith(StorageUnauthenticatedException::class) {
            storage.findAll(filePath)
        }
        assertEquals(storage, sue.storage)
    }

    @Test
    fun testGetInfoFail() {
        doReturn(false).`when`(storage).isLogged()
        val sue = assertFailsWith(StorageUnauthenticatedException::class) {
            storage.getInfo()
        }
        assertEquals(storage, sue.storage)
    }

    @Test
    fun testDeleteFail() {
        doReturn(false).`when`(storage).isLogged()
        val sue = assertFailsWith(StorageUnauthenticatedException::class) {
            storage.delete(meta)
        }
        assertEquals(storage, sue.storage)
    }

    @Test
    fun testUpload() {
        doReturn(true).`when`(storage).isLogged()
        storage.upload(record)
        verify(storage).doAuthenticatedUpload(record)
    }

    @Test
    fun testUploadProgress() {
        doReturn(true).`when`(storage).isLogged()
        val progressListener = Consumer<Long> { }
        storage.upload(record, progressListener)
        verify(storage).doAuthenticatedUpload(record, progressListener)
    }

    @Test
    fun testDownload() {
        doReturn(true).`when`(storage).isLogged()
        storage.download(filePath)
        verify(storage).doAuthenticatedDownload(filePath)
    }

    @Test
    fun testDownloadProgress() {
        doReturn(true).`when`(storage).isLogged()
        val progressListener = Consumer<Long> { }
        storage.download(filePath, progressListener)
        verify(storage).doAuthenticatedDownload(filePath, progressListener)
    }

    @Test
    fun testFindAll() {
        doReturn(true).`when`(storage).isLogged()
        storage.findAll(filePath)
        verify(storage).doAuthenticatedFindAll(filePath)
    }

    @Test
    fun testGetInfo() {
        doReturn(true).`when`(storage).isLogged()
        storage.getInfo()
        verify(storage).doAuthenticatedGetInfo()
    }

    @Test
    fun testDelete() {
        doReturn(true).`when`(storage).isLogged()
        storage.delete(meta)
        verify(storage).doAuthenticatedDelete(meta)
    }
}
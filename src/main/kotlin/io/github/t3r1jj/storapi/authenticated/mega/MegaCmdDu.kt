package io.github.t3r1jj.storapi.authenticated.mega

import com.github.eliux.mega.MegaUtils
import com.github.eliux.mega.cmd.AbstractMegaCmdCaller
import com.github.eliux.mega.error.MegaIOException
import io.github.t3r1jj.storapi.data.StorageInfo
import java.io.IOException
import java.math.BigInteger

internal class MegaCmdDu : AbstractMegaCmdCaller<StorageInfo>() {
    override fun getCmd(): String {
        return "du"
    }

    override fun call(): StorageInfo {
        try {
            val result = MegaUtils.execCmdWithOutput(executableCommand()).stream().skip(1)
                    .findFirst().orElse("/: 0")
            val results = trimSplit(result)
            return StorageInfo(
                    Mega::class.java.simpleName,
                    BigInteger.valueOf(16106130000),
                    results[1].toBigInteger()
            ) //no endpoint for total storage space, using default 15GB quota
        } catch (e: IOException) {
            throw MegaIOException("MegauploadError while executing du")
        }

    }

    private fun trimSplit(fileInfoStr: String): Array<out String> {
        return java.lang.String(java.lang.String(fileInfoStr)
                .replace("\\t", "\\s"))
                .split("\\s+")
    }
}
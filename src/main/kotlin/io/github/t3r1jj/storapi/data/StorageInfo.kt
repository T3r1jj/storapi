package io.github.t3r1jj.storapi.data

import java.math.BigInteger

data class StorageInfo(val name: String,
                       val totalSpace: BigInteger,
                       val usedSpace: BigInteger)
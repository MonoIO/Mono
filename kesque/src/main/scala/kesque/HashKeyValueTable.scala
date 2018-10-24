package kesque

import java.nio.ByteBuffer
import java.util.concurrent.locks.{Lock, ReentrantReadWriteLock}

import kafka.utils.Logging
import org.apache.kafka.common.record.CompressionType

object HashKeyValueTable {
  val fetchMaxBytesInLoadOffsets = 100 * 1024 * 1024 // 100M
  val defaultFetchMaxBytes = 4 * 1024 // 4K the size of SSD block

  def intToBytes(v: Int) = ByteBuffer.allocate(4).putInt(v).array
  def bytesToInt(v: Array[Byte]) = ByteBuffer.wrap(v).getInt
}

class HashKeyValueTable(
  topics:          Array[String],
  db:              Kesque,
  withTimeToKey:   Boolean,
  fetchMaxBytes:   Int             = HashKeyValueTable.defaultFetchMaxBytes,
  compressionType: CompressionType = CompressionType.NONE,
  cacheSize:       Int             = 10000
) extends Logging {
  /* time to key table, should be the first topic to initially create it */
  private var timeIndex = Array.ofDim[Array[Byte]](200)

  private val lock = new ReentrantReadWriteLock()
  private val readLock = lock.readLock
  private val writeLock = lock.writeLock

  def withLock[T <: Lock, V](r: => T)(f: () => V): V = {
    val lock: T = r
    try {
      lock.lock()
      f()
    } finally {
      lock.unlock()
    }
  }

  def getKeyByTime(timestamp: Long): Option[Array[Byte]] = {
    withLock(readLock) { () =>
      if (!withTimeToKey) {
        None
      } else {
        if (timestamp >= 0 && timestamp < timeIndex.length) {
          Option(timeIndex(timestamp.toInt))
        } else {
          None
        }
      }
    }
  }

}

package kesque

import java.util.Properties

import org.apache.kafka.common.record.CompressionType

import scala.collection.mutable

final class Kesque(props: Properties) {

  private val topicToTable = mutable.Map[String, HashKeyValueTable]()

  def getTimedTable(topics: Array[String], fetchMaxBytes: Int = 4096, compressionType: CompressionType = CompressionType.NONE, cacheSize: Int = 10000) = {
    topicToTable.getOrElseUpdate(topics.mkString(","), new HashKeyValueTable(topics, this, true, fetchMaxBytes, compressionType, cacheSize))
  }

}

final case class TVal(value: Array[Byte], timestamp: Long)
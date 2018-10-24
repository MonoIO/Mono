package mono.store

import mono.Hash
import mono.domain.BlockHeader
import mono.util.SimpleMap
import mono.network.p2p.messages.PV62.BlockHeaderImplicits._
import mono.store.datasource.KesqueDataSource

class BlockHeaderStorage(val source: KesqueDataSource) extends SimpleMap[Hash, BlockHeader, BlockHeaderStorage] {
  val namespace: Array[Byte] = Namespaces.HeaderNamespace
  def keySerializer: Hash => Array[Byte] = _.bytes
  def valueSerializer: BlockHeader => Array[Byte] = _.toBytes
  def valueDeserializer: Array[Byte] => BlockHeader = b => b.toBlockHeader
  /**
    * This function updates the KeyValueStore by deleting, updating and inserting new (key-value) pairs.
    *
    * @param toRemove which includes all the keys to be removed from the KeyValueStore.
    * @param toUpsert which includes all the (key-value) pairs to be inserted into the KeyValueStore.
    *                 If a key is already in the DataSource its value will be updated.
    * @return the new DataSource after the removals and insertions were done.
    */
  override def update(toRemove: Set[Hash], toUpsert: Map[Hash, BlockHeader]) = ???

  def getBlockHash(blockNumber: Long) = source.table.getKeyByTime(blockNumber).map(Hash(_))
}

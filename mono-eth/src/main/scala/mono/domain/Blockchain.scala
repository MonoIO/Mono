package mono.domain

import mono.Hash
import mono.ledger.{BlockWorldState, TrieStorage}
import mono.network.p2p.messages.PV62.BlockBody
import mono.store.BlockchainStorages
import mono.vm.{Storage, WorldState}

object Blockchain {
  trait I[S <: Storage[S], W <: WorldState[W, S]] {
    /**
      * Allows to query a blockHeader by block hash
      *
      * @param hash of the block that's being searched
      * @return [[BlockHeader]] if found
      */
    def getBlockHeaderByHash(hash: Hash): Option[BlockHeader]

    def getBlockHeaderByNumber(number: Long): Option[BlockHeader] = {
      for {
        hash <- getHashByBlockNumber(number)
        header <- getBlockHeaderByHash(hash)
      } yield header
    }

    /**
      * Allows to query for a block based on it's number
      *
      * @param number Block number
      * @return Block if it exists
      */
    def getBlockByNumber(number: Long): Option[Block] =
      for {
        hash <- getHashByBlockNumber(number)
        block <- getBlockByHash(hash)
      } yield block

    /**
      * Allows to query a blockBody by block hash
      *
      * @param hash of the block that's being searched
      * @return [[mono.network.p2p.messages.PV62.BlockBody]] if found
      */
    def getBlockBodyByHash(hash: Hash): Option[BlockBody]

    /**
      * Returns a block hash given a block number
      *
      * @param number Number of the searchead block
      * @return Block hash if found
      */
    def getHashByBlockNumber(number: Long): Option[Hash]

    /**
      * Allows to query for a block based on it's hash
      *
      * @param hash of the block that's being searched
      * @return Block if found
      */
    def getBlockByHash(hash: Hash): Option[Block] =
      for {
        header <- getBlockHeaderByHash(hash)
        body <- getBlockBodyByHash(hash)
      } yield Block(header, body)
  }


  def apply(storages: BlockchainStorages): Blockchain =
    new Blockchain(storages)
}
class Blockchain(val storages: BlockchainStorages) extends Blockchain.I[TrieStorage, BlockWorldState] {
  private val blockHeaderStorage = storages.blockHeaderStorage
  /**
    * Allows to query a blockHeader by block hash
    *
    * @param hash of the block that's being searched
    * @return [[BlockHeader]] if found
    */
  override def getBlockHeaderByHash(hash: Hash) = ???

  /**
    * Allows to query a blockBody by block hash
    *
    * @param hash of the block that's being searched
    * @return [[mono.network.p2p.messages.PV62.BlockBody]] if found
    */
  override def getBlockBodyByHash(hash: Hash) = ???

  /**
    * Returns a block hash given a block number
    *
    * @param number Number of the searchead block
    * @return Block hash if found
    */
  override def getHashByBlockNumber(number: Long) = blockHeaderStorage.getBlockHash(number)
}

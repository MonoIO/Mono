package mono.store

import mono.store.datasource.KesqueDataSource

trait BlockchainStorages {

  def blockHeaderStorage: BlockHeaderStorage

  def blockHeaderDataSource: KesqueDataSource

}

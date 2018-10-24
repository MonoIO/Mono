package mono.store

import mono.store.datasource.DataSources

object Storages {

  trait DefaultStorages extends Storages with DataSources {
    lazy val appStateStorage = new AppStateStorage(appStateDataSource)
    lazy val blockHeaderStorage = new BlockHeaderStorage(blockHeaderDataSource)
  }

}

trait Storages extends BlockchainStorages {

}
package mono.service

import java.io.File

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import kesque.Kesque
import mono.domain.Blockchain
import mono.ledger.Ledger
import mono.store.Storages
import mono.store.datasource.{KesqueDataSource, SharedLeveldbDataSources}
import mono.{Mono, util}
import mono.util.{BlockchainConfig, MiningConfig, TxPoolConfig}
import mono.validators._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object ServiceBoard extends ExtensionId[ServiceBoardExtension] with ExtensionIdProvider {
  override def lookup = ServiceBoard
  override def createExtension(system: ExtendedActorSystem) = new ServiceBoardExtension(system)
}

class ServiceBoardExtension(system: ExtendedActorSystem) extends Extension {
  import system.dispatcher

  val config = util.Config.config

  val txPoolConfig = TxPoolConfig(config)

  val blockchainConfig = BlockchainConfig(config)

  val miningConfig = MiningConfig(config)

  val storages = new Storages.DefaultStorages with SharedLeveldbDataSources {
    implicit protected val system = ServiceBoardExtension.this.system

    private lazy val monoPath = new File("./")//.getParentFile.getParentFile
    private lazy val configDir = new File(monoPath, "conf")
    private lazy val kafkaConfigFile = new File(configDir, "kafka.server.properties")
    private lazy val kafkaProps = {
      val props = org.apache.kafka.common.utils.Utils.loadProps(kafkaConfigFile.getAbsolutePath)
      props.put("log.dirs", util.Config.kesqueDir)
      props
    }

    lazy val kesque = new Kesque(kafkaProps)

    private val futureTables = Future.sequence(List(
      Future(kesque.getTimedTable(Array(
        KesqueDataSource.header,
        KesqueDataSource.body,
        KesqueDataSource.receipts,
        KesqueDataSource.td
      ), 102400))
    ))

    private val List(blockTable) = Await.result(futureTables, Duration.Inf)

    lazy val blockHeaderDataSource = new KesqueDataSource(blockTable, KesqueDataSource.header)

  }
  // There should be only one instance, instant it here or a standalone singleton service
  val blockchain: Blockchain = Blockchain(storages)

  val validators = new Validators {
    val blockValidator: BlockValidator = BlockValidator
    val blockHeaderValidator: BlockHeaderValidator.I = new BlockHeaderValidator(blockchainConfig)
    val ommersValidator: OmmersValidator.I = new OmmersValidator(blockchainConfig)
    val signedTransactionValidator = new SignedTransactionValidator(blockchainConfig)
  }

  val ledger: Ledger.I = new Ledger(blockchain, blockchainConfig)(system)
}

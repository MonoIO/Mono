package mono.network.p2p.messages

import mono.domain.{BlockHeader, SignedTransaction}
import mono.rlp
import mono.rlp.{RLPEncodeable, RLPList, RLPSerializable}
import mono.rlp.RLPImplicitConversions._
import mono.rlp.RLPImplicits._

object PV62 {

  final case class BlockBody(transactionList: Seq[SignedTransaction], uncleNodesList: Seq[BlockHeader]) {
    override def toString: String =
      s"""BlockBody{
         |transactionList: $transactionList
         |uncleNodesList: $uncleNodesList
         |}
    """.stripMargin
  }

  object BlockHeaderImplicits {

    def toBlockHeader(rlpEncodeable: RLPEncodeable): BlockHeader =
      rlpEncodeable match {
        case RLPList(parentHash, ommersHash, beneficiary, stateRoot, transactionsRoot, receiptsRoot,
        logsBloom, difficulty, number, gasLimit, gasUsed, unixTimestamp, extraData, mixHash, nonce) =>
          //BlockHeader(parentHash, ommersHash, beneficiary, stateRoot, transactionsRoot, receiptsRoot,
          //  logsBloom, rlp.toUInt256(difficulty), number, gasLimit, gasUsed, unixTimestamp, extraData, mixHash, nonce)
          BlockHeader(parentHash, stateRoot, transactionsRoot, receiptsRoot, logsBloom, rlp.toUInt256(difficulty), number, gasLimit, gasUsed)
      }

    implicit final class BlockHeaderEnc(blockHeader: BlockHeader) extends RLPSerializable {
      override def toRLPEncodable: RLPEncodeable = {
        import blockHeader._
        RLPList(parentHash)
      }
    }

    implicit final class BlockheaderDec(val bytes: Array[Byte]) {
      def toBlockHeader: BlockHeader = BlockheaderEncodableDec(rlp.rawDecode(bytes)).toBlockHeader
    }

    implicit final class BlockheaderEncodableDec(val rlpEncodeable: RLPEncodeable) {
      def toBlockHeader: BlockHeader = BlockHeaderImplicits.toBlockHeader(rlpEncodeable)
    }
  }

}

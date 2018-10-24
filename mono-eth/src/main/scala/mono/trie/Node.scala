package mono.trie

import mono.{rlp, trie}
import mono.rlp._

object Node {
  import mono.rlp.RLPImplicitConversions._
  import mono.rlp.RLPImplicits._

  object nodeEnc extends RLPEncoder[Node] {
    override def encode(obj: Node): RLPEncodeable = obj match {
      case LeafNode(key, value) =>
        RLPList(HexPrefix.encode(nibbles = key, isLeaf = true), value)

      case ExtensionNode(sharedKey, next) =>
        RLPList(HexPrefix.encode(nibbles = sharedKey, isLeaf = false), next match {
          case Right(node) => encode(node)
          case Left(bytes) => bytes
        })

      case BranchNode(children, terminator) =>
        val childrenEncoded = children.map {
          case Some(Right(node)) => encode(node)
          case Some(Left(bytes)) => RLPValue(bytes)
          case None              => RLPValue(Array.emptyByteArray)
        }
        val encoded = Array.ofDim[RLPEncodeable](childrenEncoded.length + 1)
        System.arraycopy(childrenEncoded, 0, encoded, 0, childrenEncoded.length)
        encoded(encoded.length - 1) = RLPValue(terminator.getOrElse(Array.emptyByteArray))

        RLPList(encoded: _*)
    }
  }

  object nodeDec extends RLPDecoder[Node] {
    override def decode(rlp: RLPEncodeable): Node = ???
  }
}
/**
  * When store node to storage, the key is node.hash, the value is node.encoded.
  * When node is changed, the key will always change too. Thus, to a specified
  * key, the value should be null or the same node
  */
sealed trait Node {
  lazy val encoded: Array[Byte] = rlp.encode[Node](this)(Node.nodeEnc)
  lazy val hash: Array[Byte] = trie.toHash(encoded)

  final def capped: Array[Byte] = if (encoded.length < 32) encoded else hash
}


final case class LeafNode(key: Array[Byte], value: Array[Byte]) extends Node

object ExtensionNode {
  /**
    * This function creates a new ExtensionNode with next parameter as its node pointer
    *
    * @param sharedKey of the new ExtensionNode.
    * @param next      to be inserted as the node pointer (and hashed if necessary).
    * @return a new BranchNode.
    */
  def apply(sharedKey: Array[Byte], next: Node): ExtensionNode = {
    val nextCapped = next.capped
    ExtensionNode(sharedKey, if (nextCapped.length == 32) Left(nextCapped) else Right(next))
  }
}
final case class ExtensionNode(sharedKey: Array[Byte], next: Either[Array[Byte], Node]) extends Node

final case class BranchNode(children: Array[Option[Either[Array[Byte], Node]]], terminator: Option[Array[Byte]]) extends Node {

}
package mono.trie

import java.util.Arrays

import akka.util.ByteString
import mono._
import mono.store.trienode.NodeKeyValueStorage
import mono.util.SimpleMap

object MerklePatriciaTrie {

  def apply[K, V](source: NodeKeyValueStorage)(implicit kSerializer: ByteArrayEncoder[K], vSerializer: ByteArraySerializable[V]): MerklePatriciaTrie[K, V] =
    new MerklePatriciaTrie[K, V](None, source, Map())(kSerializer, vSerializer)

  def apply[K, V](rootHash: Array[Byte], source: NodeKeyValueStorage)(implicit kSerializer: ByteArrayEncoder[K], vSerializer: ByteArraySerializable[V]): MerklePatriciaTrie[K, V] = {
    if (Arrays.equals(EmptyTrieHash, rootHash)) {
      new MerklePatriciaTrie[K, V](None, source, Map())(kSerializer, vSerializer)
    } else {
      new MerklePatriciaTrie[K, V](Some(rootHash), source, Map())(kSerializer, vSerializer)
    }
  }
}

final class MerklePatriciaTrie[K, V] private (
  rootHashOpt:          Option[Array[Byte]],
  nodeStorage:          NodeKeyValueStorage,
  private var nodeLogs: Map[Hash, Log[Array[Byte]]]
)(implicit kSerializer: ByteArrayEncoder[K], vSerializer: ByteArraySerializable[V]) extends SimpleMap[K, V, MerklePatriciaTrie[K, V]] {

  // The root hash will be already here via a series of put/remove operations
  lazy val rootHash: Array[Byte] = rootHashOpt.getOrElse(EmptyTrieHash)
  /**
    * This function inserts a (key-value) pair into the trie. If the key is already asociated with another value it is updated.
    *
    * @param key
    * @param value
    * @return New trie with the (key-value) pair inserted.
    * @throws mono.trie.MerklePatriciaTrie.MPTException if there is any inconsistency in how the trie is build.
    */
  override def put(key: K, value: V): MerklePatriciaTrie[K, V] = {
    val keyNibbles = HexPrefix.bytesToNibbles(kSerializer.toBytes(key))

    rootHashOpt map { rootId =>
      val root = getNode(rootId)
    }

    this

  }

  // --- node storage related
  private def getNode(nodeId: Array[Byte]): Node = {
    val encoded = nodeId
    rlp.decode[Node](encoded)(Node.nodeDec)
  }

  /**
    * Compose trie by toRemove and toUpsert
    *
    * @param toRemove which includes all the keys to be removed from the KeyValueStore.
    * @param toUpsert which includes all the (key-value) pairs to be inserted into the KeyValueStore.
    *                 If a key is already in the DataSource its value will be updated.
    * @return the new trie after the removals and insertions were done.
    */
  override def update(toRemove: Set[K], toUpsert: Map[K, V]): MerklePatriciaTrie[K, V] = {
    throw new UnsupportedOperationException("Use put/remove")
  }

  def persist(): MerklePatriciaTrie[K, V] = {
    val changes = nodeLogs.collect {
      case (k, Deleted(_)) => k -> None
      case (k, Updated(v)) => k -> Some(v)
    }
    nodeStorage.update(changes)
    this
  }
}
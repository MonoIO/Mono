package mono.util

trait SimpleMap[K, V, T <: SimpleMap[K, V, T]] {

  /**
    * This function inserts a (key-value) pair into the trie. If the key is already asociated with another value it is updated.
    *
    * @param key
    * @param value
    * @return New trie with the (key-value) pair inserted.
    */
  def put(key: K, value: V): T = update(key -> Some(value))

  final def update(change: (K, Option[V])): T = {
    change match {
      case (k, None)    => update(Set(k), Map())
      case (k, Some(v)) => update(Set(), Map(k -> v))
    }
  }

  /**
    * Since the remove may still have to be saved to reposity, we'll let same key
    * in both toRemove and toUpsert
    */
  final def update(changes: Iterable[(K, Option[V])]): T = {
    val (toRemove, toUpsert) = changes.foldLeft((Set[K](), Map[K, V]())) {
      case ((toRemove, toUpsert), (k, None))    => (toRemove + k, toUpsert)
      case ((toRemove, toUpsert), (k, Some(v))) => (toRemove, toUpsert + (k -> v))
    }
    update(toRemove, toUpsert)
  }

  /**
    * This function updates the KeyValueStore by deleting, updating and inserting new (key-value) pairs.
    *
    * @param toRemove which includes all the keys to be removed from the KeyValueStore.
    * @param toUpsert which includes all the (key-value) pairs to be inserted into the KeyValueStore.
    *                 If a key is already in the DataSource its value will be updated.
    * @return the new DataSource after the removals and insertions were done.
    */
  def update(toRemove: Set[K], toUpsert: Map[K, V]): T

}

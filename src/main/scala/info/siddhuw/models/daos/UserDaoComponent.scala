package info.siddhuw.models.daos

/**
 * @author Siddhu Warrier
 */

trait UserDaoComponent[T] {
  def userDao: UserDao[T]
}

trait UserDao[T] {
  /**
   *
   * Find an object by ID. Returns Some(obj[T]) if found, None if not, and throws an exception if there's more
   * than one object with that ID
   *
   * @param id ID to find
   * @return
   *         Some, None, or exception
   */
  def findById(id: String): Option[T]
}

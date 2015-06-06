package info.siddhuw.services

import info.siddhuw.models.TwitterUser

/**
 * @author Siddhu Warrier
 */


trait RedisService {
  def setTokenFor(user: TwitterUser): String
  def isTokenValid(token: String): Boolean
  def expire(token: String): Unit
}

package info.siddhuw.models

import org.squeryl.annotations.Column

/**
 * @author Siddhu Warrier
 */

case class TwitterUser(@Column("screen_name") screenName: String)

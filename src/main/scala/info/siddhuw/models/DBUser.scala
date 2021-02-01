package info.siddhuw.models

import org.squeryl.annotations.Column

/**
 * @author Siddhu Warrier
 */

case class DBUser(username: String, @Column(length = 2048, name = "pw_hash") pwHash: String, @Column(length = 2048) salt: String)

case class InputUser(username: String, password: String)
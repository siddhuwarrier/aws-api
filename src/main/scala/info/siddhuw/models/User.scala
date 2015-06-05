package info.siddhuw.models

import org.squeryl.annotations.Column

/**
 * @author Siddhu Warrier
 * @date 05/06/15.
 */

case class User(username: String, @Column("pw_hash") pwHash: String, salt: String, 
                @Column("access_key") accessKey: String, @Column("secret_access_key") secretAccessKey: String)

object User {
  val CLIENT_APP_USERNAME = "client_app"
}



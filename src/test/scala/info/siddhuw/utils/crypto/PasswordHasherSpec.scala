package info.siddhuw.utils.crypto

import org.owasp.esapi.reference.crypto.JavaEncryptor
import org.scalatest.{FlatSpec, Matchers}

/**
 * @author Siddhu Warrier
 */

class PasswordHasherSpec extends FlatSpec with Matchers {
  val password = "stupid-password"

  "The password hash" should "be validatable" in {
    val (pwHash, salt) = PasswordHasher.hash(password)
    PasswordHasher.hash(password)

    JavaEncryptor.getInstance().hash(password, salt) should equal(pwHash)
  }

  it should "not produce the same hash when applied multiple times to the same plaintext" in {
    val numAttempts = 10
    val pwHashes = List.fill(numAttempts)(PasswordHasher.hash(password)._1).toSet

    //converting to set should eliminate duplicates. But there should be none
    pwHashes.size should equal(numAttempts)
  }
}

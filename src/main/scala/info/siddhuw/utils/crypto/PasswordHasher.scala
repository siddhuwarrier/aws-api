package info.siddhuw.utils.crypto

import java.security.SecureRandom

import com.google.common.io.BaseEncoding
import org.owasp.esapi.reference.crypto.JavaEncryptor

/**
 * @author Siddhu Warrier
 */

object PasswordHasher {
  private val SALT_SIZE = 16
  private val RANDOM_GEN_ALGO = "SHA1PRNG"
  private val ENCRYPTOR = JavaEncryptor.getInstance()
  private val BASE_ENCODER = BaseEncoding.base64()
  /**
   *
   * @param password The password to hash
   * @return salted hash, salt used to encode hash
   */
  def hash(password: String): (String, String) = {
    val salt = new Array[Byte](SALT_SIZE)

    val sr = SecureRandom.getInstance(RANDOM_GEN_ALGO)
    sr.nextBytes(salt)
    val encodedSalt = BASE_ENCODER.encode(salt)

    (hash(password, encodedSalt), encodedSalt)
  }

  /**
   * This method is useful to validate hashes
   *
   * @param password the password to hash
   * @param salt the salt for the hash function
   * @return salted hash
   */
  def hash(password: String, salt: String) = {
    ENCRYPTOR.hash(password, salt)
  }
}

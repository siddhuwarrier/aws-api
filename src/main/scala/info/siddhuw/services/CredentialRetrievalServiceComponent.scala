package info.siddhuw.services

import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods._
import org.scribe.model.OAuthRequest

import scala.concurrent.{ ExecutionContext, Future }

/**
 * @author Siddhu Warrier
 */

trait CredentialRetrievalServiceComponent {
  def credentialRetrievalService: CredentialRetrievalService
}

trait CredentialRetrievalService {
  def getCredentials(signedRequest: OAuthRequest)(implicit ec: ExecutionContext): Future[String]
}


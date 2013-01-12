package io.svc.security

import io.svc.security.std.{AuthenticationFailure, UsernamePasswordCredentials}
import scalaz.{Success, Failure, Validation}

/**
 * @author Rintcius Blok
 */
object http {

  trait CredentialsExtractionFailure extends AuthenticationFailure

  /**
   * If input is not according to basic authentication scheme
   * @param input the input
   */
  case class BasicAuthenticationParseFailure(input: String) extends CredentialsExtractionFailure
  case object NoBasicAuthenticationInputSuppliedFailure extends CredentialsExtractionFailure


  def extractCredentialsBasicAuthentication(input: Option[String]): Validation[CredentialsExtractionFailure, UsernamePasswordCredentials] = {
    input match {
      case None => Failure(NoBasicAuthenticationInputSuppliedFailure)
      case Some(s) => extractCredentialsBasicAuthentication(s)
    }
  }

  /**
   * Extracts credentials that are provided according to the basic authentication scheme
   *
   * @see http://tools.ietf.org/html/rfc2617
   * @param input the input
   * @return
   */
  def extractCredentialsBasicAuthentication(input: String): Validation[CredentialsExtractionFailure, UsernamePasswordCredentials] = {

    if (input.startsWith("Basic")) {
      try {
        val str = new String(javax.xml.bind.DatatypeConverter.parseBase64Binary(input.replaceFirst("Basic", "")), "UTF-8")

        val index = str.indexOf(":")
        if (index != -1) {
          val username = str.substring(0, index)
          val password = str.substring(index + 1)
          Success(UsernamePasswordCredentials(username, password))
        } else {
          Failure(BasicAuthenticationParseFailure(input))
        }

      } catch {
        case e:RuntimeException => Failure(BasicAuthenticationParseFailure(input))
      }
    } else {
      Failure(BasicAuthenticationParseFailure(input))
    }
  }

}

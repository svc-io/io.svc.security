package io.svc.security

import javax.xml.bind.DatatypeConverter
import io.svc.security.std.UsernamePasswordCredentials
import scalaz.{Failure, Success}
import io.svc.security.http.{BasicAuthenticationParseFailure, CredentialsExtractionFailure}
import org.specs2.mutable.Specification

/**
 * @author Rintcius Blok
 */
class httpTest extends Specification {

  "basic authentication extraction" should {
    "correctly extract an encoded username and password" in {
      okTemplate("userName:password", UsernamePasswordCredentials("userName", "password"))
    }
    "correctly extract an encoded username without password" in {
      okTemplate("userName:", UsernamePasswordCredentials("userName", ""))
    }
    "correctly extract an encoded password without username" in {
      okTemplate(":password", UsernamePasswordCredentials("", "password"))
    }
    "correctly extract an encoded string without username and password" in {
      okTemplate(":", UsernamePasswordCredentials("", ""))
    }
    "correctly extract an encoded username and password with colons" in {
      okTemplate("userName::pass:word::", UsernamePasswordCredentials("userName", ":pass:word::"))
    }
    "return a BasicAuthenticationParseFailure if the supplied string does not start with Basic" in {
      nokTemplate("x", BasicAuthenticationParseFailure("x"))
    }
    "return a BasicAuthenticationParseFailure if the supplied string is not base64 after Basic" in {
      nokTemplate("Basicx", BasicAuthenticationParseFailure("Basicx"))
    }
    "return a BasicAuthenticationParseFailure if the supplied string does not contain a colon" in {
      val testStr = "userNamePasswordNoColon"
      val encoded = DatatypeConverter.printBase64Binary(testStr.getBytes("UTF-8"))
      nokTemplate("Basic" + encoded, BasicAuthenticationParseFailure("BasicdXNlck5hbWVQYXNzd29yZE5vQ29sb24="))
    }
  }

  private def okTemplate(testStr: String, expected: UsernamePasswordCredentials) {
    val encoded = DatatypeConverter.printBase64Binary(testStr.getBytes("UTF-8"))
    val actual = http.extractCredentialsBasicAuthentication("Basic" + encoded)
    actual must_== Success(expected)
  }

  private def nokTemplate(testStr: String, expected: CredentialsExtractionFailure) {
    val actual = http.extractCredentialsBasicAuthentication(testStr)
    actual must_== Failure(expected)
  }

}

package io.svc.security

import org.specs2.mutable.Specification
import io.svc.security.authentication._
import test.simple.authentication._
import io.svc.security.std._
import io.svc.security.user.{UsernamePasswordCredentialsAuthenticationService, CredentialsValidator}
import scalaz._

/**
 * @author Rintcius Blok
 */
class stdTest extends Specification {
  val joe = SimpleUser("Joe", "password4joe", "joe@mymail.com")

  val auth = new Authentication[SimpleRequest, SimpleResult, SimpleUser, AuthenticationFailure] {
    val inputValidator = new CredentialsInputValidator[SimpleRequest, UsernamePasswordCredentials, SimpleUser, AuthenticationFailure] {
      val credentialsExtractor = new CredentialsExtractor[SimpleRequest, UsernamePasswordCredentials, AuthenticationFailure] {
        override def extractCredentials(in: SimpleRequest): Validation[AuthenticationFailure, UsernamePasswordCredentials] = {
          val oCred = for {
            username <- in.username
            password <- in.password
          } yield UsernamePasswordCredentials(username, password)
          oCred map (Success(_)) getOrElse Failure(AuthenticationServiceFailure("cannot extract credentials"))
        }
      }
      val authService = new UsernamePasswordCredentialsAuthenticationService[SimpleUser] {
        val userService = new StdInMemoryUserService[String](Seq(joe))
        val credentialsValidator = new CredentialsValidator[SimpleUser, UsernamePasswordCredentials, AuthenticationFailure] {
          override def validate[ASimpleUser, AUsernamePasswordCredentials](user: ASimpleUser, credentials: AUsernamePasswordCredentials) = {
            if (credentials.asInstanceOf[UsernamePasswordCredentials].password == user.asInstanceOf[SimpleUser].password) {
              Success(user)
            } else {
              Failure(AuthenticationServiceFailure("invalid password"))
            }
          }
        }
      }
    }
    val authFailureHandler = new AuthenticationFailureHandler[SimpleRequest, AuthenticationFailure, SimpleResult] {
      override def onAuthenticationFailure(request: SimpleRequest, failure: AuthenticationFailure) = SimpleFailureResult(request, failure.toString)
    }
  }
  def echo(req: SimpleRequest, user: SimpleUser): SimpleResult = SimpleSuccessResult(req, user)

  def authenticatedEcho = auth.authentication(echo)

  //TODO define the failures properly

  "StdInMemoryAuthenticationService" should {

    "result in CannotExtractCredentials failure when not providing username/password" in {
      val helloReq = SimpleRequest("hello")
      authenticatedEcho(helloReq) must_== SimpleFailureResult(helloReq, AuthenticationServiceFailure("cannot extract credentials").toString)
    }

    "result in UsernameNotFound failure when providing username that is not there" in {
      val helloReq = SimpleRequest("hello", Some("Jim"), Some("dontcare"))
      authenticatedEcho(helloReq) must_== SimpleFailureResult(helloReq, AuthenticationServiceFailure(UsernameNotFound("Jim")).toString)
    }

    "result in InvalidPassword failure when providing invalid password" in {
      val helloReq = SimpleRequest("hello", Some("Joe"), Some("invalid"))
      authenticatedEcho(helloReq) must_== SimpleFailureResult(helloReq, AuthenticationServiceFailure("invalid password").toString)
    }

    "result in success when providing valid password" in {
      val helloReq = SimpleRequest("hello", Some("Joe"), Some("password4joe"))
      authenticatedEcho(helloReq) must_== SimpleSuccessResult(helloReq, joe)
    }
  }
}

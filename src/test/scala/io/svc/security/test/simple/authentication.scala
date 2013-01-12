package io.svc.security.test.simple

import io.svc.security.authentication._
import scalaz.Validation
import scalaz.Failure
import scalaz.Success
import io.svc.security.user.{UserWithUsername, UserProvider}

/**
 * Simple implementation of [[io.svc.security.authentication]] used for testing.
 *
 * @see io.svc.security.authentication
 * @author Rintcius Blok
 */
object authentication {

  case class SimpleRequest(request: String, username: Option[String] = None, password: Option[String] = None)

  case class SimpleUser(username: String, password: String, email: String) extends UserWithUsername[String]

  case class SimpleRequestWithUser()

  trait SimpleResult
  case class SimpleFailureResult(request: SimpleRequest, failure: String) extends SimpleResult
  case class SimpleSuccessResult(request: SimpleRequest, user: SimpleUser) extends SimpleResult

  case class SimpleCredentials(username: String, password: String)

  object SimpleAuthService extends AuthenticationService[SimpleCredentials, SimpleUser, String] {
    override def authenticate(credentials: SimpleCredentials): Validation[String, SimpleUser] = {
      if (credentials.password == ("secret4" + credentials.username)) {
        Success(SimpleUser(credentials.username, credentials.password, credentials.username + "@mymail.com"))
      } else {
        Failure("Invalid credentials for " + credentials.username)
      }
    }
  }

  object SimpleCredentialsExtractor extends CredentialsExtractor[SimpleRequest, SimpleCredentials, String] {
    override def extractCredentials(in: SimpleRequest): Validation[String, SimpleCredentials] = {
      val oCred = for {
        username <- in.username
        password <- in.password
      } yield SimpleCredentials(username, password)
      oCred map (Success(_)) getOrElse Failure("no user or password in request")
    }
  }

  object SimpleInputValidator extends InputValidator[SimpleRequest, SimpleUser, String] {
    override def validateInput(request: SimpleRequest): Validation[String, SimpleUser] = {
      val oUser = for {
        username <- request.username
        password <- request.password
      } yield SimpleUser(username, password, username + "@mymail.com")
      oUser map (Success(_)) getOrElse Failure("user or password is not supplied")
    }
  }

  val dummyUser = SimpleUser("dummy", "secret4dummy", "dummy@mymail.com")

  object SimpleUserProvider extends UserProvider[SimpleUser] {
    def user = dummyUser
  }

  object SimpleAuthFailureHandler extends AuthenticationFailureHandler[SimpleRequest, String, SimpleResult] {
    override def onAuthenticationFailure(request: SimpleRequest, failure: String) = SimpleFailureResult(request, failure)
  }

  /**
   * 'Full' authentication process, using all traits defined in [[io.svc.security.authentication]].
   */
  val fullAuth = new Authentication[SimpleRequest, SimpleResult, SimpleUser, String] {
    val inputValidator = new CredentialsInputValidator[SimpleRequest, SimpleCredentials, SimpleUser, String] {
      val credentialsExtractor = SimpleCredentialsExtractor
      val authService = SimpleAuthService
    }

    val authFailureHandler = SimpleAuthFailureHandler

  }


  /**
   * Authentication process with a simpler input validation strategy than fullAuth.
   */
  val auth = new Authentication[SimpleRequest, SimpleResult, SimpleUser, String] {
    val inputValidator = SimpleInputValidator
    val authFailureHandler = SimpleAuthFailureHandler
  }

  /**
   * Authentication process that does not do any authentication.
   */
  val noAuth = new NoAuthentication[SimpleRequest, SimpleResult, SimpleUser] {
    val userProvider = SimpleUserProvider
  }

  def simpleActionWithUser(request: SimpleRequest, user: SimpleUser): SimpleResult = {
    if (request.request.toLowerCase.contains("hello")) {
      SimpleSuccessResult(request, user)
    } else {
      SimpleFailureResult(request, "request does not contain hello")
    }
  }

  val simpleAuthAction = auth.authentication(simpleActionWithUser)

  val simpleFullAuthAction = fullAuth.authentication(simpleActionWithUser)

  val simpleNoAuthAction = noAuth.authentication(simpleActionWithUser)
}

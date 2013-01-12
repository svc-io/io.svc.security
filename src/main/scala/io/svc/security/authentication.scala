package io.svc.security

import scalaz.{Success, Failure, Validation}
import io.svc.security.user.UserProvider
import io.svc.security.std.UsernamePasswordCredentials


/**
 * Traits defining the AuthenticationProcess in an abstract way. These traits are meant to be extended,
 * typically resulting in a binding to an application framework (such as the Play 2 binding in io.svc.security.play).
 * This binding can then be used as security framework inside the application framework.
 *
 * E.g. in order to bind to Play2 these type parameters will be instantiated to Play types as follows:
 * - I (input type) => play.api.mvc.Request
 * - O (output type) => play.api.mvc.Response
 * The other type parameters will typically not be instantiated to a type that is part of the application framework. E.g.:
 * - U (type representing a user) => will either be instantiated to a type of io.svc.security or an application specific type
 *
 * @author Rintcius Blok
 */
object authentication {

  /**
   * Base trait for the authentication process. This process will normally embed [[io.svc.security.authentication.AuthenticationService]] in an environment,
   * but there are shortcuts in the process available that do not require [[io.svc.security.authentication.AuthenticationService]], the easiest being
   * [[io.svc.security.authentication.NoAuthentication]]
   * @tparam I input type
   * @tparam O output type
   * @tparam U type representing a user
   */
  trait AuthenticationProcess[I, O, U] {
    /**
     * Authenticate an action (or not ;) )
     * @param action the action to authenticate
     * @return
     */
    def authentication(action: (I, U) => O): I => O
  }

  /**
   * Shortcuts the authentication process by directly executing the action without authentication.
   * @tparam I input type
   * @tparam O output type
   * @tparam U type representing a user
   */
  trait NoAuthentication[I, O, U] extends AuthenticationProcess[I, O, U] {

    val userProvider: UserProvider[U]

    override def authentication(action: (I, U) => O): I => O = {
      in: I =>
        action(in, userProvider.user)
    }
  }

  /**
   * Trait that captures the common authentication process.
   * @tparam I input type
   * @tparam O output type
   * @tparam U type representing a user
   * @tparam F failure type
   */
  trait Authentication[I, O, U, F] extends AuthenticationProcess[I, O, U] {

    val inputValidator: InputValidator[I, U, F]
    val authFailureHandler: AuthenticationFailureHandler[I, F, O]

    /**
     * Authenticates the supplied action. It first validates the input.
     * If validation fails then the AuthenticationFailureHandler will handle
     * the input further. If it succeeds the action will be performed.
     * @param action the action to authenticate
     * @return
     */
    override def authentication(action: (I, U) => O): I => O = {
      in: I =>
        inputValidator.validateInput(in).fold(
          failure = { f => authFailureHandler.onAuthenticationFailure(in, f) },
          success = { user => action(in, user) }
        )
    }
  }

  /**
   * Trait that validates input.
   * @tparam I input type
   * @tparam U type representing a user
   * @tparam F failure type
   */
  trait InputValidator[-I, +U, +F] {
    /**
     * Validate the provided input
     * @param in the input to validate
     * @return the result of the validation
     */
    def validateInput(in: I): Validation[F, U]
  }

  /**
   * Trait for handling authentication failures.
   * @tparam I input type
   * @tparam F failure type
   * @tparam O output type
   */
  trait AuthenticationFailureHandler[I, F, O] {
    def onAuthenticationFailure(in: I, failure: F): O
  }

  /**
   * Trait that defines a strategy to validate input (thus extending InputValidator) in terms of a provided
   * CredentialsExtractor and AuthenticationService.
   * @tparam In input type
   * @tparam Credentials credentials type
   * @tparam User type representing a user
   * @tparam F failure type
   */
  trait CredentialsInputValidator[In, Credentials, User, +F] extends InputValidator[In, User, F] {

    val credentialsExtractor: CredentialsExtractor[In, Credentials, F]
    val authService: AuthenticationService[Credentials, User, F]

    /**
     * Validate the provided input by extracting credentials as defined by CredentialsExtractor.
     * If the credentials cannot be extracted a failure is returned. Otherwise the extracted credentials
     * will be used to authenticate against AuthenticationService and that result will be returned.
     * @param in the input to validate
     * @return the result of the validation
     */
    override def validateInput(in: In): Validation[F, User] = {
      credentialsExtractor.extractCredentials(in).fold(
        failure = { f => Failure(f) },
        success = { user => authService.authenticate(user) }
      )
    }
  }

  trait CredentialsExtractor[In, +Credentials, +F] {

    def extractCredentials(in: In): Validation[F, Credentials]
  }

  /**
   * The authentication service
   * @tparam Credentials credentials type
   * @tparam User user type
   * @tparam F failure type
   */
  trait AuthenticationService[-Credentials, User, +F] {
    def authenticate(credentials: Credentials): Validation[F, User]
  }
}

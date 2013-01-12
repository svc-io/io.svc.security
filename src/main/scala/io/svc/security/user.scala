package io.svc.security

import scalaz.{Success, Failure, Validation}
import io.svc.security.authentication.AuthenticationService
import io.svc.security.std.{AuthenticationServiceFailure, AuthenticationFailure, UsernamePasswordCredentials}

/**
 * @author Rintcius Blok
 */
object user {

  trait UserProvider[+User] {
    def user: User
  }

  trait UserWithUsername[+Username] {
    def username: Username
  }

  trait UserService[Username, +Failure] {
    def findByUsername(username: Username): Validation[Failure, UserWithUsername[Username]]
  }

  trait CredentialsValidator[+User, +Credentials, +Failure] {
    def validate[A >: User, B >: Credentials](user: A, credentials: B): Validation[Failure, A]
  }

  trait UsernamePasswordCredentialsAuthenticationService[User <: UserWithUsername[String]] extends AuthenticationService[UsernamePasswordCredentials, User, AuthenticationFailure] {
    val userService: UserService[String, AuthenticationFailure]
    val credentialsValidator: CredentialsValidator[User, UsernamePasswordCredentials, AuthenticationFailure]
    override def authenticate(credentials: UsernamePasswordCredentials): Validation[AuthenticationFailure, User] = {
      //TODO get rid of the asInstanceOf
      val oUser: Validation[io.svc.security.std.AuthenticationFailure,User] = userService.findByUsername(credentials.username).asInstanceOf[Validation[io.svc.security.std.AuthenticationFailure,User]]
      oUser match {
        case Failure(f) => Failure(AuthenticationServiceFailure(f))
          //TODO get rid of the asInstanceOf
        case Success(user) => credentialsValidator.validate[UserWithUsername[String], UsernamePasswordCredentials](user: UserWithUsername[String], credentials: UsernamePasswordCredentials).asInstanceOf[Validation[io.svc.security.std.AuthenticationFailure,User]]
      }
    }
  }

  trait UsersProvider[+Username] {
    def users: Seq[UserWithUsername[Username]]
  }
}

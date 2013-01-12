package io.svc.security

import org.specs2.mutable.Specification
import test.simple.authentication._

/**
 * @author Rintcius Blok
 */
class authenticationTest extends Specification {

  val simpleRequest_noCredentials = SimpleRequest("hello")
  val simpleRequest_invalidCredentials = SimpleRequest("hello", Some("Joe"), Some("invalid"))
  val simpleRequest_validCredentials = SimpleRequest("hello", Some("Joe"), Some("secret4Joe"))

  val joe = SimpleUser("Joe", "secret4Joe", "Joe@mymail.com")

  "action without authentication" should {
    "return a success result if no credentials are provided" in {
      val res = simpleNoAuthAction(simpleRequest_noCredentials)
      SimpleSuccessResult(simpleRequest_noCredentials, dummyUser) must_==  res
    }
    "return a success result if invalid credentials are provided" in {
      val res = simpleNoAuthAction(simpleRequest_invalidCredentials)
      SimpleSuccessResult(simpleRequest_invalidCredentials, dummyUser) must_== res
    }
    "return a success result if valid credentials are provided" in {
      val res = simpleNoAuthAction(simpleRequest_validCredentials)
      SimpleSuccessResult(simpleRequest_validCredentials, dummyUser) must_== res
    }
  }

  "action with simple authentication accepting any username/password" should {
    "return a failure result if no credentials are provided" in {
      val res = simpleAuthAction(simpleRequest_noCredentials)
      SimpleFailureResult(simpleRequest_noCredentials, "user or password is not supplied") must_== res
    }
    "return a success result if invalid credentials are provided" in {
      val res = simpleAuthAction(simpleRequest_invalidCredentials)
      SimpleSuccessResult(simpleRequest_invalidCredentials, joe.copy(password = "invalid")) must_== res
    }
    "return a success result if valid credentials are provided" in {
      val res = simpleAuthAction(simpleRequest_validCredentials)
      SimpleSuccessResult(simpleRequest_validCredentials, joe) must_== res
    }
  }

  "action with full authentication" should {
    "return a failure result if no credentials are provided" in {
      val res = simpleFullAuthAction(simpleRequest_noCredentials)
      SimpleFailureResult(simpleRequest_noCredentials, "no user or password in request") must_== res
    }
    "return a failure result if invalid credentials are provided" in {
      val res = simpleFullAuthAction(simpleRequest_invalidCredentials)
      SimpleFailureResult(simpleRequest_invalidCredentials, "Invalid credentials for Joe") must_== res
    }
    "return a success result if valid credentials are provided" in {
      val res = simpleFullAuthAction(simpleRequest_validCredentials)
      SimpleSuccessResult(simpleRequest_validCredentials, joe) must_== res
    }
  }
}

package io.svc.security

import scalaz.{Success, Failure, Validation}
import io.svc.security.user.{UsersProvider, UserService, UserWithUsername}
import io.svc.security.std.{UsernameNotFound, UserServiceFailure}

/**
 * @author Rintcius Blok
 */
object inMemory {

  trait InMemoryUserService[Username] extends UserService[Username, UserServiceFailure] {

    val usersProvider: UsersProvider[Username]

    lazy val userMap: Map[Username, UserWithUsername[Username]] =
      usersProvider.users.foldLeft (Map(): Map[Username, UserWithUsername[Username]]) { (map: Map[Username, UserWithUsername[Username]], user: UserWithUsername[Username]) => addEntry(map, user) }

    override def findByUsername(username: Username): Validation[UserServiceFailure, UserWithUsername[Username]] = {
      userMap.get(username) map { Success(_) } getOrElse(Failure(UsernameNotFound(username)))
    }
  }

  private def addEntry[Username](m: Map[Username, UserWithUsername[Username]], user: UserWithUsername[Username]): Map[Username, UserWithUsername[Username]] = {
    m + (user.username -> user)
  }

}

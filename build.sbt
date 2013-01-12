name := "io-svc-security"

organization := "io.svc"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.1"

//logLevel := Level.Debug

//resolvers := Seq("local nexus public" at "http://localhost:8081/nexus/content/groups/public")

resolvers := Seq("Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/")

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10-M1" % "test"

libraryDependencies += "org.specs2" %% "specs2" % "1.12.3" % "test"

credentials += Credentials(Path.userHome / ".m2" / ".credentials")

publishTo <<= version {
      v: String =>
        val nexus = "http://localhost:8081/"
        if (v.trim.endsWith("SNAPSHOT")) {
          Some("snapshots" at nexus + "nexus/content/repositories/snapshots")
        }
        else {
          Some("releases" at nexus + "nexus/content/repositories/releases")
        }
    }

publishMavenStyle := true

pomIncludeRepository := {
  x => false
}

seq(aetherPublishSettings: _*)

testOptions in Test += Tests.Argument("junitxml")
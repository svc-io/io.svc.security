name := "io-svc-security"

organization := "io.svc"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.1"

licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))

homepage := Some(url("https://github.com/svc-io/io.svc.security"))

//logLevel := Level.Debug

resolvers := Seq("Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/")

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10-M1" % "test"

libraryDependencies += "org.specs2" %% "specs2" % "1.12.3" % "test"

credentials += Credentials(Path.userHome / ".m2" / "sonatype.credentials")

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

pomIncludeRepository := {
  x => false
}

testOptions in Test += Tests.Argument("junitxml")

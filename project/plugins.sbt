resolvers += Resolver.url("sbt-plugin-releases", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.6")

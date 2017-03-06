resolvers ++= Seq(
  "hmrc-sbt-plugin-releases" at "https://dl.bintray.com/hmrc/sbt-plugin-releases",
	"Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "1.4.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "0.9.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.12")

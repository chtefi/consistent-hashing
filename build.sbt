name := "testanythinghere"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "org.http4s" %% "http4s-core" % "0.18.1"
libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.18.1" % Test
libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"
libraryDependencies += "org.typelevel" %% "cats-free" % "1.0.1"
//libraryDependencies += "org.typelevel" %% "cats-effect" % "1.0.1"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"
libraryDependencies += "io.monix" %% "monix" % "3.0.0-RC1"

scalacOptions ++= Seq(
  // See other posts in the series for other helpful options
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ypartial-unification"
)

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
libraryDependencies += "io.estatico" %% "newtype" % "0.3.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
libraryDependencies += "com.github.ssedano" % "jump-consistent-hash" % "1.0.0"

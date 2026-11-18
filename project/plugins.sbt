// kubuszok plugin (bundles: sbt-git, sbt-pgp, sbt-scalafmt, sbt-commandmatrix, sbt-scalajs, sbt-scala-native, and more)
// sbt 2.0 has projectMatrix built in; sbt-welcome has no sbt2 build and is no longer bundled.
// TODO: remove after sbt-kubuszok 0.2.5
resolvers += "Maven Central Snapshots" at "https://central.sonatype.com/repository/maven-snapshots"
addSbtPlugin("com.kubuszok" % "sbt-kubuszok" % "0.2.4-4-g52226ed-SNAPSHOT")

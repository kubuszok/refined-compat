// kubuszok plugin (bundles: sbt-git, sbt-pgp, sbt-scalafmt, sbt-commandmatrix, sbt-scalajs, sbt-scala-native, and more)
// sbt 2.0 has projectMatrix built in; sbt-welcome has no sbt2 build and is no longer bundled.
addSbtPlugin("com.kubuszok" % "sbt-kubuszok" % "0.2.3-10-g736e4c8-SNAPSHOT")

// TEMP: sbt-kubuszok snapshot under test
resolvers += "Maven Central Snapshots" at "https://central.sonatype.com/repository/maven-snapshots/"

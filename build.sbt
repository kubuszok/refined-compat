import kubuszok.sbt._
import kubuszok.sbt.KubuszokPlugin.autoImport._
import sbtwelcome.UsefulTask
import commandmatrix.extra.*

Global / allowUnsafeScalaLibUpgrade := true

// Versions:

val versions = new {
  val scala213 = "2.13.16"
  val scala3 = "3.3.7"

  val scalas = List(scala213, scala3)
  val platforms = List(VirtualAxis.jvm, VirtualAxis.js, VirtualAxis.native)

  val hearth = "0.3.0-22-gc856d5b-SNAPSHOT"
  val refined = "0.11.3"
  val munit = "1.2.4"
}

// Common settings:

val settings = Seq(
  scalacOptions ++= foldVersion(scalaVersion.value)(
    for2_13 = Seq("-Xsource:3", "-language:implicitConversions"),
    for3 = Seq("-language:implicitConversions")
  )
)

val publishSettings = Seq(
  organization := "com.kubuszok",
  homepage := Some(url("https://github.com/kubuszok/refined-compat")),
  organizationHomepage := Some(url("https://kubuszok.com")),
  licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/kubuszok/refined-compat/"),
      "scm:git:git@github.com:kubuszok/refined-compat.git"
    )
  ),
  startYear := Some(2026),
  developers := List(
    Developer("MateuszKubuszok", "Mateusz Kubuszok", "", url("https://github.com/MateuszKubuszok"))
  ),
  pomExtra := (
    <issueManagement>
      <system>GitHub issues</system>
      <url>https://github.com/kubuszok/refined-compat/issues</url>
    </issueManagement>
  ),
  projectType := ProjectType.ScalaLibrary
)

val noPublishSettings =
  Seq(projectType := ProjectType.NonPublished)

val resolverSettings = Seq(
  resolvers += mavenCentralSnapshots,
  resolvers += Resolver.mavenLocal
)

// Cross-quotes plugin for Scala 3:

val useCrossQuotes = versions.scalas.flatMap { scalaVersion =>
  foldVersion(scalaVersion)(
    for2_13 = List(
      MatrixAction {
        case (version, List(VirtualAxis.jvm)) => version.isScala2
        case _                                => false
      }.Configure(_.settings(libraryDependencies += "com.kubuszok" %% "hearth-cross-quotes" % versions.hearth)),
      MatrixAction {
        case (version, List(VirtualAxis.js)) => version.isScala2
        case _                               => false
      }.Configure(_.settings(libraryDependencies += "com.kubuszok" %% "hearth-cross-quotes" % versions.hearth)),
      MatrixAction {
        case (version, List(VirtualAxis.native)) => version.isScala2
        case _                                   => false
      }.Configure(_.settings(libraryDependencies += "com.kubuszok" %% "hearth-cross-quotes" % versions.hearth))
    ),
    for3 = List(
      MatrixAction
        .ForScala(_.isScala3)
        .Configure(
          _.settings(
            libraryDependencies += "com.kubuszok" %% "hearth-cross-quotes" % versions.hearth % Provided,
            scalacOptions ++= {
              val pluginJar = (Compile / dependencyClasspath).value
                .find(_.data.getName.contains("hearth-cross-quotes"))
                .map(_.data.getAbsolutePath)
                .getOrElse(sys.error("hearth-cross-quotes jar not found on classpath"))
              Seq(s"-Xplugin:$pluginJar")
            }
          )
        )
    )
  )
}

// Modules:

lazy val al = new Aliases(published = Seq(compat, tests))

lazy val root = project
  .in(file("."))
  .enablePlugins(KubuszokRootPlugin)
  .settings(publishSettings)
  .settings(noPublishSettings)
  .settings(
    name := "refined-compat-root",
    logo :=
      s"""refined-compat ${version.value} for (${versions.scala213}, ${versions.scala3}) x (JVM, Scala.js, Scala Native)
         |
         |This build uses sbt-projectmatrix:
         | - Scala JVM adds no suffix to a project name seen in build.sbt
         | - Scala.js adds the "JS" suffix to a project name seen in build.sbt
         | - Scala Native adds the "Native" suffix to a project name seen in build.sbt
         | - Scala 2.13 adds no suffix to a project name seen in build.sbt
         | - Scala 3 adds the suffix "3" to a project name seen in build.sbt
         |""".stripMargin,
    usefulTasks := al.usefulTasks()
  )
  .aggregate(compat.projectRefs *)
  .aggregate(tests.projectRefs *)

lazy val compat = projectMatrix
  .in(file("compat"))
  .someVariations(versions.scalas, versions.platforms)(useCrossQuotes *)
  .settings(
    moduleName := "refined-compat",
    name := "refined-compat",
    description := "Compile-time refinement validation for refined types on Scala 3, powered by Hearth's semiEval"
  )
  .settings(settings *)
  .settings(publishSettings *)
  .settings(resolverSettings *)
  .settings(
    libraryDependencies ++= Seq(
      "com.kubuszok" %%% "hearth" % versions.hearth,
      "eu.timepit" %%% "refined" % versions.refined
    )
  )

lazy val tests = projectMatrix
  .in(file("tests"))
  .someVariations(versions.scalas, List(VirtualAxis.jvm))()
  .settings(
    moduleName := "refined-compat-tests",
    name := "refined-compat-tests"
  )
  .settings(settings *)
  .settings(publishSettings *)
  .settings(noPublishSettings *)
  .settings(resolverSettings *)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalameta" %%% "munit" % versions.munit % Test
    )
  )
  .dependsOn(compat)

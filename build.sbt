import kubuszok.sbt._
import kubuszok.sbt.KubuszokPlugin.autoImport._
import commandmatrix.extra.*

Global / allowUnsafeScalaLibUpgrade := true

// Versions:
// sbt 2.0's build dialect drops the structural-type refinement on `new { ... }`, so a `versions`
// object's members no longer resolve from lifted setting expressions. Use plain top-level vals
// (the proven sbt-2.0 pattern).

val scala213 = "2.13.18"
val scala3 = "3.3.8"

val scalas = List(scala213, scala3)
val platforms = List(VirtualAxis.jvm, VirtualAxis.js, VirtualAxis.native)

val hearthVersion = "0.4.0"
val refinedVersion = "0.11.4"
val munitVersion = "1.3.3"

// Common settings:

val settings = Seq(
  scalacOptions ++= foldVersion(scalaVersion.value)(
    for2_13 = Seq("-Xsource:3", "-language:implicitConversions", "-release", "11"),
    for3 = Seq("-language:implicitConversions", "-release", "11")
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
    Developer("MateuszKubuszok", "Mateusz Kubuszok", "", url("https://kubuszok.com"))
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

val useCrossQuotes = scalas.flatMap { scalaVersion =>
  foldVersion(scalaVersion)(
    for2_13 = List(
      MatrixAction {
        case (version, List(VirtualAxis.jvm)) => version.isScala2
        case _                                => false
      }.Configure(_.settings(libraryDependencies += "com.kubuszok" %% "hearth-cross-quotes" % hearthVersion)),
      MatrixAction {
        case (version, List(VirtualAxis.js)) => version.isScala2
        case _                               => false
      }.Configure(_.settings(libraryDependencies += "com.kubuszok" %% "hearth-cross-quotes" % hearthVersion)),
      MatrixAction {
        case (version, List(VirtualAxis.native)) => version.isScala2
        case _                                   => false
      }.Configure(_.settings(libraryDependencies += "com.kubuszok" %% "hearth-cross-quotes" % hearthVersion))
    ),
    for3 = List(
      MatrixAction
        .ForScala(_.isScala3)
        .Configure(
          // On Scala 3 cross-quotes is a *compiler plugin* and is published for JVM only — the same
          // plugin jar is reused across all platforms (JVM/JS/Native). In sbt 2.0 `%%` is now
          // platform-aware (it would try to resolve `hearth-cross-quotes_sjs1_3` / `_native0.5_3`,
          // which do not exist), so reference the JVM artifact directly with single `%` + literal
          // `_3` suffix as a Provided dependency (kept off the runtime classpath; the jar is wired
          // into the compiler via the `-Xplugin` option resolved from the dependency classpath below).
          _.settings(
            libraryDependencies += "com.kubuszok" % "hearth-cross-quotes_3" % hearthVersion % Provided,
            scalacOptions ++= {
              // sbt 2.0: classpath entries are xsbti.HashedVirtualFileRef (no getName/getAbsolutePath);
              // resolve to a real path via the build's fileConverter.
              val converter = fileConverter.value
              val pluginJar = (Compile / dependencyClasspath).value
                .map(ref => converter.toPath(ref.data).toAbsolutePath)
                .find(_.getFileName.toString.contains("hearth-cross-quotes"))
                .map(_.toString)
                .getOrElse(sys.error("hearth-cross-quotes jar not found on classpath"))
              Seq(s"-Xplugin:$pluginJar")
            }
          )
        )
    )
  )
}

// Lazy vals not relying on sun.misc.Unsafe (futureproofing for JDKs that remove it):
// On the 3.3 LTS line lazy vals use the legacy bitmap encoding that breaks under newer JDKs.
// `-Yfuture-lazy-vals` opts into the new encoding (built-in on 3.4+) but requires a Java output
// version >= 9. The shared `settings` above already sets `-release 11`, which pins the output
// version to 11 (>= 9), so the flag works on its own — adding an explicit `-java-output-version`
// would only clash with `-release` ("flag -java-output-version set repeatedly"). Apply ONLY on
// 3.3.8 + JVM; never on 2.13, never on non-JVM (Scala Native 0.5.12 crashes on the flag), never on 3.4+.

val futureLazyVals = MatrixAction
  .ForPlatform(VirtualAxis.jvm)
  .Configure(
    _.settings(
      scalacOptions ++= {
        if (scalaVersion.value == scala3) Seq("-Yfuture-lazy-vals")
        else Seq.empty
      }
    )
  )

// Modules:

// Convenience aliases. sbt-welcome (which previously exposed `logo`/`usefulTasks`) has no sbt 2.0
// build and is no longer bundled by sbt-kubuszok, so the help-menu tasks are registered as aliases
// instead. In a projectMatrix build the root aggregates every Scala-version/platform variation, so a
// plain aggregated task already covers all of them (no `+` cross-stepping needed). Note: in sbt 2.0
// the bare `test` task is incremental/machine-cached; `testFull` forces a real, full run.
addCommandAlias("compileAll", "compile")
addCommandAlias("testAll", "testFull")
addCommandAlias("publishLocalAll", "publishLocal")

lazy val root = project
  .in(file("."))
  .enablePlugins(KubuszokRootPlugin)
  .settings(publishSettings)
  .settings(noPublishSettings)
  .settings(
    name := "refined-compat-root"
  )
  .aggregate(compat.projectRefs *)
  .aggregate(tests.projectRefs *)

lazy val compat = projectMatrix
  .in(file("compat"))
  .someVariations(scalas, platforms)((useCrossQuotes :+ futureLazyVals) *)
  .settings(
    moduleName := "refined-compat",
    name := "refined-compat",
    description := "Compile-time refinement validation for refined types on Scala 3, powered by Hearth's semiEval"
  )
  .settings(settings *)
  .settings(publishSettings *)
  .settings(resolverSettings *)
  .settings(
    // sbt 2.0: %% is platform-aware (encodes Scala version + JS/Native suffix); %%% is gone.
    libraryDependencies ++= Seq(
      "com.kubuszok" %% "hearth" % hearthVersion,
      "eu.timepit" %% "refined" % refinedVersion
    )
  )

lazy val tests = projectMatrix
  .in(file("tests"))
  .someVariations(scalas, List(VirtualAxis.jvm))(futureLazyVals)
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
      "org.scalameta" %% "munit" % munitVersion % Test
    )
  )
  .dependsOn(compat)

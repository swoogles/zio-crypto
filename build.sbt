import BuildHelper._

inThisBuild(
  List(
    organization := "dev.zio",
    homepage := Some(url("https://zio.github.io/zio-crypto/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "jdegoes",
        "John De Goes",
        "john@degoes.net",
        url("http://degoes.net")
      )
    ),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc")
  )
)

addCommandAlias("prepare", "fix; fmt")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("fix", "scalafixAll")
addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheckAll")
addCommandAlias("fixCheck", "scalafixAll --check")

val zioVersion  = "1.0.11"
val tinkVersion = "1.6.1"

lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true
  )
  .aggregate(zioCryptoJVM)

lazy val zioCrypto = crossProject(JVMPlatform)
  .in(file("zio-crypto"))
  .settings(stdSettings("zio-crypto"))
  .settings(crossProjectSettings)
  .settings(buildInfoSettings("zio.crypto"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"               %% "zio"          % zioVersion,
      "dev.zio"               %% "zio-test"     % zioVersion % "test",
      "dev.zio"               %% "zio-test-sbt" % zioVersion % "test",
      "com.google.crypto.tink" % "tink"         % tinkVersion
    )
  )
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val zioCryptoJVM = zioCrypto.jvm
  .settings(dottySettings)

lazy val docs = project
  .in(file("zio-crypto-docs"))
  .dependsOn(zioCrypto.jvm)
  .settings(
    skip.in(publish) := true,
    moduleName := "zio-crypto-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion
    ),
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(root),
    target in (ScalaUnidoc, unidoc) := (baseDirectory in LocalRootProject).value / "website" / "static" / "api",
    cleanFiles += (target in (ScalaUnidoc, unidoc)).value,
    docusaurusCreateSite := docusaurusCreateSite.dependsOn(unidoc in Compile).value,
    docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(unidoc in Compile).value
  )
  .dependsOn(zioCryptoJVM)
  .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)

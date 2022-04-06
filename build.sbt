name := "weos"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-math3" % "3.0",
  javaJdbc,
  javaEbean,
  cache,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.apache.poi" % "poi" % "3.17"
)

play.Project.playJavaSettings

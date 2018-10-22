organization := "io.uacf.gg"

name := "helpful-avro"

version := "0.2.2"

scalaVersion := "2.12.4"

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

libraryDependencies ++= Seq(
  "io.spray"               %% "spray-json"               % "1.3.4",
  "com.github.scopt"       %% "scopt"                    % "3.7.0",
  "org.apache.avro"         % "avro"                     % "1.8.2",
  "org.scalatest"          %% "scalatest"                % "3.0.0"  % "test"
)

scalacOptions in ThisBuild ++= Seq(
  "-Xlog-implicits",
  "-feature"
)

packMain := Map("app" -> "")

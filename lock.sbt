// DON'T EDIT THIS FILE.
// This file is auto generated by sbt-lock 0.6.0.
// https://github.com/tkawachi/sbt-lock/
dependencyOverrides ++= {
  if (!(sbtLockHashIsUpToDate in ThisBuild).value && sbtLockIgnoreOverridesOnStaleHash.value) {
    Seq.empty
  } else {
    Seq(
      "org.scalactic" % "scalactic_2.12" % "3.0.5"
    )
  }
}
// LIBRARY_DEPENDENCIES_HASH 3a68d5f9bc7e4cf06a65f16baee5586e1b7e4720
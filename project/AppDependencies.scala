/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._

object AppDependencies {

  private val play26Version = "2.6.25"
  private val play27Version = "2.7.4"
  private val play28Version = "2.8.8"

  val compileCommon: Seq[ModuleID] = Seq(
    "joda-time"          % "joda-time"     % "2.10.1",
    "org.apache.commons" % "commons-lang3" % "3.4"
  )

  val compilePlay26: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.4.20",
    "uk.gov.hmrc"       %% "mongo-lock" % "7.0.0-play-26",
    "com.typesafe.play" %% "play"       % play26Version
  )

  val compilePlay27: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "mongo-lock" % "7.0.0-play-27",
    "com.typesafe.play" %% "play"       % play27Version
  )

  val compilePlay28: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "mongo-lock" % "7.0.0-play-28",
    "com.typesafe.play" %% "play"       % play28Version
  )

  val testCommon: Seq[ModuleID] = Seq(
    "org.scalatest"     %% "scalatest"      % "3.0.7"   % Test,
    "org.pegdown"       % "pegdown"         % "1.6.0"   % Test,
    "ch.qos.logback"    % "logback-classic" % "1.2.3"   % Test,
    "org.mockito"       % "mockito-core"    % "3.9.0"   % Test,
    "org.scalatestplus" %% "mockito-3-4"    % "3.2.8.0" % Test
  )

  val testPlay26: Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2"         % Test,
    "uk.gov.hmrc"            %% "reactivemongo-test" % "5.0.0-play-26" % Test
  )

  val testPlay27: Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.0"         % Test,
    "uk.gov.hmrc"            %% "reactivemongo-test" % "5.0.0-play-27" % Test
  )

  val testPlay28: Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"         % Test,
    "uk.gov.hmrc"            %% "reactivemongo-test" % "5.0.0-play-28" % Test,
    "com.vladsch.flexmark"         % "flexmark-all"              % "0.36.8"            % Test
  )
}

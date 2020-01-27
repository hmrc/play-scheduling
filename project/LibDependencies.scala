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

object LibDependencies {

  def apply(): Seq[ModuleID] = compile ++ test

  private val play25Version = "2.5.19"
  private val play26Version = "2.6.20"

  private val compile: Seq[ModuleID] = PlayCrossCompilation.dependencies(
    shared = Seq(
      "com.typesafe.akka"   %% "akka-actor"   % "2.4.20",
      "joda-time"           % "joda-time"     % "2.10.1",
      "org.apache.commons"  % "commons-lang3" % "3.4"
    ),
    play25 = Seq(
      "uk.gov.hmrc"         %% "mongo-lock"   % "6.18.0-play-25",
      "com.typesafe.play"   %% "play"         % play25Version     % Provided
    ),
    play26 = Seq(
      "uk.gov.hmrc"         %% "mongo-lock"   % "6.18.0-play-26",
      "com.typesafe.play"   %% "play"         % play26Version     % Provided
    )
  )

  private val test: Seq[ModuleID] = PlayCrossCompilation.dependencies(
    shared = Seq(
      "org.scalatest"           %% "scalatest"          % "3.0.7"   % Test,
      "org.pegdown"             % "pegdown"             % "1.6.0"   % Test,
      "ch.qos.logback"          % "logback-classic"     % "1.2.3"   % Test,

      "org.mockito"             %  "mockito-all"        % "1.10.19" % Test
    ),
    play25 = Seq(
      "org.scalatestplus.play"  %% "scalatestplus-play" % "2.0.1"   % Test,
      "uk.gov.hmrc" %%  "reactivemongo-test"  % "4.16.0-play-25"  % Test
    ),
    play26 = Seq(
      "org.scalatestplus.play"  %% "scalatestplus-play" % "3.1.2"   % Test,
      "uk.gov.hmrc" %%  "reactivemongo-test"  % "4.16.0-play-26"  % Test
    )
  )

}

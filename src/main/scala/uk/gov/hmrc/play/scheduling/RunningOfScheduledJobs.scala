/*
 * Copyright 2015 HM Revenue & Customs
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

package uk.gov.hmrc.play.scheduling

import akka.actor.{Cancellable, Scheduler}
import org.apache.commons.lang3.time.StopWatch
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings, Logger}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait RunningOfScheduledJobs extends GlobalSettings {
  def scheduler(app: Application): Scheduler = Akka.system(app).scheduler
  val scheduledJobs: Seq[ScheduledJob]

  private[scheduling] var cancellables : Seq[Cancellable] = Seq.empty

  override def onStart(app: Application) {
    super.onStart(app)

    implicit val hc = HeaderCarrier()

    Logger.info(s"Scheduling jobs: $scheduledJobs")
    cancellables = scheduledJobs.map { job =>
      scheduler(app).schedule(job.initialDelay, job.interval) {
        val stopWatch = new StopWatch
        stopWatch.start()
        Logger.info(s"Executing job ${job.name}")

        job.execute(hc).onComplete {
          case Success(job.Result(message)) =>
            stopWatch.stop()
            Logger.info(s"Completed job ${job.name} in $stopWatch: $message")
          case Failure(throwable) =>
            stopWatch.stop()
            Logger.error(s"Exception running job ${job.name} after $stopWatch", throwable)
        }
      }
    }
  }

  override def onStop(app: Application) {
    Logger.info(s"Cancelling all scheduled jobs.")
    cancellables.foreach(_.cancel())
    scheduledJobs.foreach { job =>
      Logger.info(s"Checking if job ${job.configKey} is running")
      while(Await.result(job.isRunning, 5.seconds)) {
        Logger.warn(s"Waiting for job ${job.configKey} to finish")
        Thread.sleep(1000)
      }
      Logger.warn(s"Job ${job.configKey} is finished")
    }
  }
}

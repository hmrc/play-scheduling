/*
 * Copyright 2017 HM Revenue & Customs
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
import com.google.inject.Inject
import org.apache.commons.lang3.time.StopWatch
import play.api.{Application, Logger}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by william on 13/02/17.
  */
trait JobExecutor {

  def scheduler: Scheduler

  def scheduleJobs(jobs: Seq[ScheduledJob]): Seq[Cancellable]

  def stopJobs(jobs: Seq[ScheduledJob], cancellables: Seq[Cancellable]): Boolean

}

class DefaultJobExecutor @Inject()(app: Application, lifecycle: ApplicationLifecycle) extends JobExecutor {

  def scheduler = app.actorSystem.scheduler

  def scheduleJobs(jobs: Seq[ScheduledJob]): Seq[Cancellable] = {
    implicit val ec = play.api.libs.concurrent.Execution.defaultContext

    Logger.info(s"Scheduling jobs: $jobs")
    val cancellables = jobs.map { job =>
      scheduler.schedule(job.initialDelay, job.interval) {
        val stopWatch = new StopWatch
        stopWatch.start()
        Logger.info(s"Executing job ${job.name}")

        job.execute.onComplete {
          case Success(job.Result(message)) =>
            stopWatch.stop()
            Logger.info(s"Completed job ${job.name} in $stopWatch: $message")
          case Failure(throwable) =>
            stopWatch.stop()
            Logger.error(s"Exception running job ${job.name} after $stopWatch", throwable)
        }
      }
    }

    lifecycle.addStopHook {
      () => Future.successful(stopJobs(jobs, cancellables))
    }
    cancellables
  }

  def stopJobs(jobs: Seq[ScheduledJob], cancellables: Seq[Cancellable]): Boolean = {
    Logger.info(s"Cancelling all scheduled jobs.")
    cancellables.foreach(_.cancel())
    jobs.foreach { job =>
      Logger.info(s"Checking if job ${job.configKey} is running")
      while(Await.result(job.isRunning, 5.seconds)) {
        Logger.warn(s"Waiting for job ${job.configKey} to finish")
        Thread.sleep(1000)
      }
      Logger.warn(s"Job ${job.configKey} is finished")
    }
    cancellables.forall(_.isCancelled)
  }

}

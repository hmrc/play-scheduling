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
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import play.api.inject.ApplicationLifecycle
import play.api.test.FakeApplication
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import uk.gov.hmrc.play.test.{DelayProcessing, UnitSpec}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by william on 14/02/17.
  */
class JobExecutorSpec extends UnitSpec
  with BeforeAndAfterAll with Eventually with DelayProcessing {

  private val fakeApplication = FakeApplication()

  override protected def afterAll(): Unit = {
    fakeApplication.stop()
  }

  private val testExecutor = new DefaultJobExecutor(
    fakeApplication,
    fakeApplication.injector.instanceOf(classOf[ApplicationLifecycle])
  )

  "When starting the app, the scheduled job runner" should {

    "do nothing if there are no scheduled jobs configured" in {
      testExecutor.scheduleJobs(Seq.empty) shouldBe empty
    }

    "schedule a configured job with the given interval and initialDuration" in {
      val job = new ScheduledJob {
        override def execute(implicit ec: ExecutionContext) = Future.successful(Result("done"))

        override def name = "test"

        override def interval = 5.seconds

        override def initialDelay = 5.seconds

        override def isRunning = Future.successful(false)
      }

      testExecutor.scheduleJobs(Seq(job)) shouldNot be(empty)
    }

    "set up the scheduled job to run the execute method" in {
      var capturedRunnable: Runnable = new Runnable {
        override def run() = ???
      }
      val job = new ScheduledJob {
        var executed = false
        override def execute(implicit ec: ExecutionContext) = {
          executed = true
          Future.successful(Result("done"))
        }

        override def name = "test"

        override def interval = 5.seconds

        override def initialDelay = 5.seconds

        override def isRunning = Future.successful(false)
      }

      val executor = new DefaultJobExecutor(
        fakeApplication,
        fakeApplication.injector.instanceOf(classOf[ApplicationLifecycle])
      ) {
        override def scheduler: Scheduler = new StubbedScheduler {
          override def schedule(initialDelay: FiniteDuration, interval: FiniteDuration, runnable: Runnable)(implicit executor: ExecutionContext) = {
            capturedRunnable = runnable
            null
          }
        }
      }

      executor.scheduleJobs(Seq(job))

      job should not be 'executed
      capturedRunnable.run()
      job should be ('executed)

    }

  }

  "When stopping the app, the scheduled job runner" should {

    "cancel all of the scheduled jobs" in {
      val job = new ScheduledJob {
        override def execute(implicit ec: ExecutionContext) = Future.successful(Result("done"))

        override def name = "test"

        override def interval = 5.seconds

        override def initialDelay = 5.seconds

        override def isRunning = Future.successful(false)
      }
      val jobs = Seq(job)
      val cancellables = testExecutor.scheduleJobs(jobs)

      testExecutor.stopJobs(jobs, cancellables) shouldBe true
    }

    "block while a scheduled jobs are still running" in {
      val job = new ScheduledJob {
        override def execute(implicit ec: ExecutionContext) = Future.successful(Result("done"))

        override def name = "test"

        override def interval = 2.seconds

        override def initialDelay = 3.seconds

        var isRunning = Future.successful(false)
      }
      job.isRunning = Future.successful(true)

      val jobs = Seq(job)

      val stopFuture = Future {
        testExecutor.stopJobs(jobs, testExecutor.scheduleJobs(jobs))
      }

      fixedDelay(5000)


      stopFuture should not be 'completed
      job.isRunning = Future.successful(false)
      eventually { stopFuture should be ('completed) }
    }

  }

  class StubbedScheduler extends Scheduler {
    def schedule(initialDelay: FiniteDuration, interval: FiniteDuration, runnable: Runnable)(implicit executor: ExecutionContext): Cancellable = ???
    def maxFrequency: Double = ???
    def scheduleOnce(delay: FiniteDuration, runnable: Runnable)(implicit executor: ExecutionContext): Cancellable = ???
  }

}

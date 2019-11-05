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

package uk.gov.hmrc.play.scheduling

import akka.actor.{Cancellable, Scheduler}
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.time.{Minute, Span}
import play.api.Application
import play.api.test.FakeApplication

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class RunningOfScheduledJobsSpec extends WordSpec with Matchers with Eventually {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = 5.seconds)

  "When starting the app, the scheduled job runner" should {

    "do nothing if there are no scheduled jobs configured" in new TestCase {
      val runner = new RunningOfScheduledJobs {
        override def scheduler(app: Application) = new StubbedScheduler
        val scheduledJobs                        = Seq.empty
      }

      runner.onStart(FakeApplication())
    }

    "schedule a configured job with the given interval and initialDuration" in new TestCase {
      object captured {
        var initialDelay: FiniteDuration = _
        var interval: FiniteDuration     = _
      }
      val runner = new RunningOfScheduledJobs {
        override def scheduler(app: Application): Scheduler = new StubbedScheduler {
          override def schedule(initialDelay: FiniteDuration, interval: FiniteDuration, runnable: Runnable)(
            implicit executor: ExecutionContext) = {
            captured.initialDelay = initialDelay
            captured.interval     = interval
            null
          }
        }

        val scheduledJobs = Seq(testScheduledJob)
      }

      runner.onStart(FakeApplication())

      captured should have('initialDelay (testScheduledJob.initialDelay))
      captured should have('interval (testScheduledJob.interval))
    }

    "set up the scheduled job to run the execute method" in new TestCase {
      var capturedRunnable: Runnable = _
      override val testScheduledJob = new TestScheduledJob {
        var executed = false
        override def execute(implicit ec: ExecutionContext) = {
          executed = true
          Future.successful(this.Result("done"))
        }
      }
      val runner = new RunningOfScheduledJobs {
        override def scheduler(app: Application): Scheduler = new StubbedScheduler {
          override def schedule(initialDelay: FiniteDuration, interval: FiniteDuration, runnable: Runnable)(
            implicit executor: ExecutionContext) = {
            capturedRunnable = runnable
            null
          }
        }

        val scheduledJobs = Seq(testScheduledJob)
      }

      runner.onStart(FakeApplication())

      testScheduledJob should not be 'executed
      capturedRunnable.run()
      testScheduledJob should be('executed)
    }
  }

  "When stopping the app, the scheduled job runner" should {
    "cancel all of the scheduled jobs" in new TestCase {
      val runner = new RunningOfScheduledJobs { val scheduledJobs = Seq.empty }
      runner.cancellables = Seq(new StubCancellable, new StubCancellable)

      every(runner.cancellables) should not be 'cancelled
      runner.onStop(FakeApplication())
      every(runner.cancellables) should be('cancelled)
    }
    "block while a scheduled jobs are still running" in new TestCase {
      val runner = new RunningOfScheduledJobs { val scheduledJobs = Seq(testScheduledJob) }

      testScheduledJob.isRunning = Future.successful(true)

      val stopFuture = Future {
        runner.onStop(FakeApplication())
      }

      val deadline: Deadline = 5000.milliseconds.fromNow
      while (deadline.hasTimeLeft()) {
        /* Intentionally burning CPU cycles for fixed period */
      }

      stopFuture should not be 'completed
      testScheduledJob.isRunning = Future.successful(false)
      eventually (timeout(Span(1, Minute))) { stopFuture should be('completed) }
    }
  }

  trait TestCase {
    class StubbedScheduler extends Scheduler {
      def schedule(initialDelay: FiniteDuration, interval: FiniteDuration, runnable: Runnable)(
        implicit executor: ExecutionContext): Cancellable = ???
      def maxFrequency: Double                            = ???
      def scheduleOnce(delay: FiniteDuration, runnable: Runnable)(implicit executor: ExecutionContext): Cancellable =
        ???
    }
    class TestScheduledJob extends ScheduledJob {
      override lazy val initialDelay: FiniteDuration = 2.seconds
      override lazy val interval: FiniteDuration     = 3.seconds
      def name                                       = "TestScheduledJob"

      def execute(implicit ec: ExecutionContext) = Future.successful(Result("done"))
      var isRunning: Future[Boolean]             = Future.successful(false)
    }
    val testScheduledJob = new TestScheduledJob
    class StubCancellable extends Cancellable {
      var isCancelled = false
      def cancel(): Boolean = {
        isCancelled = true
        isCancelled
      }
    }
  }

}
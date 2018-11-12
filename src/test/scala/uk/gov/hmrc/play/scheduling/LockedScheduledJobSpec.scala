/*
 * Copyright 2018 HM Revenue & Customs
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

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerTest 
import uk.gov.hmrc.lock.LockRepository
import uk.gov.hmrc.play.test.UnitSpec

import play.api.libs.json.Json
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.api.collections.bson.BSONCollection
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Await}
import scala.util.Try
import org.joda.time.Duration

class LockedScheduledJobSpec extends UnitSpec with ScalaFutures with GuiceOneAppPerTest with BeforeAndAfterEach {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> "mongodb://localhost:27017/test-play-schedule")
    .build()

  class SimpleJob(val name: String) extends LockedScheduledJob {

    protected lazy val mongoConnection = new MongoDbConnection {}
    protected implicit lazy val db = mongoConnection.db
    override val releaseLockAfter = new Duration(1000)

    val start = new CountDownLatch(1)

    val lockRepository = new  LockRepository()

    def continueExecution() = start.countDown()

    val executionCount = new AtomicInteger(0)

    def executions: Int = executionCount.get()

    override def executeInLock(implicit ec: ExecutionContext): Future[Result] = {
      Future {
        start.await()
        Result(executionCount.incrementAndGet().toString)
      }
    }

    override def initialDelay = FiniteDuration(1, TimeUnit.SECONDS)

    override def interval =  FiniteDuration(1, TimeUnit.SECONDS)
  }

  "ExclusiveScheduledJob" should {

    "let job run in sequence" in {
      val job = new SimpleJob("job1")
      job.continueExecution()
      Await.result(job.execute, 1.second).message shouldBe "Job with job1 run and completed with result 1"
      Await.result(job.execute, 1.second).message shouldBe "Job with job1 run and completed with result 2"
    }

    "not allow job to run in parallel" in {
      val job = new SimpleJob("job2")

      val pausedExecution = job.execute
      pausedExecution.isCompleted shouldBe false
      job.isRunning.futureValue shouldBe true
      job.execute.futureValue.message shouldBe "Job with job2 cannot aquire mongo lock, not running"
      job.isRunning.futureValue shouldBe true

      job.continueExecution()
      pausedExecution.futureValue.message shouldBe "Job with job2 run and completed with result 1"
      job.isRunning.futureValue shouldBe false
    }

    "should tolerate exceptions in execution" in {
      val job = new SimpleJob("job3") {
        override def executeInLock(implicit ec: ExecutionContext): Future[Result] = throw new RuntimeException
      }

      Try(job.execute.futureValue)

      job.isRunning.futureValue shouldBe false
    }
  }
}

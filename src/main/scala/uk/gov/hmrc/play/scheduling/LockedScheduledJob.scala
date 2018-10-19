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

import org.joda.time.Duration
import play.modules.reactivemongo.MongoDbConnection
import uk.gov.hmrc.lock.LockRepository
import uk.gov.hmrc.lock.LockKeeper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait LockedScheduledJob extends ScheduledJob with MongoDbConnection {

  lazy val lockRepository = new LockRepository

  lazy val exclusiveTimePeriodLock: LockKeeper = new LockKeeper {
    val repo = lockRepository
    val lockId = s"$name-scheduled-job-lock"
    val forceLockReleaseAfter: Duration = new Duration(interval.toMillis)
  }

  def executeInLock(implicit ec: ExecutionContext): Future[this.Result]

  final def execute(implicit ec: ExecutionContext): Future[Result] =
    exclusiveTimePeriodLock.tryLock { 
      executeInLock
    } map { 
      case Some(res) => Result(s"Job with $name run and completed with result $res")
      case None => Result(s"Job with $name not cannot aquire mongo lock, not running")
    }

}

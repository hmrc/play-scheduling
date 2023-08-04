/*
 * Copyright 2023 HM Revenue & Customs
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

import uk.gov.hmrc.mongo.lock.{LockRepository, LockService}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future, duration}

trait LockedScheduledJob extends ScheduledJob {

  def executeInLock(implicit ec: ExecutionContext): Future[this.Result]

  val releaseLockAfter: Duration

  val lockRepo: LockRepository

  lazy val lockKeeper: LockService = new LockService {
    override val lockRepository: LockRepository = lockRepo
    override val lockId: String = s"$name-scheduled-job-lock"
    override val ttl: duration.Duration = releaseLockAfter
  }
  
  final def execute(implicit ec: ExecutionContext): Future[Result] =
    lockKeeper.withLock {
      executeInLock
    } map {
      case Some(Result(msg)) => Result(s"Job with $name run and completed with result $msg")
      case None              => Result(s"Job with $name cannot aquire mongo lock, not running")
    }

}

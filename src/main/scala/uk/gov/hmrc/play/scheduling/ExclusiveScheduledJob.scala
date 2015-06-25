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

import java.util.concurrent.Semaphore

import uk.gov.hmrc.play.audit.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait ExclusiveScheduledJob extends ScheduledJob {

  def executeInMutex(implicit hc: HeaderCarrier): Future[this.Result]

  final def execute(implicit hc: HeaderCarrier):Future[Result] =
    if (mutex.tryAcquire()) {
      Try(executeInMutex) match {
        case Success(f) => f andThen { case _ => mutex.release() }
        case Failure(e) => Future.successful(mutex.release()).flatMap(_ => Future.failed(e))
      }
    }
    else Future.successful(Result("Skipping execution: job running"))

  def isRunning: Future[Boolean] = Future.successful(mutex.availablePermits() == 0)

  final private val mutex = new Semaphore(1)
}

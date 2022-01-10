/*
 * Copyright 2022 HM Revenue & Customs
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

package utils.stubs

import play.api.test.Helpers.{FORBIDDEN, INTERNAL_SERVER_ERROR, OK}
import utils.IntegrationBaseSpec

object IhtStub extends IntegrationBaseSpec {

  def responseForSubmitApplication(status: Int, body: String): Unit = {
    stubPost("/iht/AA123456A/ABC1234567890/application/submit", status, body)
  }

  def stubSuccessfulResponse(): Unit = responseForSubmitApplication(OK, "Success response received: 12345678")

  def stubInternalServerError(): Unit = responseForSubmitApplication(INTERNAL_SERVER_ERROR, "No registration details found")

  def stubForbiddenResponse(): Unit = responseForSubmitApplication(FORBIDDEN, "Submitter is not the lead executor")

}

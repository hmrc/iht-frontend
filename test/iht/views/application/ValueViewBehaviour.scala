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

package iht.views.application

trait ValueViewBehaviour[A] extends SubmittableApplicationPageBehaviour[A] {

  val value_id = "value"

  /**
    * Assumes that the Call for the continue button has been set up as CommonBuilder.DefaultCall1.
    */
  def valueView() = {
    applicationPageWithErrorSummaryBox()

    s"have an input field with an ID of $value_id" in {
      Option(doc.getElementById(value_id)).isDefined shouldBe true
    }
  }
}

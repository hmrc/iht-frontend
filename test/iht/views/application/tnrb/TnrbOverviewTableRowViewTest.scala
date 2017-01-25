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

package iht.views.application.tnrb

import iht.forms.TnrbForms._
import iht.testhelpers.{CommonBuilder, TestHelper}
import iht.views.ViewTestHelper
import iht.views.html.application.tnrb.tnrb_overview_table_row

class TnrbOverviewTableRowViewTest extends ViewTestHelper {

  val ihtReference = Some("ABC1A1A1A")
  val deceasedDetails = CommonBuilder.buildDeceasedDetails
  val regDetails = CommonBuilder.buildRegistrationDetails.copy(ihtReference = ihtReference,
                        deceasedDetails = Some(deceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusMarried))),
                        deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath))

  val tnrbModel = CommonBuilder.buildTnrbEligibility
  val widowCheck = CommonBuilder.buildWidowedCheck

  lazy val id = "home-in-uk"
  lazy val questionText = "Sample question"
  lazy val questionScreenReaderText = "Sample screen reader"
  lazy val questionCategory = "Sample category"


  def tnrbOverviewTableRow(id: String = "home-in-uk",
                           questionText:String = "Sample question",
                           questionScreenReaderText: String = "Sample screen reader",
                           questionCategory:String = "questionAnswer",
                           answerValue:String = "",
                           link:Option[Call] = None,
                           linkScreenReader:String = "") =  {

    implicit val request = createFakeRequest()
    val view = tnrb_overview_table_row(id,
      questionText,
      questionScreenReaderText,
      questionCategory:String,
      answerValue:String = "",
    link:Option[Call] = None,
    linkScreenReader:String).toString

    val doc = asDocument(view)
  }

  "TnrbOverviewTableRow" must {

    "have the correct id" in {

    }

    "have the correct question text" in {

    }

    "show the value if it has" in {

    }

    "show the correct link with text" in {

    }
  }

}

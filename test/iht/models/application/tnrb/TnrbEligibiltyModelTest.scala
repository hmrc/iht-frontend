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

package iht.models.application.tnrb

import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 03/11/16.
  */

class TnrbEligibiltyModelTest extends UnitSpec with MockitoSugar{

  "areAllQuestionsAnswered" must {

    "returns true if all the tnrb questions are answered" in {
      val tnrb = CommonBuilder.buildTnrbEligibility.copy(
        dateOfPreDeceased = Some(CommonBuilder.DefaultDateOfPreDeceased))

      tnrb.areAllQuestionsAnswered shouldBe true
    }

    "returns false if all the tnrb questions are not answered" in {
      val tnrb = CommonBuilder.buildTnrbEligibility.copy(isPartnerLivingInUk = None)

      tnrb.areAllQuestionsAnswered shouldBe false
    }
  }

}

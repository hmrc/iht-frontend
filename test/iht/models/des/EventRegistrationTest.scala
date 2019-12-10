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

package models.des

import iht.FakeIhtApp
import iht.config.AppConfig
import iht.testhelpers.CommonBuilder
import org.scalatestplus.mockito.MockitoSugar

class EventRegistrationTest extends FakeIhtApp with MockitoSugar {
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "EventRegistration" must {
    "convert all fields from registration details to event registration where there are co-executors" in {
      val rd1 = CommonBuilder.buildRegistrationDetails1
      val er1 = CommonBuilder.buildEventRegistration1
      val er2 = EventRegistration.fromRegistrationDetails(rd1)
      er2 mustBe (er1.copy(acknowledgmentReference = er2.acknowledgmentReference))
    }

    "convert all fields from registration details to event registration where there are NO co-executors" in {
      val rd1 = CommonBuilder.buildRegistrationDetails2
      val er1 = CommonBuilder.buildEventRegistration2
      val er2 = EventRegistration.fromRegistrationDetails(rd1)
      er2 mustBe (er1.copy(acknowledgmentReference = er2.acknowledgmentReference))
    }

    "convert all fields from registration details to event registration where the nino has spaces" in {
      val rd1 = CommonBuilder.buildRegistrationDetails3
      val er1 = CommonBuilder.buildEventRegistration3
      val er2 = EventRegistration.fromRegistrationDetails(rd1)
      er2 mustBe (er1.copy(acknowledgmentReference = er2.acknowledgmentReference))
    }

    "convert all fields from registration details to event registration where the marital status is " +
      "Married or in Civil Partnership" in {
      val rd1 = CommonBuilder.buildRegistrationDetails4
      val er1 = CommonBuilder.buildEventRegistration4
      val er2 = EventRegistration.fromRegistrationDetails(rd1)
      er2 mustBe (er1.copy(acknowledgmentReference = er2.acknowledgmentReference))
    }

    "convert all fields from registration details to event registration where the marital status is " +
      "Widowed or a surviving civil partner" in {
      val rd1 = CommonBuilder.buildRegistrationDetails5
      val er1 = CommonBuilder.buildEventRegistration5
      val er2 = EventRegistration.fromRegistrationDetails(rd1)
      er2 mustBe (er1.copy(acknowledgmentReference = er2.acknowledgmentReference))
    }

    "convert all fields from registration details to event registration where the marital status is Single" in {
      val rd1 = CommonBuilder.buildRegistrationDetails6
      val er1 = CommonBuilder.buildEventRegistration6
      val er2 = EventRegistration.fromRegistrationDetails(rd1)
      er2 mustBe (er1.copy(acknowledgmentReference = er2.acknowledgmentReference))
    }

    "convert all fields from registration details to event registration where the marital status is " +
      "Divorced or former Civil Partner" in {
      val rd1 = CommonBuilder.buildRegistrationDetails7
      val er1 = CommonBuilder.buildEventRegistration7
      val er2 = EventRegistration.fromRegistrationDetails(rd1)
      er2 mustBe (er1.copy(acknowledgmentReference = er2.acknowledgmentReference))
    }

  }
}

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

package iht.testhelpers

import uk.gov.hmrc.play.test.UnitSpec

/**
 *
 * Created by Vineet Tyagi on 26/05/15.
 *
 */
class CommonBuilderTest extends UnitSpec {

  "buildDeceasedDateOfDeath" must {
    "create DeceasedDateOfDeath model with default value" in {
      val deceasedDateOfDeath=CommonBuilder.buildDeceasedDateOfDeath
      assert(deceasedDateOfDeath.dateOfDeath.equals(CommonBuilder.DefaultDeceasedDOD),
        "Default date is " + CommonBuilder.DefaultDeceasedDOD)
    }
  }

  "buildApplicantDetails" must {
    "create ApplicantDetails model with default value" in {
      val applicantDetails=CommonBuilder.buildApplicantDetails
      assert(applicantDetails.firstName.equals(Some(CommonBuilder.DefaultFirstName)),
        "Default name is " + CommonBuilder.DefaultFirstName)
      assert(applicantDetails.lastName.equals(Some(CommonBuilder.DefaultLastName)),
        "Default name is " + CommonBuilder.DefaultLastName)
      assert(applicantDetails.nino.equals(Some(CommonBuilder.DefaultNino)),
        "Default nino is " + CommonBuilder.DefaultNino)
      assert(applicantDetails.role.equals(Some(CommonBuilder.DefaultRole)),
        "Default role is " + CommonBuilder.DefaultRole)
      assert(applicantDetails.country.equals(Some(CommonBuilder.DefaultCountry)),
        "Default Country is " + CommonBuilder.DefaultCountry)
    }
  }

  "buildApplicantDetails" must {
    "create ApplicantDetails model with given field values" in {
      val nino = CommonBuilder.DefaultNino
      val applicantDetails=CommonBuilder.buildApplicantDetails copy (firstName = Some("FirstName"),
        lastName = Some("LastName"),
        nino = Some(nino))
      assert(applicantDetails.firstName.equals(Some("FirstName")),"Changed name is FirstName")
      assert(applicantDetails.lastName.equals(Some("LastName")),"Changed last name is LastName")
      assert(applicantDetails.nino.equals(Some(nino)),"Changed nino is the same nino")
    }
  }

  "buildDeceasedDetails" must {
    "create DeceasedDetails model with default value" in {
      val deceasedDetails=CommonBuilder.buildDeceasedDetails
      assert(deceasedDetails.firstName.equals(Some(CommonBuilder.DefaultFirstName)),
        "Default name is " + CommonBuilder.DefaultFirstName)
      assert(deceasedDetails.lastName.equals(Some(CommonBuilder.DefaultLastName)),
        "Default name is " + CommonBuilder.DefaultLastName)
      assert(deceasedDetails.nino.equals(Some(CommonBuilder.DefaultNino)),
        "Default nino is " + CommonBuilder.DefaultNino)
      assert(deceasedDetails.domicile.equals(Some(CommonBuilder.DefaultDomicile)),
        "Default Domicile is " + CommonBuilder.DefaultDomicile)
      assert(deceasedDetails.maritalStatus.equals(Some(CommonBuilder.DefaultMaritalStatus)),
        "Default Marital status is " + CommonBuilder.DefaultMaritalStatus)
    }
  }

  "buildCoExecutor" must {
    "create CoExecutor model with default value" in {
      val coExecutor=CommonBuilder.buildCoExecutor
      assert(coExecutor.firstName.equals(CommonBuilder.DefaultFirstName),
        "Default name is " + CommonBuilder.DefaultFirstName)
      assert(coExecutor.lastName.equals(CommonBuilder.DefaultLastName),
        "Default name is " + CommonBuilder.DefaultLastName)
      assert(coExecutor.nino.equals(CommonBuilder.DefaultNino),
        "Default nino is " + CommonBuilder.DefaultNino)
      assert(coExecutor.role.get.equals(CommonBuilder.DefaultCoExecutorRole),
        "Default role is " + CommonBuilder.DefaultCoExecutorRole)

    }
  }

  "buildRegistrationDetails" must {
    "create RegistrationDetails model with default value" in {
      val registrationDetails = CommonBuilder.buildRegistrationDetails
      assert(registrationDetails.applicantDetails == None, "Default Applicant Details is None")
      assert(registrationDetails.deceasedDetails == None, "Default DeceasedDetais is None")
      assert(registrationDetails.coExecutors.size == 0, "Default CoExecutors size is Zero")
    }
  }
}

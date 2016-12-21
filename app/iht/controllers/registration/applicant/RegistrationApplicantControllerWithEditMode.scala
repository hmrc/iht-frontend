/*
 * Copyright 2016 HM Revenue & Customs
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

package iht.controllers.registration.applicant

import iht.controllers.registration.RegistrationBaseControllerWithEditMode
import iht.models.{ApplicantDetails, RegistrationDetails}
import play.api.data.Form


trait RegistrationApplicantControllerWithEditMode extends RegistrationBaseControllerWithEditMode[ApplicantDetails] {
  def fillForm(rd: RegistrationDetails): Form[ApplicantDetails] =
    rd.applicantDetails.fold(form)(ad => form.fill(ad))
}

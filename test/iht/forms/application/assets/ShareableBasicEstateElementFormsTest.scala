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

package iht.forms.application.assets

import iht.forms.ApplicationForms._

/**
  * Created by vineet on 15/12/16.
  */
class ShareableBasicEstateElementFormsTest extends ShareableBasicEstateElementFormBehaviour {

  "MoneyDeceasedOwned form " must {
    behave like deceasedOwnedForm(moneyFormOwn, "error.assets.money.deceasedOwned.select")
  }

  "MoneyJointlyOwned form" must {
    behave like jointlyOwnedForm(moneyJointlyOwnedForm, "error.assets.money.jointlyOwned.select")
  }

  "HouseholdDeceasedOwned form" must {
    behave like deceasedOwnedForm(householdFormOwn, "error.assets.household.deceasedOwned.select")
  }

  "HouseholdJointlyOwned form" must {
    behave like jointlyOwnedForm(householdJointlyOwnedForm, "error.assets.household.jointlyOwned.select")
  }

  "MotorVehiclesDeceasedOwned form" must {
    behave like deceasedOwnedForm(vehiclesFormOwn, "error.assets.vehicles.deceasedOwned.select")
  }

  "MotorVehiclesJointlyOwned form" must {
    behave like jointlyOwnedForm(vehiclesJointlyOwnedForm, "error.assets.vehicles.jointlyOwned.select")
  }

}

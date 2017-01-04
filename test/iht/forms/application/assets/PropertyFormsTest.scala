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

import iht.constants.FieldMappings
import iht.forms.ApplicationForms._
import iht.forms.FormTestHelper
import iht.models.application.assets.{Properties, Property}

class PropertyFormsTest extends FormTestHelper {

 "PropertiesForm" must {
   behave like yesNoQuestion[Properties]("isOwned",
     propertiesForm,
     _.isOwned,
     "error.assets.property.owned.select"
   )
 }

  "PropertyTenureForm" must {
    behave like multipleChoiceQuestion[Property]("tenure",
                                            propertyTenureForm,
                                            _.tenure,
                                           "error.assets.property.tenure.select",
                                            FieldMappings.tenures)
  }

  "PropertyTypeForm" must {
    behave like multipleChoiceQuestion[Property]("propertyType",
                                            propertyTypeForm,
                                            _.propertyType,
                                            "error.assets.property.type.select",
                                            FieldMappings.propertyType)
  }

  "TypeOfOwnershipForm" must {
    behave like multipleChoiceQuestion[Property]("typeOfOwnership",
                                          typeOfOwnershipForm,
                                        _.typeOfOwnership,
                                        "error.assets.property.ownership.select",
                                        FieldMappings.typesOfOwnership)
   }

  "PropertyValueForm" must {
    behave like mandatoryCurrencyValue[Property]("value", propertyValueForm)
  }

  "PropertyAddressForm" must {
    behave like ukAddress[Property]("address", propertyAddressForm, _.address)
  }

}

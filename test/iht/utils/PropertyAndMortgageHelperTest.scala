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

package iht.utils

import iht.FakeIhtApp
import iht.constants.IhtProperties.{AppSectionPropertiesID, AssetsPropertiesOwnedID}
import iht.models.application.assets.Properties
import iht.models.application.debts.{Mortgage, MortgageEstateElement}
import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import play.api.mvc.Results
import uk.gov.hmrc.play.test.UnitSpec

class PropertyAndMortgageHelperTest extends FakeIhtApp with MockitoSugar {

  "PropertyAndMortgagesHelper" when {

    val helper = PropertyAndMortgageHelper

    val propertiesNo = Properties(Some(false))
    val propertiesYes = Properties(Some(true))
    val propertyList = CommonBuilder.buildPropertyList

    val mortgage1 = Mortgage(id = "1", value = Some(BigDecimal(5000)),isOwned = Some(true))
    val mortgage2 = Mortgage(id = "2", value = Some(BigDecimal(2000)), isOwned = Some(true))
    val mortgageList = List(mortgage1, mortgage2)

    val appDetailsWithoutProperties = CommonBuilder.buildApplicationDetails
    val appDetailsWithProperties = CommonBuilder.buildApplicationDetails.copy(
      allAssets = Some(CommonBuilder.buildAllAssetsWithAllSectionsFilled),
      allLiabilities = Some(CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled))

    "updatePropertyList is called" must {

      "return an empty list if there are no properties owned" in {
        val result = helper.updatePropertyList(propertiesNo, appDetailsWithoutProperties)
        result mustBe Nil
      }

      "return a list of existing properties if there are properties owned" in {
        val appDetails = appDetailsWithProperties.copy(propertyList = propertyList)
        val result = helper.updatePropertyList(propertiesYes, appDetails)
        result mustBe propertyList
      }

    }

    "isMortgagesLargerThanProperties is called" must {

      "return true if there are more mortgages than properties" in {
        val result = helper.isMortgagesLargerThanProperties(appDetailsWithoutProperties.copy(
          allLiabilities = Some(CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled)))
        result mustBe true
      }

      "return false if there are not more mortgages than properties" in {
        val result = helper.isMortgagesLargerThanProperties(appDetailsWithProperties.copy(propertyList = propertyList))
        result mustBe false
      }

    }

    "reduceMortgagesToMatchProperties is called" must {

      "return 0 mortgages when there are 0 properties" in {
        val result = helper.reduceMortgagesToMatchProperties(appDetailsWithoutProperties.copy(
          allLiabilities = Some(CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled)))
        result mustBe Nil
      }

      "return 1 mortgage when there is 1 property and 2 mortgages" in {
        val result = helper.reduceMortgagesToMatchProperties(appDetailsWithoutProperties.copy(
          propertyList = List(CommonBuilder.property),
          allLiabilities = Some(CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled)))
        result.size mustBe 1
      }

      "return 2 mortgages when there are 2 properties and 2 mortgages" in {
        val result = helper.reduceMortgagesToMatchProperties(appDetailsWithoutProperties.copy(
          propertyList = List(CommonBuilder.property, CommonBuilder.property2),
          allLiabilities = Some(CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled)))
        result.size mustBe 2
      }

      "return 2 mortgages when there are 3 properties and 2 mortgages" in {
        val result = helper.reduceMortgagesToMatchProperties(appDetailsWithoutProperties.copy(
          propertyList = List(CommonBuilder.property,
            CommonBuilder.property2,
            CommonBuilder.property2.copy(id = Some("3"))),
          allLiabilities = Some(CommonBuilder.buildAllLiabilitiesWithAllSectionsFilled)))
        result.size mustBe 2
      }

    }

    "updateMortgages is called" must {

      "return a None if there are no properties owned" in {
        val result = helper.updateMortgages(propertiesNo, appDetailsWithProperties)
        result mustBe None
      }

      "return the a MortgageEstateElement if there are properties owned" in {
        val result = helper.updateMortgages(propertiesYes, appDetailsWithProperties.copy(propertyList = propertyList))
        result mustBe Some(MortgageEstateElement(Some(true), mortgageList))
      }

    }

    "previousValueOfIsPropertyOwned is called" must {

      "return true if properties owned" in {
        val result = helper.previousValueOfIsPropertyOwned(appDetailsWithProperties)
        result mustBe Some(true)
      }

      "return false if properties not owned" in {
        val result = helper.previousValueOfIsPropertyOwned(appDetailsWithoutProperties.copy(
          allAssets = Some(CommonBuilder.buildAllAssetsAnsweredNo)))
        result mustBe Some(false)
      }

      "return None if no answer given to properties owned" in {
        val result = helper.previousValueOfIsPropertyOwned(appDetailsWithoutProperties)
        result mustBe None
      }

    }

    "doesPropertyListContainProperties is called" must {

      "return true if property details have been provided" in {
        val result = helper.doesPropertyListContainProperties(appDetailsWithProperties.copy(propertyList = propertyList))
        result mustBe true
      }

      "return false if no property details have been provided" in {
        val result = helper.doesPropertyListContainProperties(appDetailsWithoutProperties)
        result mustBe false
      }

    }

    "determineRedirectLocationForPropertiesOwnedQuestion is called" must {

      "return a redirect to Assets Overview when properties is owned answered no " in {
        val result = helper.determineRedirectLocationForPropertiesOwnedQuestion(propertiesNo, appDetailsWithoutProperties)
        result mustBe Results.Redirect(CommonHelper.addFragmentIdentifier(
          iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad(),
          Some(AppSectionPropertiesID)))
      }

      "return a redirect to Property Details Overview when properties owned is answered yes for the first time" in {
        val result = helper.determineRedirectLocationForPropertiesOwnedQuestion(propertiesYes, appDetailsWithoutProperties)
        result mustBe Results.Redirect(
          iht.controllers.application.assets.properties.routes.PropertyDetailsOverviewController.onPageLoad())
      }

      "return a redirect to Properties Overview when properties is owned answered yes " +
        "and was previously answered yes and there are no properties in the property list" in {
        val result = helper.determineRedirectLocationForPropertiesOwnedQuestion(propertiesYes,
          appDetailsWithoutProperties)
        result mustBe Results.Redirect(
          iht.controllers.application.assets.properties.routes.PropertyDetailsOverviewController.onPageLoad())
      }

      "return a redirect to Properties Overview when properties is owned answered yes " +
        "and was previously answered yes and there are properties in the property list" in {
        val result = helper.determineRedirectLocationForPropertiesOwnedQuestion(propertiesYes,
          appDetailsWithProperties.copy(propertyList = propertyList))
        result mustBe Results.Redirect(CommonHelper.addFragmentIdentifier(
          iht.controllers.application.assets.properties.routes.PropertiesOverviewController.onPageLoad(),
          Some(AssetsPropertiesOwnedID)))
      }

    }

  }

}

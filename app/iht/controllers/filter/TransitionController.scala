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

package iht.controllers.filter

import iht.controllers.auth.CustomPasscodeAuthentication
import play.api.i18n.Messages
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object TransitionController extends TransitionController

trait TransitionController extends FrontendController with CustomPasscodeAuthentication {


  def onPageLoadScotland = doPageLoad(Messages("iht.countries.scotland"))
  def onPageLoadNorthernIreland = doPageLoad(Messages("iht.countries.northernIreland"))
  def onPageLoadOtherCountry = doPageLoad(Messages("page.iht.filter.domicile.choice.other"))

  def doPageLoad(country: String) = customAuthenticatedActionAsync {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.use_paper_form(country)))
    }
  }
}

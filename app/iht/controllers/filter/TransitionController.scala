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

import iht.config.IhtFormPartialRetriever
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.controller.{UnauthorisedAction, FrontendController}
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

object TransitionController extends TransitionController

trait TransitionController extends FrontendController {


  def onPageLoadScotland = doPageLoad("iht.countries.scotland")
  def onPageLoadNorthernIreland = doPageLoad("iht.countries.northernIreland")
  def onPageLoadOtherCountry = doPageLoad("page.iht.filter.domicile.choice.other")

  implicit val formPartialRetriever: FormPartialRetriever = IhtFormPartialRetriever

  def doPageLoad(countryMessageKey: String) = UnauthorisedAction.async {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.use_paper_form(countryMessageKey)))
    }
  }
}

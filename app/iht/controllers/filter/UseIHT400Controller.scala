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

import javax.inject.{Inject, Singleton}

import iht.controllers.auth.CustomPasscodeAuthentication
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

/**
 * Created by adwelly on 21/10/2016.
 */

@Singleton
class UseIHT400Controller @Inject()(implicit val messagesApi: MessagesApi) extends FrontendController with CustomPasscodeAuthentication {

  def onPageLoad = customAuthenticatedActionAsync {
    implicit request => {
      Future.successful(Ok(iht.views.html.filter.use_iht400()(request, messagesApi.preferred(request))))
    }
  }
}

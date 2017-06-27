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

package iht.controllers.registration

import javax.inject.{Inject, Singleton}

import play.api.i18n.MessagesApi

import scala.concurrent.Future

@Singleton
class CompletedRegistrationController @Inject()(val messagesApi: MessagesApi) extends RegistrationController{
  override def guardConditions: Set[Predicate] = Set.empty

  def onPageLoad() = authorisedForIht {
    implicit user =>implicit request => {
      withRegistrationDetailsOrRedirect(request.uri) { rd =>
         Future.successful(Ok(iht.views.html.registration.completed_registration(rd.ihtReference.get)))
      }
    }
  }

  def onSubmit = authorisedForIht {
    implicit user =>implicit request => {
      Future(Redirect(iht.controllers.home.routes.IhtHomeController.onPageLoad()))
    }
  }
}

/*
 * Copyright 2018 HM Revenue & Customs
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

import iht.constants.Constants
import play.api.mvc.{Request, Session}
import uk.gov.hmrc.play.frontend.auth.AuthContext


object SessionHelper {

  def ensureSessionHasNino(session: Session, user: AuthContext): Session =
    CommonHelper.withValue(StringHelper.getNino(user)) { currentNino =>
      val optionSession = session.get(Constants.NINO).fold[Option[Session]](
        None
      ) { foundNino =>
        if (foundNino == currentNino) {
          Option(session)
        } else {
          None
        }
      }
      optionSession.fold(session + (Constants.NINO -> currentNino))(identity)
    }

  def getNinoFromSession(request:Request[_]): Option[String] = request.session.get(Constants.NINO)

}

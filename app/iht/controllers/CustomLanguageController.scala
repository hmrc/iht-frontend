/*
 * Copyright 2021 HM Revenue & Customs
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

package iht.controllers

import iht.config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import scala.util.Try

@Singleton
class CustomLanguageController @Inject()(val appConfig: AppConfig,
                                         val languageUtils: LanguageUtils,
                                         val cc: MessagesControllerComponents) extends FrontendController(cc) with I18nSupport {
  /** Converts a string to a URL, using the route to this controller. **/
  val englishLang = Lang("en")

  def langToCall(lang: String): Call = {
    if(appConfig.isWelshEnabled) {
      iht.controllers.routes.CustomLanguageController.switchToLanguage(lang)
    } else {
      iht.controllers.routes.CustomLanguageController.switchToLanguage("english")
    }
  }

  def switchToLanguage(language: String): Action[AnyContent] = Action { implicit request =>
    val lang =
      if(appConfig.isWelshEnabled) {
        languageMap.getOrElse(language, languageUtils.getCurrentLang)
      } else {
        englishLang
      }
    val redirectURL = request.headers.get(REFERER).getOrElse(fallbackURL)

    Redirect(redirectURL).withLang(Lang.apply(lang.code)).flashing(Flash(Map("switching-language" -> "true")))
  }

  /** Provides a fallback URL if there is no referer in the request header. **/
  protected def fallbackURL: String =
    Try(appConfig.servicesConfig.getString(s"language.fallbackUrl")).getOrElse("/")

  /** Returns a mapping between strings and the corresponding Lang object. **/
  def languageMap: Map[String, Lang] =
    Map(
      "english" -> Lang("en"),
      "cymraeg" -> Lang("cy")
    )
}

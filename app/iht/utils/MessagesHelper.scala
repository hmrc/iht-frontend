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

import play.api.Play.current
import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.i18n.{Lang, Messages}

/**
  *
  * This object contains all the common functionalities that can be reused
  */
object MessagesHelper {
  def translateToPreferredLanguage(content: String, sourceMessages: Messages, targetLanguageCode: String): String = {
    val sourceLang = sourceMessages.lang.code.substring(0, 2)
    if (sourceLang == targetLanguageCode) {
      content
    } else {
      sourceMessages.messages.messages(sourceLang).find(_._2 == content) match {
        case None =>
          content
        case Some(messageFound) =>
          sourceMessages.messages.messages(targetLanguageCode)(messageFound._1).replace("''", "'")
      }
    }
  }

  def messagesForLang(sourceMessages: Messages, targetLanguageCode: String): Messages = {
    implicit val lang = Lang.apply(targetLanguageCode)
    applicationMessages
  }

  def englishMessages(messageKey: String, messages: Messages): Option[String] = {
    messages.messages.messages("en").find(_._1 == messageKey) match {
      case None => None
      case Some(msg) => Some(msg._2.replace("''", "'"))
    }
  }
}

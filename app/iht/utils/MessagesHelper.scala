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
    val sourceLang = sourceMessages.lang.code.substring(0,2)
    if (sourceLang == targetLanguageCode) {
      content
    } else {
      val key = sourceMessages.messages.messages(sourceLang).find(_._2 == content).fold("Key not found")(_._1)
      sourceMessages.messages.messages(targetLanguageCode)(key).replace("''", "'")
    }
  }

  def messagesForLang(sourceMessages: Messages, targetLanguageCode: String): Messages = {
    implicit val lang = Lang.apply(targetLanguageCode)
    applicationMessages
  }
}

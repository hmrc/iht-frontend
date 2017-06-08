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

package iht.utils.pdf

import play.api.i18n.{Lang, Messages, MessagesApi}

/**
  * Created by grant on 02/12/16.
  */

object MessagesTranslator {
  def apply(msg: Messages):MessagesTranslator = {
    new MessagesTranslator(msg)
  }
}

class MessagesTranslator(msg: Messages) {
  def getMessagesText(key: String): String = msg(key)

  def getMessagesTextWithParameter(key: String, parameter:String ): String = msg(key, parameter)

  def getMessagesTextWithParameters(key: String, parameter1: String, parameter2:String): String =
    msg(key, parameter1, parameter2)

  def getMessagesTextWithParameters(key: String, parameter1: String, parameter2:String , parameter3: String): String =
    msg(key, parameter1, parameter2, parameter3)
}

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

import javax.inject.Inject
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.{Source, URIResolver}

import play.api.{Environment, Logger}

/**
  * Created by david-beer on 29/09/16.
  */
class StylesheetResolver @Inject() (env:Environment) extends URIResolver {
  override def resolve(href: String, base: String): Source = {
    Logger.info("Stylesheet location to convert " + href)
    val resource: String = href.substring(href.lastIndexOf("/pdf") + 1)
    env.resourceAsStream(resource) match {
      case None =>
        Logger.info ("No input stream")
        throw new RuntimeException("No stylesheet resolver stream available")
      case Some(inputStream) =>
    Logger.info ("Valid input stream")
    new StreamSource (inputStream)
    }
  }
}

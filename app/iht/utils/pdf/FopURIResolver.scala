/*
 * Copyright 2016 HM Revenue & Customs
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

import java.io.OutputStream
import java.net.URI

import org.apache.xmlgraphics.io.{Resource, ResourceResolver}
import play.api.{Logger, Play}
import play.api.Play.current

/**
  * Created by david-beer on 25/10/16.
  */
object FopURIResolver extends FopURIResolver

class FopURIResolver extends ResourceResolver {

  override def getOutputStream(uri: URI): OutputStream = ???

  override def getResource(uri: URI): Resource = {
    Logger.info("URI to convert to resource " + uri.toASCIIString)
    val resource: String = uri.getPath().substring(uri.getPath().lastIndexOf("/pdf") + 1)
    val inputStream = Play.classloader.getResourceAsStream(resource)
    if (inputStream != null) {
      Logger.info("Valid input stream")
      new Resource(inputStream)
    } else {
      Logger.info("No input stream")
      null
    }
  }
}

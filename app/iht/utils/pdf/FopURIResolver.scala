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

import java.io.OutputStream
import java.net.URI
import javax.inject.{Inject, Singleton}

import org.apache.xmlgraphics.io.{Resource, ResourceResolver}
import play.api.{Environment, Logger}

@Singleton
class FopURIResolver @Inject() (env:Environment) extends ResourceResolver {
  override def getOutputStream(uri: URI): OutputStream = ???

  override def getResource(uri: URI): Resource = {
    Logger.info("URI to convert to resource " + uri.toASCIIString)
    val resource: String = uri.getPath.substring(uri.getPath.lastIndexOf("/pdf") + 1)
    env.resourceAsStream(resource) match {
      case None =>
        Logger.info ("No input stream")
        throw new RuntimeException("No input stream available for FOP resource")
      case Some(inputStream) =>
      Logger.info ("Valid input stream")
      new Resource (inputStream)
    }
  }
}

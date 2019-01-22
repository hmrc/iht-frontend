/*
 * Copyright 2019 HM Revenue & Customs
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

import java.net.{URI, URLEncoder}

import iht.config.{AppConfig, ApplicationConfig}
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.play.config.AppName
import uk.gov.hmrc.play.frontend.auth._
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{ConfidenceLevel, CredentialStrength}

import scala.concurrent.Future

object IhtSection extends Enumeration {
  val Registration, Application = Value
}

trait AuthHelper extends AppName {
//  private def getCompositePageVisibilityPredicate(postSignInUrl: String, notAuthorisedUrl: String, requiredConfidenceLevel: Int) = {
//    new CompositePageVisibilityPredicate {
//      private val ivUpliftURI: URI =
//        new URI(ApplicationConfig.ivUrlUplift +
//          s"completionURL=${URLEncoder.encode(postSignInUrl, "UTF-8")}&" +
//          s"failureURL=${URLEncoder.encode(notAuthorisedUrl, "UTF-8")}" +
//          s"&confidenceLevel=$requiredConfidenceLevel")
//
//      override def children: Seq[PageVisibilityPredicate] = Seq(
//        new UpliftingIdentityConfidencePredicate(ConfidenceLevel.fromInt(requiredConfidenceLevel), ivUpliftURI)
//      )
//    }
//  }
//
//  def getIhtTaxRegime(ihtSection: IhtSection.Value) = ihtSection match {
//    case IhtSection.Registration =>
//      IhtRegimeForRegistration
//    case IhtSection.Application =>
//      IhtRegimeForApplication
//    case _ => throw new RuntimeException("Could not figure out tax regime")
//  }

  def getIVUrlForFailedConfidenceLevel(ihtSection: IhtSection.Value, requiredConfidenceLevel: Int): String = {
    lazy val ivUpliftUrl = ApplicationConfig.ivUrlUplift

    val (postSignInUrl, notAuthorisedUrl) = ihtSection match {
      case IhtSection.Registration => (ApplicationConfig.postIVRedirectUrlRegistration, ApplicationConfig.notAuthorisedRedirectUrlRegistration)
      case IhtSection.Application => (ApplicationConfig.postIVRedirectUrlApplication, ApplicationConfig.notAuthorisedRedirectUrlApplication)
      case _ => throw new RuntimeException("Could not figure out composite page visibility predicate")
    }

    ivUpliftUrl +
      s"completionURL=${URLEncoder.encode(postSignInUrl, "UTF-8")}" +
      s"&failureURL=${URLEncoder.encode(notAuthorisedUrl, "UTF-8")}" +
      s"&confidenceLevel=$requiredConfidenceLevel"
  }


  def getIhtSignInUrl: String = ApplicationConfig.ggSignInUrl

  def getIhtContinueUrl(ihtSection: IhtSection.Value): String = ihtSection match {
    case IhtSection.Registration => ApplicationConfig.postSignInRedirectUrlRegistration
    case IhtSection.Application => ApplicationConfig.postSignInRedirectUrlApplication
    case _ => throw new RuntimeException("Could not figure out tax regime")
  }

//  def getIhtCompositePageVisibilityPredicate(ihtSection: IhtSection.Value) =
//    ihtSection match {
//    case IhtSection.Registration => getCompositePageVisibilityPredicate(
//      ApplicationConfig.postIVRedirectUrlRegistration,
//      ApplicationConfig.notAuthorisedRedirectUrlRegistration,
//      ApplicationConfig.ivUpliftConfidenceLevel)
//    case IhtSection.Application => getCompositePageVisibilityPredicate(
//      ApplicationConfig.postIVRedirectUrlApplication,
//      ApplicationConfig.notAuthorisedRedirectUrlApplication,
//      ApplicationConfig.ivUpliftConfidenceLevel)
//    case _ => throw new RuntimeException("Could not figure out composite page visibility predicate")
//  }
}

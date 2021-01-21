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

package iht.config

import java.util.PropertyResourceBundle

import iht.utils.CommonHelper._
import javax.inject.{Inject, Singleton}
import org.joda.time.LocalDate
import play.api.Environment

import scala.util.{Success, Try}

@Singleton
class DefaultIHTPropertyRetriever @Inject()(val environment: Environment) extends IhtPropertyRetriever

trait IhtPropertyRetriever {
  val environment: Environment

  lazy val resourceStream: PropertyResourceBundle =
    (environment.resourceAsStream("iht.properties") flatMap { stream =>
      val optBundle: Option[PropertyResourceBundle] = Try(new PropertyResourceBundle(stream)) match {
        case Success(bundle) => Some(bundle)
        case _               => None
      }
      stream.close()
      optBundle
    }).getOrElse(throw new RuntimeException("[IhtPropertyRetriever] Could not retrieve property bundle"))

  def getProperty(key: String): String = resourceStream.getString(key).trim

  def getPropertyAsStringArray(key: String): Array[String] = getProperty(key).split(",")

  def getPropertyAsInt(key: String): Int = getProperty(key).trim.toInt

  def getPropertyAsBigDecimal(key: String): BigDecimal = BigDecimal(getProperty(key).trim)

  def getPropertyAsDate(key: String): LocalDate =
    withValue(getProperty(key).trim.split("-")) { dateAsArray =>
      new LocalDate(dateAsArray(0).toInt, dateAsArray(1).toInt, dateAsArray(2).toInt)
    }

  private[config] def parseAssignmentsToSeqTuples(listOfAssignments:String): Seq[(String, String)] = {
    if (listOfAssignments.trim.length == 0) {
      Seq()
    } else {
      val splitItems: Array[String] = listOfAssignments.split(",")
      splitItems.toSeq.map { property =>
        withValue(property.trim.split("=")) { splitProperty =>
          if(splitProperty.length == 2) {
            Tuple2(splitProperty(0).trim, splitProperty(1).trim)
          } else {
            throw new RuntimeException("Invalid property-value assignment: " + splitProperty)
          }
        }
      }
    }
  }

  def getPropertyAsSeqStringTuples(key: String): Seq[(String,String)] =
    withValue(getProperty(key).trim)(parseAssignmentsToSeqTuples)
}
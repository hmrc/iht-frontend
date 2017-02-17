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

package iht.config

import java.util.PropertyResourceBundle

import iht.utils.{CommonHelper, StringHelper}
import org.joda.time.LocalDate
import play.api.Play
import play.api.Play.current
import iht.utils.CommonHelper._

/**
 * Created by Vineet Tyagi on 28/09/15.
 */
object IhtPropertiesReader {

  val value  = Play.application.resourceAsStream("iht.properties").getOrElse(throw new RuntimeException("iht.properties file couldn't be retrieved."))
  val propertyResource = {
    try {
      new PropertyResourceBundle(value)
    } finally {
      value.close()
    }
  }

  def getProperty(key: String) = propertyResource.getString(key).trim

  def getPropertyAsStringArray(key: String): Array[String] = getProperty(key).split(",")

  def getPropertyAsInt(key: String): Int = getProperty(key).trim.toInt

  def getPropertyAsBigDecimal(key: String): BigDecimal = BigDecimal(getProperty(key).trim)

  def getPropertyAsDate(key: String): LocalDate =
    withValue(getProperty(key).trim.split("-")) { dateAsArray =>
      new LocalDate(dateAsArray(0).toInt, dateAsArray(1).toInt, dateAsArray(2).toInt)
    }

  def getPropertyAsSeqStringTuples(key: String): Seq[(String,String)] =
    withValue(getProperty(key).trim)(StringHelper.parseAssignmentsToSeqTuples)
}

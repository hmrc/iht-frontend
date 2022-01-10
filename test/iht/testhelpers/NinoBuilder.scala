/*
 * Copyright 2022 HM Revenue & Customs
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

package iht.testhelpers

import uk.gov.hmrc.domain.{Generator, Nino}

import scala.util.Random

/**
  * Created by yasar on 24/10/16.
  */
object NinoBuilder {
  def randomNino: Nino = Nino(new Generator(new Random()).nextNino.nino)
  val defaultNino = randomNino.toString()
  def addSpacesToNino(nino:String) = nino.substring(0,2)+ " " + nino.substring(2)

}

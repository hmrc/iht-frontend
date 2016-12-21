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

package iht

package object models {

  /**
    * Provides the next Id for a list of any elements that have an
    * optional string id. Note that I do not consider use of ID's in
    * this structure to be good practice.
    */
  def nextId(ts : Seq[{def id : Option[String]}]) =
  {(0 :: ts.toList.flatMap(x => x.id).map(_.toInt)).max + 1}.toString
}

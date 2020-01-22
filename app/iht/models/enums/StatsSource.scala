/*
 * Copyright 2020 HM Revenue & Customs
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

package iht.models.enums


object StatsSource extends Enumeration{
  type StatsSource =Value

  val COMPLETED_REG = Value
  val COMPLETED_REG_ADDITIONAL_EXECUTORS = Value
  val COMPLETED_APP = Value
  val ADDITIONAL_EXECUTOR_APP = Value
  val NO_ASSETS_DEBTS_EXEMPTIONS_APP = Value
  val ASSETS_ONLY_APP = Value
  val ASSETS_AND_DEBTS_ONLY_APP = Value
  val ASSET_DEBTS_EXEMPTIONS_TNRB_APP = Value
}

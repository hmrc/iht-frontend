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

package iht.models.application.basicElements

/**
  * Created by vineet on 03/11/16.
  */
trait ShareableEstateElement extends EstateElement with Shareable {

  override def isValueEntered: Boolean = {
    (value,shareValue) match{
      case(None,None) => false
      case _ => true
    }
  }

  override def totalValue: Option[BigDecimal]={
    (value,shareValue) match{
      case(None,None) => None
      case(a,b) => Some(a.getOrElse(BigDecimal(0)) + b.getOrElse(BigDecimal(0)))
    }
  }
}

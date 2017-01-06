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

package iht.utils

import play.api.data._

import scala.collection.mutable.ListBuffer

case class DoubleValidatingForm[T] (
                                     form : Form[T],
                                     extraErrorHandling : Option[PartialFunction[T,Seq[(String,String)]]] = None
                                     )  {

  def apply(x : String) = form.apply(x)

  def bindFromRequest()(implicit request: play.api.mvc.Request[_]): DoubleValidatingForm[T] =
    this.copy ( form = form.bindFromRequest()(request) )

  def fold[R](
               hasErrors: Form[T] => R,
               success: T => R
               ): R = form.value match {
    case Some(v) if form.errors.isEmpty =>
      extraErrorHandling match {
        case Some(ex) if ex.isDefinedAt(v) && !ex(v).isEmpty => {
          var errors = new ListBuffer[FormError]()
          ex(v).foreach { value =>
            errors += (FormError(value._1, value._2))
          }
          hasErrors (form.copy(errors = errors, value = form.value))
        }
        case _ => {
          success(v)
        }
      }
    case _ => hasErrors(form)
  }

  def validating(constraint : PartialFunction[T,Seq[(String,String)]]) = {
    this copy (
      extraErrorHandling = Some(constraint)
      )
  }
}

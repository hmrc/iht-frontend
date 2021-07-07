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

package iht.views.ihtHelpers.standard

import iht.FakeIhtApp
import iht.views.HtmlSpec
import iht.views.html.ihtHelpers.standard.article
import play.twirl.api.Html

class ArticleTest extends FakeIhtApp with HtmlSpec {
  lazy val articleView: article = app.injector.instanceOf[article]

  "article" must {

    "display the correct value for articleClasses, content and no lang attribute" in {
      val result = articleView(Html("test"), false, None, false, "en")(messagesApi.preferred(createFakeRequest()))
      val doc = asDocument(result)

      val tag = doc.getElementsByTag("article")
      tag.attr("articleClasses") mustBe empty
      tag.attr("lang") mustBe empty
      tag.html() mustBe "test"
    }

    "display the lang attribute when currentLang is Welsh " in {
      val result = articleView(Html("test"), false, None, false, "cy")(messagesApi.preferred(createFakeRequest()))
      val doc = asDocument(result)

      val tag = doc.getElementsByTag("article")
      tag.attr("articleClasses") mustBe empty
      tag.attr("lang") mustBe "cy"
      tag.html() mustBe "test"
    }

  }
}

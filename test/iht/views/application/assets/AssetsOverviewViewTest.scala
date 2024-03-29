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

package iht.views.application.assets

import iht.controllers.application.assets.pensions.routes._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.views.ViewTestHelper
import iht.views.html.application.asset.assets_overview

class AssetsOverviewViewTest extends ViewTestHelper  {

  lazy val assetsOverviewView: assets_overview = app.injector.instanceOf[assets_overview]

  "assets_overview" must {

    "have no message keys in html" in {
      implicit val request = createFakeRequest()
      val assets = AllAssets(privatePension = Some(PrivatePension(isChanged = None, value = None, isOwned = Some(true))))
      val view = assetsOverviewView(ApplicationDetails(allAssets = Some(assets)), assets, "", "").toString()
      noMessageKeysShouldBePresent(view)
    }

    def assertPensionOvervewLinkPointsToCorrectPage(hasPension: Option[Boolean], url: String) = {
      implicit val request = createFakeRequest()
      val assets = AllAssets(privatePension = Some(PrivatePension(isChanged = None, value = None, isOwned = hasPension)))
      assetsOverviewView(ApplicationDetails(allAssets = Some(assets)), assets, "", "").toString() must include(url)
    }

    "link to the pension Yes/No screen when the user has not indicated that they have a private pension" in {
      assertPensionOvervewLinkPointsToCorrectPage(None, PensionsOwnedQuestionController.onPageLoad.url)
    }

    "link to the pension Yes/No screen when the user has explicitly stated there are no private pensions" in {
      assertPensionOvervewLinkPointsToCorrectPage(Some(false), PensionsOwnedQuestionController.onPageLoad.url)
    }

    "link to the pension overview screen when the user has explicitly stated there are private pension(s)" in {
      assertPensionOvervewLinkPointsToCorrectPage(Some(true), PensionsOverviewController.onPageLoad.url)
    }
  }
}

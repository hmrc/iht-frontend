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

package iht.models.application.assets

import iht.testhelpers.{AssetsWithAllSectionsSetToNoBuilder, CommonBuilder}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 03/11/16.
  */
class AllAssetsTest extends UnitSpec with MockitoSugar{

  "totalValueWithoutProperties" must {
    "returns total assets value without properties" in {
      val allAssets = CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(properties = None)
      allAssets.totalValueWithoutProperties shouldBe (BigDecimal(1500))
    }
  }

  "totalValueWithoutPropertiesOption" must {

    "returns optional total assets value without properties" in {
      val allAssets = CommonBuilder.buildAllAssetsWithAllSectionsFilled.copy(properties = None)
      allAssets.totalValueWithoutPropertiesOption shouldBe Some((BigDecimal(1500)))
    }

    "returns None when there is no Assets" in {
      val allAssets = CommonBuilder.buildAllAssets
      allAssets.totalValueWithoutPropertiesOption shouldBe empty
    }
  }

  "areAllAssetsSectionsAnsweredNo" must {
    "returns true when all sections answered no in assets" in {

      val allAssetsWithAnsweredNo = AssetsWithAllSectionsSetToNoBuilder.buildAllAssets.copy(
        other = Some(CommonBuilder.buildBasicElement.copy(isOwned = Some(false), value = Some(BigDecimal(1000)))))

      allAssetsWithAnsweredNo.areAllAssetsSectionsAnsweredNo shouldBe true
    }

    "returns false when all but one section answered no in assets" in {
      val allAssetsWithOneNoAnswer = AssetsWithAllSectionsSetToNoBuilder.buildAllAssets copy (
        foreign = Some(CommonBuilder.buildBasicElement.copy(isOwned = Some(false), value = None))
        )

      allAssetsWithOneNoAnswer.areAllAssetsSectionsAnsweredNo shouldBe false
    }
  }
}

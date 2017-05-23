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

import iht.FakeIhtApp
import iht.models.application.gifts.PreviousYearsGifts
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.test.UnitSpec

class GiftsHelperTest extends UnitSpec with FakeIhtApp with MockitoSugar {

  "GiftsHelper" must {
    "generate sequence of years needed for gifts" in {
      implicit val request = createFakeRequest()
      val dateForGifts = new LocalDate(2015, 12, 30)
      val giftsForYears = GiftsHelper.createPreviousYearsGiftsLists(dateForGifts)
      val previousYears = List(
        PreviousYearsGifts(Some("1"), None, None, Some("2008-12-31"), Some("2009-4-5")),
        PreviousYearsGifts(Some("2"), None, None, Some("2009-4-6"), Some("2010-4-5")),
        PreviousYearsGifts(Some("3"), None, None, Some("2010-4-6"), Some("2011-4-5")),
        PreviousYearsGifts(Some("4"), None, None, Some("2011-4-6"), Some("2012-4-5")),
        PreviousYearsGifts(Some("5"), None, None, Some("2012-4-6"), Some("2013-4-5")),
        PreviousYearsGifts(Some("6"), None, None, Some("2013-4-6"), Some("2014-4-5")),
        PreviousYearsGifts(Some("7"), None, None, Some("2014-4-6"), Some("2015-4-5")),
        PreviousYearsGifts(Some("8"), None, None, Some("2015-4-6"), Some("2015-12-30")))

      giftsForYears should be(previousYears)
    }

    "generate sequence of years needed for gifts where date of death is in the 1st quarter of the year" in {
      implicit val request = createFakeRequest()
      val dateForGifts = new LocalDate(2014, 3, 12)
      val giftsForYears = GiftsHelper.createPreviousYearsGiftsLists(dateForGifts)
      val previousYears = List(
        PreviousYearsGifts(Some("1"), None, None, Some("2007-3-13"), Some("2007-4-5")),
        PreviousYearsGifts(Some("2"), None, None, Some("2007-4-6"), Some("2008-4-5")),
        PreviousYearsGifts(Some("3"), None, None, Some("2008-4-6"), Some("2009-4-5")),
        PreviousYearsGifts(Some("4"), None, None, Some("2009-4-6"), Some("2010-4-5")),
        PreviousYearsGifts(Some("5"), None, None, Some("2010-4-6"), Some("2011-4-5")),
        PreviousYearsGifts(Some("6"), None, None, Some("2011-4-6"), Some("2012-4-5")),
        PreviousYearsGifts(Some("7"), None, None, Some("2012-4-6"), Some("2013-4-5")),
        PreviousYearsGifts(Some("8"), None, None, Some("2013-4-6"), Some("2014-3-12")))

      giftsForYears should be(previousYears)

    }

    "generate exact 7 financial years if deceased died on 5 April of any year" in {
      implicit val request = createFakeRequest()
      val dateForGifts = new LocalDate(2015, 4, 5)
      val giftsForYears = GiftsHelper.createPreviousYearsGiftsLists(dateForGifts)
      val previousYears = List(
        PreviousYearsGifts(Some("1"), None, None, Some("2008-4-6"), Some("2009-4-5")),
        PreviousYearsGifts(Some("2"), None, None, Some("2009-4-6"), Some("2010-4-5")),
        PreviousYearsGifts(Some("3"), None, None, Some("2010-4-6"), Some("2011-4-5")),
        PreviousYearsGifts(Some("4"), None, None, Some("2011-4-6"), Some("2012-4-5")),
        PreviousYearsGifts(Some("5"), None, None, Some("2012-4-6"), Some("2013-4-5")),
        PreviousYearsGifts(Some("6"), None, None, Some("2013-4-6"), Some("2014-4-5")),
        PreviousYearsGifts(Some("7"), None, None, Some("2014-4-6"), Some("2015-4-5")))

      giftsForYears should be(previousYears)
    }
  }
}

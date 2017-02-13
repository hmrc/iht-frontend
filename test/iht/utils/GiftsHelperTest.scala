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
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  "GiftsHelper" must {
    "generate sequence of years needed for gifts" in {
      implicit val request = createFakeRequest()
      val dateForGifts = new LocalDate(2015, 12, 30)
      val giftsForYears = GiftsHelper.createPreviousYearsGiftsLists(dateForGifts)
      val previousYears = List(
        PreviousYearsGifts(Some("1"), None, None, Some("31 December 2008"), Some("5 April 2009")),
        PreviousYearsGifts(Some("2"), None, None, Some("6 April 2009"), Some("5 April 2010")),
        PreviousYearsGifts(Some("3"), None, None, Some("6 April 2010"), Some("5 April 2011")),
        PreviousYearsGifts(Some("4"), None, None, Some("6 April 2011"), Some("5 April 2012")),
        PreviousYearsGifts(Some("5"), None, None, Some("6 April 2012"), Some("5 April 2013")),
        PreviousYearsGifts(Some("6"), None, None, Some("6 April 2013"), Some("5 April 2014")),
        PreviousYearsGifts(Some("7"), None, None, Some("6 April 2014"), Some("5 April 2015")),
        PreviousYearsGifts(Some("8"), None, None, Some("6 April 2015"), Some("30 December 2015")))

      giftsForYears should be(previousYears)
    }

    "generate sequence of years needed for gifts where date of death is in the 1st quarter of the year" in {
      implicit val request = createFakeRequest()
      val dateForGifts = new LocalDate(2014, 3, 12)
      val giftsForYears = GiftsHelper.createPreviousYearsGiftsLists(dateForGifts)
      val previousYears = List(
        PreviousYearsGifts(Some("1"), None, None, Some("13 March 2007"), Some("5 April 2007")),
        PreviousYearsGifts(Some("2"), None, None, Some("6 April 2007"), Some("5 April 2008")),
        PreviousYearsGifts(Some("3"), None, None, Some("6 April 2008"), Some("5 April 2009")),
        PreviousYearsGifts(Some("4"), None, None, Some("6 April 2009"), Some("5 April 2010")),
        PreviousYearsGifts(Some("5"), None, None, Some("6 April 2010"), Some("5 April 2011")),
        PreviousYearsGifts(Some("6"), None, None, Some("6 April 2011"), Some("5 April 2012")),
        PreviousYearsGifts(Some("7"), None, None, Some("6 April 2012"), Some("5 April 2013")),
        PreviousYearsGifts(Some("8"), None, None, Some("6 April 2013"), Some("12 March 2014")))

      giftsForYears should be(previousYears)

    }

    "generate exact 7 financial years if deceased died on 5 April of any year" in {
      implicit val request = createFakeRequest()
      val dateForGifts = new LocalDate(2015, 4, 5)
      val giftsForYears = GiftsHelper.createPreviousYearsGiftsLists(dateForGifts)
      val previousYears = List(
        PreviousYearsGifts(Some("1"), None, None, Some("6 April 2008"), Some("5 April 2009")),
        PreviousYearsGifts(Some("2"), None, None, Some("6 April 2009"), Some("5 April 2010")),
        PreviousYearsGifts(Some("3"), None, None, Some("6 April 2010"), Some("5 April 2011")),
        PreviousYearsGifts(Some("4"), None, None, Some("6 April 2011"), Some("5 April 2012")),
        PreviousYearsGifts(Some("5"), None, None, Some("6 April 2012"), Some("5 April 2013")),
        PreviousYearsGifts(Some("6"), None, None, Some("6 April 2013"), Some("5 April 2014")),
        PreviousYearsGifts(Some("7"), None, None, Some("6 April 2014"), Some("5 April 2015")))

      giftsForYears should be(previousYears)
    }
  }
}

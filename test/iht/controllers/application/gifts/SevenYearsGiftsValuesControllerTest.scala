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

package iht.controllers.application.gifts

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.models.application.gifts.PreviousYearsGifts
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import iht.views.HtmlSpec
import org.joda.time.LocalDate
import org.jsoup.nodes.Document
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier

/**
 * Created by james on 14/01/16.
 */
class SevenYearsGiftsValuesControllerTest extends ApplicationControllerTest with HtmlSpec {

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val hc = new HeaderCarrier()
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def sevenYearsGiftsValuesController = new SevenYearsGiftsValuesController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  def sevenYearsGiftsValuesControllerNotAuthorised = new SevenYearsGiftsValuesController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val ihtConnector = mockIhtConnector
    override val isWhiteListEnabled = false
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC1234567890"),
    deceasedDateOfDeath = Some(CommonBuilder.buildDeceasedDateOfDeath)
    )

  "SevenYearsGiftsValuesController" must {
    "return OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = sevenYearsGiftsValuesController.onPageLoad()(createFakeRequest(isAuthorised = true))
      status(result) should be(OK)
    }

    "display guidance text on the page" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = sevenYearsGiftsValuesController.onPageLoad()(createFakeRequest(isAuthorised = true))
      status(result) should be(OK)
      contentAsString(result) should include(Messages("page.iht.application.gifts.sevenYears.values.guidance"))
    }

    "redirect to ida login page on PageLoad if the user is not logged in" in {
      val result = sevenYearsGiftsValuesControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(loginUrl))
    }

    "on page load have application details from local storage over secure storage" in {
      val previousYears = List(
        PreviousYearsGifts(Some("1"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2008"), Some("5 April 2009")),
        PreviousYearsGifts(Some("2"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2009"), Some("5 April 2010")),
        PreviousYearsGifts(Some("3"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2010"), Some("5 April 2011")),
        PreviousYearsGifts(Some("4"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2011"), Some("5 April 2012")),
        PreviousYearsGifts(Some("5"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2012"), Some("5 April 2013")),
        PreviousYearsGifts(Some("6"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2013"), Some("5 April 2014")),
        PreviousYearsGifts(Some("7"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2014"), Some("5 April 2015")))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allGifts = Some(CommonBuilder.buildAllGifts),
        giftsList = Some(previousYears)
      )

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = sevenYearsGiftsValuesController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include("6 April 2008")
      contentAsString(result) should include("5 April 2009")
    }

    "change values message is shown instead of give values if 0 is entered by user" in {
      val previousYears = List(
        PreviousYearsGifts(Some("1"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2008"), Some("5 April 2009")),
        PreviousYearsGifts(Some("2"), None, None, Some("6 April 2009"), Some("5 April 2010")),
        PreviousYearsGifts(Some("3"), None, None, Some("6 April 2010"), Some("5 April 2011")),
        PreviousYearsGifts(Some("4"), None, None, Some("6 April 2011"), Some("5 April 2012")),
        PreviousYearsGifts(Some("5"), None, None, Some("6 April 2012"), Some("5 April 2013")),
        PreviousYearsGifts(Some("6"), None, None, Some("6 April 2013"), Some("5 April 2014")),
        PreviousYearsGifts(Some("7"), None, None, Some("6 April 2014"), Some("5 April 2015")))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allGifts = Some(CommonBuilder.buildAllGifts),
        giftsList = Some(previousYears)
      )

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        regDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails,
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = sevenYearsGiftsValuesController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      contentAsString(result) should include(Messages("iht.estateReport.changeValues"))

    }

    "page loads when there are no gifts in persist storage" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        regDetails = registrationDetails,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = sevenYearsGiftsValuesController.onPageLoad()(createFakeRequest(isAuthorised = true))
      status(result) should be(OK)
    }

    "display a pound sign next to individual value of gifts and value of exemptions in table" in {
      val previousYears = List(
        PreviousYearsGifts(Some("1"), Some(BigDecimal(10)), Some(BigDecimal(6)), Some("6 April 2008"), Some("5 April 2009")),
        PreviousYearsGifts(Some("2"), Some(BigDecimal(100)), Some(BigDecimal(30)), Some("6 April 2009"), Some("5 April 2010")),
        PreviousYearsGifts(Some("3"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2010"), Some("5 April 2011")),
        PreviousYearsGifts(Some("4"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2011"), Some("5 April 2012")),
        PreviousYearsGifts(Some("5"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2012"), Some("5 April 2013")),
        PreviousYearsGifts(Some("6"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2013"), Some("5 April 2014")),
        PreviousYearsGifts(Some("7"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2014"), Some("5 April 2015")))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allGifts = Some(CommonBuilder.buildAllGifts),
        giftsList = Some(previousYears)
      )

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        regDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails,
        getAppDetailsFromCache = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val result = sevenYearsGiftsValuesController.onPageLoad()(createFakeRequest())
      status(result) should be(OK)
      /*Previous Years Gifts 1*/
      contentAsString(result) should include("£10") //value
      contentAsString(result) should include("£6") //exemptions
      contentAsString(result) should include("£4") //total
      /*Previous Years Gifts 2*/
      contentAsString(result) should include("£100") //value
      contentAsString(result) should include("£30") //exemptions
      contentAsString(result) should include("£70") //total
    }


      "display correct totals at bottom of gifts table when data is entered" in {
        val previousYears = List(
          PreviousYearsGifts(Some("1"), Some(BigDecimal(10)), Some(BigDecimal(5)), Some("6 April 2008"), Some("5 April 2009")),
          PreviousYearsGifts(Some("2"), Some(BigDecimal(100)), Some(BigDecimal(30)), Some("6 April 2009"), Some("5 April 2010")),
          PreviousYearsGifts(Some("3"), Some(BigDecimal(30)), Some(BigDecimal(50)), Some("6 April 2010"), Some("5 April 2011")),
          PreviousYearsGifts(Some("4"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2011"), Some("5 April 2012")),
          PreviousYearsGifts(Some("5"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2012"), Some("5 April 2013")),
          PreviousYearsGifts(Some("6"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2013"), Some("5 April 2014")),
          PreviousYearsGifts(Some("7"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2014"), Some("5 April 2015")))

        val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
          allGifts = Some(CommonBuilder.buildAllGifts),
          giftsList = Some(previousYears)
        )

        createMocksForApplication(mockCachingConnector,
          mockIhtConnector,
          appDetails = Some(applicationDetails),
          regDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails,
          getAppDetails = true,
          getAppDetailsFromCache = true,
          saveAppDetails = true,
          storeAppDetailsInCache = true)

        val result = sevenYearsGiftsValuesController.onPageLoad()(createFakeRequest())

        status(result) should be(OK)
        contentAsString(result) should include("£140") //gifts value
        contentAsString(result) should include("£85") //exemptions value
        contentAsString(result) should include("£55") //total value added over 7 years
      }

      "contain the correct links for all the gift years " in {

        val previousYears = List(
          PreviousYearsGifts(Some("1"), Some(BigDecimal(10)), Some(BigDecimal(5)), Some("6 April 2008"), Some("5 April 2009")),
          PreviousYearsGifts(Some("2"), Some(BigDecimal(100)), Some(BigDecimal(30)), Some("6 April 2009"), Some("5 April 2010")),
          PreviousYearsGifts(Some("3"), Some(BigDecimal(30)), Some(BigDecimal(50)), Some("6 April 2010"), Some("5 April 2011")),
          PreviousYearsGifts(Some("4"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2011"), Some("5 April 2012")),
          PreviousYearsGifts(Some("5"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2012"), Some("5 April 2013")),
          PreviousYearsGifts(Some("6"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2013"), Some("5 April 2014")),
          PreviousYearsGifts(Some("7"), Some(BigDecimal(0)), Some(BigDecimal(0)), Some("6 April 2014"), Some("5 April 2015")))

        val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
          allGifts = Some(CommonBuilder.buildAllGifts),
          giftsList = Some(previousYears)
        )

        createMocksForApplication(mockCachingConnector,
          mockIhtConnector,
          appDetails = Some(applicationDetails),
          getAppDetails = true,
          regDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails,
          getAppDetailsFromCache = true,
          saveAppDetails = true,
          storeAppDetailsInCache = true)

        val result = sevenYearsGiftsValuesController.onPageLoad()(createFakeRequest())

        status(result) should be(OK)
        val content = contentAsString(result)
        val doc: Document = asDocument(content)

        // Testing link data for all the gift years
        for (previousYearGift <- previousYears)
          testGiftYearLinkData(doc, previousYearGift.yearId.getOrElse(""))
      }
    }

    private def testGiftYearLinkData(doc: Document, yearId: String) =
    {
      val yearLink = doc.getElementById(s"edit-gift-$yearId")
      assertEqualsValue(doc, s"a#edit-gift-$yearId span",
        Messages("iht.estateReport.changeValues"))
      yearLink.attr("href") shouldBe
        routes.GiftsDetailsController.onPageLoad(yearId).url
    }

}

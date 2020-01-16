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

package iht.testhelpers

import iht.config.AppConfig
import iht.connector.{CachingConnector, CitizenDetailsConnector, IhtConnector}
import iht.constants.Constants
import iht.models._
import iht.models.application.{ApplicationDetails, IhtApplication, ProbateDetails}
import iht.models.des.ihtReturn.IHTReturn
import iht.testhelpers.CommonBuilder._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import uk.gov.hmrc.http.NotFoundException

import scala.concurrent.Future


trait MockObjectBuilder {
  implicit val mockAppConfig: AppConfig

  def createMockToGetCaseDetails(ihtConnector: IhtConnector,
                                 regDetails: Future[RegistrationDetails] = Future.successful(buildRegistrationDetailsWithDeceasedAndIhtRefDetails))  = {
    when(ihtConnector.getCaseDetails(any(), any())(any()))
      .thenReturn(regDetails)
  }

  val defaultAppDetails = ApplicationDetails(allAssets= None,
    propertyList = Nil,
    allLiabilities = None,
    allExemptions = None,
    allGifts = None,
    giftsList = None,
    charities = Seq(),
    qualifyingBodies = Seq(),
    widowCheck = None,
    increaseIhtThreshold = None,
    status = TestHelper.AppStatusInProgress,
    kickoutReason = Some("This has been kept to be used to distinguish from other application objects"),
    ihtRef = Some("AH6566565656565"),
    reasonForBeingBelowLimit = None)

  // Creates Registration Details with Default Deceased Details and ihtRef=ABC123
  def buildRegistrationDetailsWithDeceasedAndIhtRefDetails = buildRegistrationDetails copy (
    deceasedDetails = Some(buildDeceasedDetails), ihtReference = Some("AbC123"))

  def createMockToGetCitizenDetails(connector: CitizenDetailsConnector, person: CidPerson) =
    when(connector.getCitizenDetails(any())(any(), any())).thenReturn(Future.successful(person))

  def createMockToThrowExceptionWhenGettingCitizenDetails(connector: CitizenDetailsConnector) =
    when(connector.getCitizenDetails(any())(any(), any())).thenThrow(new RuntimeException)

  def createMockToThrowNotFoundExceptionWhenGettingCitizenDetails(connector: CitizenDetailsConnector) =
    when(connector.getCitizenDetails(any())(any(), any())).thenReturn(Future.failed(new NotFoundException("")))

  /**
    * Creates Mock to store RegistrationDetails in Cache using CachingConnector
    */
  def createMockToStoreRegDetailsInCacheWithFailure(cachingConnector: CachingConnector,
                                                    regDetails: Option[RegistrationDetails] = Some(buildRegistrationDetails)) = {
    when(cachingConnector.storeRegistrationDetails(any())(any(), any()))
      .thenReturn(Future.successful(None))
  }

  /**
    * Creates Mock To store ApplicationDetails using CachingConnector
    */
  def createMockToStoreApplicationDetailsInCache(cachingConnector: CachingConnector,
                                                 appDetails: Option[ApplicationDetails] = Some(buildApplicationDetails)) = {
    when(cachingConnector.storeApplicationDetails(any())(any(), any()))
      .thenReturn(Future.successful(appDetails))
  }

  /**
    * Creates Mock To get Existing RegistrationDetails using CachingConnector
    */
  def createMockToThrowExceptionWhileGettingExistingRegDetails(cachingConnector: CachingConnector,
                                                               exceptionMsg: String = "RunTime Exception Occured") = {
    when(cachingConnector.getRegistrationDetails(any(), any()))
      .thenThrow(new RuntimeException(exceptionMsg))
  }

  /**
    * Creates mock to getCaseDetails using IhtConnector
    */

  /**
    * Creates mock to getCaseList using IhtConnector
    */

  def createMockToGetCaseList(ihtConnector: IhtConnector, ihtAppList: Seq[IhtApplication] = Nil)={
    when(ihtConnector.getCaseList(any())(any())).thenReturn(Future.successful(ihtAppList))
  }

  /**
    * Creates mock to getProbateDetails using IhtConnector
    */
  def createMockToGetProbateDetails(ihtConnector: IhtConnector,
                                    probateDetails: Option[ProbateDetails] = Some(buildProbateDetails)) = {
    when(ihtConnector.getProbateDetails(any(), any(), any())(any()))
      .thenReturn(Future.successful(probateDetails))
  }

  /**
    * Creates mock to getProbateDetails from cache using CacheConnector
    */
  def createMockToGetProbateDetailsFromCache(cachingConnector: CachingConnector,
                                             probateDetails: Option[ProbateDetails] = Some(buildProbateDetails)) = {
    when(cachingConnector.getProbateDetails(any(), any()))
      .thenReturn(Future.successful(probateDetails))
  }

  /**
    * Creates mock to store Probate Details in cache using CachingConnector
    */
  def createMockToStoreProbateDetailsInCache(cachingConnector: CachingConnector,
                                             probateDetails: Option[ProbateDetails] = Some(buildProbateDetails)) = {

    when(cachingConnector.storeProbateDetails(any())(any(),any()))
      .thenReturn(Future.successful(probateDetails))
  }

  /**
    * Creates mock to submit the application using IhtConnector
    */
  def createMockToSubmitApplication(ihtConnector: IhtConnector ,
                                    returnId: Option[String] = Some("XXX")) = {
    when(ihtConnector.submitApplication(any(),any(),any())(any(), any())).thenReturn(Future.successful(returnId))
  }

  /**
    * Creates mock to get submitted applicationusing IhtConnector
    */

  def createMockToGetSubmittedApplicationDetails(ihtConnector: IhtConnector,
                                                 ihtReturn: Option[IHTReturn] = Some(CommonBuilder.buildIHTReturn)) = {
    when(ihtConnector.getSubmittedApplicationDetails(any(), any(), any())(any()))
      .thenReturn(Future.successful(ihtReturn))
  }

  /**
    * Creates mock to submit registration using IhtConnector
    */
  def createMockToSubmitRegistration(ihtConnector: IhtConnector,
                                     returnIhtRef: String = "ABC123") = {
    when(ihtConnector.submitRegistration(any(), any())(any(), any()))
      .thenReturn(Future.successful(returnIhtRef))
  }

  /**
    * Creates mock to get the realtime risk message using IhtConnector
    */
  def createMockToGetRealtimeRiskMessage(ihtConnector: IhtConnector,
                                         riskResponse: Option[String]= Some("")) = {
    when(ihtConnector.getRealtimeRiskingMessage(any(), any())(any()))
      .thenReturn(Future.successful(riskResponse))

  }

  /**
    * Creates mock to doNothing when deleteSingleValue sync from cache using CachingConnector
    */
  def createMockToDoNothingWhenDeleteSingleValueFromCache(cachingConnector: CachingConnector)={
    doNothing().when(cachingConnector).deleteSingleValue(any())(any(),any())
  }

  /**
    * Creates mock to doNothing when deleteApplication using IhtConnector
    */
  def createMockToDoNothingWhenDeleteApplication(ihtConnector: IhtConnector)={
    doNothing().when(ihtConnector).deleteApplication(any(),any())(any())
  }

  def createMockToRequestClearance(ihtConnector: IhtConnector,
                                   clearanceResponse: Boolean = true) = {
    when(ihtConnector.requestClearance(any(),any())(any()))
      .thenReturn(Future.successful(clearanceResponse))
  }

  /**
    * Creates mock for default registration object and for others as per the supplied arguements.
    */
  def createMocksForApplication(cachingConnector: CachingConnector,
                                ihtConnector: IhtConnector,
                                regDetails: RegistrationDetails = buildRegistrationDetailsWithDeceasedAndIhtRefDetails,
                                appDetails: Option[ApplicationDetails] = Some(buildApplicationDetails),
                                singleValue: Option[String] = None,
                                getAppDetailsObject: Option[ApplicationDetails] = Some(defaultAppDetails),
                                getAppDetailsFromCacheObject: Option[ApplicationDetails]  = Some(defaultAppDetails),
                                saveAppDetailsObject: Option[ApplicationDetails] = Some(defaultAppDetails),
                                storeAppDetailsInCacheObject: Option[ApplicationDetails] = Some(defaultAppDetails),
                                getAppDetails: Boolean = false,
                                getAppDetailsFromCache: Boolean = false,
                                saveAppDetails: Boolean = false,
                                storeAppDetailsInCache: Boolean = false,
                                getSingleValueFromCache: Boolean = false) = {

    createMockToGetRegDetailsFromCacheNoOption(cachingConnector, Future.successful(Some(regDetails)))

    if (getAppDetails) {

      createMockToGetApplicationDetails(ihtConnector, getUpdatedAppDetailsObject(appDetails, getAppDetailsObject))
    }

    if(getAppDetailsFromCache) {
      createMockToGetApplicationDetailsFromCache(cachingConnector, getUpdatedAppDetailsObject(appDetails, getAppDetailsFromCacheObject))
    }

    if (saveAppDetails) {
      createMockToSaveApplicationDetails(ihtConnector, getUpdatedAppDetailsObject(appDetails, saveAppDetailsObject))
    }

    if (getSingleValueFromCache) {
      createMockToGetSingleValueFromCache(cachingConnector, singleValueReturn = singleValue)
    }
  }

  /**
    * Creates Mock To get Existing RegistrationDetails using CachingConnector
    */
  def createMockToGetRegDetailsFromCacheNoOption(cachingConnector: CachingConnector,
                                                 regDetails: Future[Option[RegistrationDetails]] = Future.successful(Some(buildRegistrationDetailsWithDeceasedAndIhtRefDetails))) = {
    when(cachingConnector.getRegistrationDetails(any(), any()))
      .thenReturn(regDetails)
  }

  /**
    * Creates Mock To get ApplicationDetails using IhtConnector
    */
  def createMockToGetApplicationDetails(ihtConnector: IhtConnector,
                                        appDetails: Option[ApplicationDetails] = Some(buildApplicationDetails)) = {
    when(ihtConnector.getApplication(any(), any(), any())(any()))
      .thenReturn(Future.successful(appDetails))
  }

  /**
    * Creates Mock To get ApplicationDetails using CachingConnector
    */
  def createMockToGetApplicationDetailsFromCache(cachingConnector: CachingConnector,
                                                 appDetails: Option[ApplicationDetails] = Some(buildApplicationDetails)) = {
    when(cachingConnector.getApplicationDetails(any(), any()))
      .thenReturn(Future.successful(appDetails))
  }

  /**
    * Creates Mock To save ApplicationDetails using IhtConnector
    */
  def createMockToSaveApplicationDetails(ihtConnector: IhtConnector,
                                         appDetails: Option[ApplicationDetails] = Some(buildApplicationDetails)) = {
    when(ihtConnector.saveApplication(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(appDetails))
  }

  private def getUpdatedAppDetailsObject(appDetails: Option[ApplicationDetails],
                                         otherAppDetailsObject: Option[ApplicationDetails]) : Option[ApplicationDetails]= {
    if(!otherAppDetailsObject.isDefined) {
      otherAppDetailsObject
    } else {
      if(!(otherAppDetailsObject.get == defaultAppDetails)) {
        otherAppDetailsObject
      }else {
        appDetails
      }
    }
  }

  def createMockForRegistration(cachingConnector: CachingConnector,
                                regDetails: Option[RegistrationDetails] = Some(defaultRegDetails),
                                getRegDetailsFromCache: Boolean = false,
                                getExistingRegDetailsFromCache: Boolean = false,
                                storeRegDetailsInCache: Boolean = false,
                                getRegDetailsFromCacheObject: Option[RegistrationDetails] = Some(defaultRegDetails),
                                getExistingRegDetailsFromCacheObject: RegistrationDetails = defaultRegDetails,
                                storeRegDetailsInCacheObject: Option[RegistrationDetails] = Some(defaultRegDetails)) = {

    if(getRegDetailsFromCache){
      createMockToGetRegDetailsFromCache(cachingConnector, getUpdatedRegDetailsObject(regDetails, getRegDetailsFromCacheObject))
    }

    if(getExistingRegDetailsFromCache){
      createMockToGetRegDetailsFromCacheNoOption(cachingConnector, getUpdatedRegDetailsObject(regDetails,
        Some(getExistingRegDetailsFromCacheObject)))
    }

    if(storeRegDetailsInCache) {
      createMockToStoreRegDetailsInCache(cachingConnector, getUpdatedRegDetailsObject(regDetails, storeRegDetailsInCacheObject))
    }

  }

  /**
    * Creates mock to get the RegistrationDetails from the cache using CachingConnector
    */
  def createMockToGetRegDetailsFromCache(cachingConnector: CachingConnector,
                                         regDetails: Future[Option[RegistrationDetails]] = Future.successful(Some(buildRegistrationDetails)))
                                          = {
    when(cachingConnector.getRegistrationDetails(any(), any())).thenReturn(regDetails)
  }

  /**
    * Creates Mock to store RegistrationDetails in Cache using CachingConnector
    */
  def createMockToStoreRegDetailsInCache(cachingConnector: CachingConnector,
                                         regDetails: Future[Option[RegistrationDetails]] = Future.successful(Some(buildRegistrationDetails))) = {
    when(cachingConnector.storeRegistrationDetails(any())(any(), any()))
      .thenReturn(regDetails)
  }

  private def getUpdatedRegDetailsObject(regDetails: Option[RegistrationDetails],
                                         otherRegDetailsObject: Option[RegistrationDetails]) : Future[Option[RegistrationDetails]]= {
    if(!otherRegDetailsObject.isDefined) {
      Future.successful(otherRegDetailsObject)
    } else {
      if(!(otherRegDetailsObject.get == defaultRegDetails)) {
        Future.successful(otherRegDetailsObject)
      }else {
        Future.successful(regDetails)
      }
    }
  }

  def defaultRegDetails = CommonBuilder.buildRegistrationDetails.copy(acknowledgmentReference =
    "This has been kept to be used to distinguish from other registration details objects")

  def createMocksForExemptionsGuidanceSingleValue(cachingConnector: CachingConnector,
                                                  finalDestinationURL: String) = {
    createMockToGetSingleValueFromCache(cachingConnector,
      same(Constants.ExemptionsGuidanceContinueUrlKey), None)

    createMockToStoreSingleValueInCache(cachingConnector,
      same(Constants.ExemptionsGuidanceContinueUrlKey),
      Some(finalDestinationURL))
    createMockToDeleteKeyFromCache(cachingConnector, Constants.ExemptionsGuidanceContinueUrlKey)
  }

  /**
    * Create mock to getSingleValueSync from cache using CachingConnector
    */

  def createMockToGetSingleValueFromCache(cachingConnector: CachingConnector,
                                          singleValueFormKey: String = any(),
                                          singleValueReturn: Option[String]) = {
    when(cachingConnector.getSingleValue(singleValueFormKey)(any(), any()))
      .thenReturn(Future.successful(singleValueReturn))
  }

  /**
    * Creates mock to store single value in cache using CachingConnector
    */
  def createMockToStoreSingleValueInCache(cachingConnector: CachingConnector,
                                          singleValueFormKey: String = any(),
                                          singleValueReturn: Option[String]) = {
    when(cachingConnector.storeSingleValue(singleValueFormKey, any())(any(), any()))
      .thenReturn(Future.successful(singleValueReturn))
  }

  /**
    * Creates mock to delete key from Cache
    */
  def createMockToDeleteKeyFromCache[A](cachingConnector: CachingConnector, key: A): OngoingStubbing[Future[Any]] = {
    when(cachingConnector.cacheDelete(any())(any(), any())).thenReturn(Future.successful(key))
  }

}

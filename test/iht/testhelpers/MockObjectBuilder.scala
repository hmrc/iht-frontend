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

package iht.testhelpers

import iht.connector.{CachingConnector, CitizenDetailsConnector, IhtConnector}
import iht.constants.Constants
import iht.models._
import iht.models.application.{IhtApplication, ApplicationDetails, ProbateDetails}
import iht.testhelpers.CommonBuilder._
import models.des.iht_return.IHTReturn
import org.mockito.Matchers._
import org.mockito.Mockito._

import scala.concurrent.Future

/**
  * Created by vineet on 16/03/16.
  */
object MockObjectBuilder {

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

  val defaultRegDetails = CommonBuilder.buildRegistrationDetails.copy(acknowledgmentReference =
    "This has been kept to be used to distinguish from other registration details objects")

  // Creates Registration Details with Default Deceased Details and ihtRef=ABC123
  val buildRegistrationDetailsWithDeceasedAndIhtRefDetails = buildRegistrationDetails copy (
    deceasedDetails = Some(buildDeceasedDetails), ihtReference = Some("AbC123"))

  def createMockToGetCitizenDetails(connector: CitizenDetailsConnector, person: CidPerson) =
    when(connector.getCitizenDetails(any())(any())).thenReturn(Future.successful(person))

  def createMockToThrowExceptionWhenGettingCitizenDetails(connector: CitizenDetailsConnector) =
    when(connector.getCitizenDetails(any())(any())).thenThrow(new RuntimeException)

  /**
    * Creates mock to get the RegistrationDetails from the cache using CachingConnector
    */
  def createMockToGetRegDetailsFromCache(cachingConnector: CachingConnector,
                                      regDetails: Option[RegistrationDetails] = Some(buildRegistrationDetails))  = {
    when(cachingConnector.getRegistrationDetails(any(), any())).thenReturn(Future.successful(regDetails))
  }

  /**
    * Creates Mock To get Existing RegistrationDetails using CachingConnector
    */
  def createMockToGetExistingRegDetailsFromCache(cachingConnector: CachingConnector,
                                      regDetails: RegistrationDetails = buildRegistrationDetailsWithDeceasedAndIhtRefDetails) = {
    when(cachingConnector.getExistingRegistrationDetails(any(), any()))
      .thenReturn(regDetails)
  }

  /**
    * Creates Mock to store RegistrationDetails in Cache using CachingConnector
    */
  def createMockToStoreRegDetailsInCache(cachingConnector: CachingConnector,
                                         regDetails: Option[RegistrationDetails] = Some(buildRegistrationDetails)) = {
    when(cachingConnector.storeRegistrationDetails(any())(any(), any()))
      .thenReturn(Future.successful(regDetails))
  }

  /**
    * Creates Mock to store RegistrationDetails in Cache using CachingConnector
    */
  def createMockToStoreRegDetailsInCacheWithFailure(cachingConnector: CachingConnector,
                                         regDetails: Option[RegistrationDetails] = Some(buildRegistrationDetails)) = {
    when(cachingConnector.storeRegistrationDetails(any())(any(), any()))
      .thenReturn(Future.successful(None))
  }

  /**
    * Creates Mock To get Temp ApplicationDetails using CachingConnector
    */
  def createMockToGetTempApplicationDetailsFromCache(cachingConnector: CachingConnector,
                                        appDetails: Option[ApplicationDetails] = Some(buildApplicationDetails)) = {
    when(cachingConnector.getApplicationDetailsTemp()(any(),any()))
      .thenReturn(Future.successful(appDetails))
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
    * Creates Mock To store ApplicationDetails using CachingConnector
    */
  def createMockToStoreApplicationDetailsInCache(cachingConnector: CachingConnector,
                                          appDetails: Option[ApplicationDetails] = Some(buildApplicationDetails)) = {
    when(cachingConnector.storeApplicationDetails(any())(any(), any()))
      .thenReturn(Future.successful(appDetails))
    }

  /**
    * Creates Mock To store Temp ApplicationDetails using CachingConnector
    */

  def createMockToStoreTempApplicationDetailsInCache(cachingConnector: CachingConnector,
                                           appDetails: Option[ApplicationDetails] = Some(buildApplicationDetails))  = {
    when(cachingConnector.storeApplicationDetailsTemp(any())(any(), any()))
      .thenReturn(Future.successful(appDetails))
  }

  /**
    * Creates Mock To save ApplicationDetails using IhtConnector
    */
  def createMockToSaveApplicationDetails(ihtConnector: IhtConnector,
                                        appDetails: Option[ApplicationDetails] = Some(buildApplicationDetails)) = {
    when(ihtConnector.saveApplication(any(), any(), any())(any()))
      .thenReturn(Future.successful(appDetails))
  }

  /**
    * Creates Mock To get Existing RegistrationDetails using CachingConnector
    */
  def createMockToThrowExceptionWhileGettingExistingRegDetails(cachingConnector: CachingConnector,
                                                               exceptionMsg: String = "RunTime Exception Occured") = {
    when(cachingConnector.getExistingRegistrationDetails(any(), any()))
      .thenThrow(new RuntimeException(exceptionMsg))
  }

  /**
    * Creates mock to getCaseDetails using IhtConnector
    */

  def createMockToGetCaseDetails(ihtConnector: IhtConnector,
                regDetails: RegistrationDetails = buildRegistrationDetailsWithDeceasedAndIhtRefDetails)  = {
    when(ihtConnector.getCaseDetails(any(), any())(any()))
      .thenReturn(Future.successful(regDetails))
  }

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
    when(ihtConnector.submitApplication(any(),any(),any())(any())).thenReturn(Future.successful(returnId))
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
    when(ihtConnector.submitRegistration(any(), any())(any()))
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
    * Create mock to getSingleValueSynv from cache using CachingConnector
    */

  def createMockToGetSingleValueSyncFromCache(cachingConnector: CachingConnector,
                                              singleValueFormKey: String = any(),
                                              singleValueReturn: Option[String]=None
                                             ) ={
    when(cachingConnector.getSingleValueSync(singleValueFormKey)(any(), any()))
      .thenReturn(singleValueReturn)
  }

  /**
    * Create mock to storeSingleValueSynv from cache using CachingConnector
    */

  def createMockToStoreSingleValueSyncInCache(cachingConnector: CachingConnector,
                                              singleValueFormKey: String = any(),
                                              singleValueReturn: Option[String]=None
                                             ) ={
    when(cachingConnector.storeSingleValueSync(singleValueFormKey, any())(any(), any()))
      .thenReturn(singleValueReturn)
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
  def createMockToDeleteKeyFromCache[A](cachingConnector: CachingConnector, input: A)= {
    when(cachingConnector.delete(any())(any(), any())).thenReturn(Future.successful[A](input))
  }

  /**
    * Creates mock to doNothing when deleteSingleValue sync from cache using CachingConnector
    */
  def createMockToDoNothingWhenDeleteSingleValueSyncFromCache(cachingConnector: CachingConnector)={
    doNothing().when(cachingConnector).deleteSingleValueSync(any())(any(),any())
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
                   storeAppDetailsTempInCacheObject: Option[ApplicationDetails] = Some(defaultAppDetails) ,
                   getAppDetailsTempFromCacheObject: Option[ApplicationDetails] = Some(defaultAppDetails),
                   getAppDetails: Boolean = false,
                   getAppDetailsFromCache: Boolean = false,
                   saveAppDetails: Boolean = false,
                   getAppDetailsTempFromCache: Boolean = false,
                   storeAppDetailsInCache: Boolean = false,
                   storeAppDetailsTempInCache: Boolean = false,
                   getSingleValueFromCache: Boolean = false) = {

    createMockToGetExistingRegDetailsFromCache(cachingConnector, regDetails)

    if (getAppDetails) {

      createMockToGetApplicationDetails(ihtConnector, getUpdatedAppDetailsObject(appDetails, getAppDetailsObject))
    }

    if(getAppDetailsFromCache) {
      createMockToGetApplicationDetailsFromCache(cachingConnector, getUpdatedAppDetailsObject(appDetails, getAppDetailsFromCacheObject))
    }

    if (saveAppDetails) {
      createMockToSaveApplicationDetails(ihtConnector, getUpdatedAppDetailsObject(appDetails, saveAppDetailsObject))
    }

    if (getAppDetailsTempFromCache) {

      createMockToGetTempApplicationDetailsFromCache(cachingConnector, getUpdatedAppDetailsObject(appDetails, getAppDetailsTempFromCacheObject))
    }

    if (storeAppDetailsInCache) {
      createMockToStoreApplicationDetailsInCache(cachingConnector, getUpdatedAppDetailsObject(appDetails, storeAppDetailsInCacheObject))
    }

    if (storeAppDetailsTempInCache) {
      createMockToStoreTempApplicationDetailsInCache(cachingConnector, getUpdatedAppDetailsObject(appDetails, storeAppDetailsTempInCacheObject))
    }

    if (getSingleValueFromCache) {
      createMockToGetSingleValueSyncFromCache(cachingConnector, singleValueReturn = singleValue)
    }
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
      createMockToGetExistingRegDetailsFromCache(cachingConnector, getUpdatedRegDetailsObject(regDetails,
                                                Some(getExistingRegDetailsFromCacheObject)).get)
    }

    if(storeRegDetailsInCache) {
      createMockToStoreRegDetailsInCache(cachingConnector, getUpdatedRegDetailsObject(regDetails, storeRegDetailsInCacheObject))
    }

  }

  private def getUpdatedRegDetailsObject(regDetails: Option[RegistrationDetails],
                                         otherRegDetailsObject: Option[RegistrationDetails]) : Option[RegistrationDetails]= {
    if(!otherRegDetailsObject.isDefined) {
      otherRegDetailsObject
    } else {
      if(!(otherRegDetailsObject.get == defaultRegDetails)) {
        otherRegDetailsObject
      }else {
        regDetails
      }
    }
  }

  def createMocksForExemptionsGuidanceSingleValue(cachingConnector: CachingConnector,
                                       finalDestinationURL: String) = {
    createMockToGetSingleValueFromCache(cachingConnector,
      same(Constants.ExemptionsGuidanceContinueUrlKey), None)

    createMockToStoreSingleValueInCache(cachingConnector,
      same(Constants.ExemptionsGuidanceContinueUrlKey),
      Some(finalDestinationURL))
    createMockToDeleteKeyFromCache(cachingConnector, Constants.ExemptionsGuidanceContinueUrlKey)
  }

}

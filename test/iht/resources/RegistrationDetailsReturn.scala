/*
 * Copyright 2018 HM Revenue & Customs
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

package iht.resources

import iht.models.{ApplicantDetails, CoExecutor, DeceasedDetails}
import iht.utils.StringHelper

case class RegistrationDetailsReturn(applicantDetails: ApplicantDetails, deceasedDetails: DeceasedDetails,
                                     coExecutors: Seq[CoExecutor], acknowledgementRef: String = StringHelper.generateAcknowledgeReference) {

  val data =
    s"""
    <RegistrationDetails>
        <coExecutors>
            <firstName>${coExecutors(0).firstName}</firstName>
            <lastName>${coExecutors(0).lastName}</lastName>
            <isAddressInUk>true</isAddressInUk>
            <role>Executor</role>
            <dateOfBirth>1998-12-12</dateOfBirth>
            <ukAddress>
                <ukAddressLine1>addr1</ukAddressLine1>
                <countryCode>GB</countryCode>
                <postCode>${coExecutors(0).ukAddress.get.postCode}</postCode>
                <ukAddressLine2>addr2</ukAddressLine2>
                <ukAddressLine3>addr3</ukAddressLine3>
                <ukAddressLine4>addr4</ukAddressLine4>
            </ukAddress>
            <id>${coExecutors(0).id.getOrElse("")}</id>
            <contactDetails>
                <phoneNo>${coExecutors(0).contactDetails.phoneNo}</phoneNo>
            </contactDetails>
            <nino>${coExecutors(0).nino}</nino>
        </coExecutors>
        <coExecutors>
            <firstName>${coExecutors(1).firstName}</firstName>
            <lastName>${coExecutors(1).lastName}</lastName>
            <isAddressInUk>true</isAddressInUk>
            <role>Executor</role>
            <dateOfBirth>1998-12-12</dateOfBirth>
            <ukAddress>
                <ukAddressLine1>addr1</ukAddressLine1>
                <countryCode>GB</countryCode>
                <postCode>${coExecutors(1).ukAddress.get.postCode}</postCode>
                <ukAddressLine2>addr2</ukAddressLine2>
                <ukAddressLine3>addr3</ukAddressLine3>
                <ukAddressLine4>addr4</ukAddressLine4>
            </ukAddress>
            <id>${coExecutors(1).id.getOrElse("")}</id>
            <contactDetails>
                <phoneNo>${coExecutors(1).contactDetails.phoneNo}</phoneNo>
            </contactDetails>
            <nino>${coExecutors(1).nino}</nino>
        </coExecutors>
        <coExecutors>
            <firstName>${coExecutors(2).firstName}</firstName>
            <lastName>${coExecutors(2).lastName}</lastName>
            <isAddressInUk>true</isAddressInUk>
            <role>Executor</role>
            <dateOfBirth>1998-12-12</dateOfBirth>
            <ukAddress>
                <ukAddressLine1>addr1</ukAddressLine1>
                <countryCode>GB</countryCode>
                <postCode>${coExecutors(2).ukAddress.get.postCode}</postCode>
                <ukAddressLine2>addr2</ukAddressLine2>
                <ukAddressLine3>addr3</ukAddressLine3>
                <ukAddressLine4>addr4</ukAddressLine4>
            </ukAddress>
            <id>${coExecutors(2).id.getOrElse("")}</id>
            <contactDetails>
                <phoneNo>${coExecutors(2).contactDetails.phoneNo}</phoneNo>
            </contactDetails>
            <nino>${coExecutors(2).nino}</nino>
        </coExecutors>
        <deceasedDateOfDeath>
            <dateOfDeath>2011-12-12</dateOfDeath>
        </deceasedDateOfDeath>
        <applicantDetails>
            <firstName>${applicantDetails.firstName.get}</firstName>
            <lastName>${applicantDetails.lastName.get}</lastName>
            <country>England or Wales</country>
            <role>Lead Executor</role>
            <middleName/>
            <dateOfBirth>1998-12-12</dateOfBirth>
            <ukAddress>
                <ukAddressLine1>addr1</ukAddressLine1>
                <countryCode>GB</countryCode>
                <postCode>${applicantDetails.ukAddress.get.postCode}</postCode>
                <ukAddressLine2>addr2</ukAddressLine2>
                <ukAddressLine3>addr3</ukAddressLine3>
                <ukAddressLine4>addr4</ukAddressLine4>
            </ukAddress>
            <doesLiveInUK>true</doesLiveInUK>
            <isApplyingForProbate>true</isApplyingForProbate>
            <phoneNo>${applicantDetails.phoneNo.get}</phoneNo>
            <nino>${applicantDetails.nino.get}</nino>
        </applicantDetails>
        <acknowledgmentReference>${acknowledgementRef}</acknowledgmentReference>
        <deceasedDetails>
            <firstName>${deceasedDetails.firstName.get}</firstName>
            <lastName>${deceasedDetails.lastName.get}</lastName>
            <isAddressInUK>true</isAddressInUK>
            <domicile>England or Wales</domicile>
            <middleName/>
            <ukAddress>
                <ukAddressLine1>addr1</ukAddressLine1>
                <countryCode>GB</countryCode>
                <postCode>${deceasedDetails.ukAddress.get.postCode}</postCode>
                <ukAddressLine2>addr2</ukAddressLine2>
                <ukAddressLine3>addr3</ukAddressLine3>
                <ukAddressLine4>addr4</ukAddressLine4>
            </ukAddress>
            <dateOfBirth>1998-12-12</dateOfBirth>
            <maritalStatus>Single</maritalStatus>
            <nino>${deceasedDetails.nino.get}</nino>
        </deceasedDetails>
        <ihtReference>ABC</ihtReference>
        <status>Awaiting Return</status>
    </RegistrationDetails>
    """.stripMargin
}

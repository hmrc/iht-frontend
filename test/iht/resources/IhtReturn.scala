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

package iht.resources

case class IhtReturn(survivingSpouseFirstName: String, survivingSpouseSurname: String, survivingSpousePostCode: String,
                     survivingSpouseNino: String, deceasedSpouseFirstName: String, deceasedSpouseSurname: String,
                     deceasedSpousePostCode: String, deceasedSpouseNino: String) {

  val data =
    s"""
     <IHTReturn>
       <submitter>
         <submitterRole>Lead Executor</submitterRole>
       </submitter>
        <deceased>
            <survivingSpouse>
                <firstName>$survivingSpouseFirstName</firstName>
                <lastName>$survivingSpouseSurname</lastName>
                <dateOfMarriage>2000-06-27</dateOfMarriage>
                <domicile>England or Wales</domicile>
                <dateOfBirth>2011-11-12</dateOfBirth>
                <title>Mrs</title>
                <mainAddress>
                    <countryCode>GB</countryCode>
                    <postalCode>$survivingSpousePostCode</postalCode>
                    <addressLine1>addr1</addressLine1>
                    <addressLine2>addr2</addressLine2>
                </mainAddress>
                <nino>$survivingSpouseNino</nino>
            </survivingSpouse>
            <transferOfNilRateBand>
                <deceasedSpouses>
                    <spousesEstate>
                        <agriculturalOrBusinessRelief>true</agriculturalOrBusinessRelief>
                        <giftsWithReservation>false</giftsWithReservation>
                        <jointAssetsPassingToOther>true</jointAssetsPassingToOther>
                        <whollyExempt>false</whollyExempt>
                        <unusedNilRateBand>100</unusedNilRateBand>
                        <domiciledInUk>true</domiciledInUk>
                        <otherGifts>false</otherGifts>
                        <benefitFromTrust>true</benefitFromTrust>
                    </spousesEstate>
                    <spouse>
                        <firstName>$deceasedSpouseFirstName</firstName>
                        <lastName>$deceasedSpouseSurname</lastName>
                        <dateOfDeath>2010-10-12</dateOfDeath>
                        <dateOfMarriage>2008-12-13</dateOfMarriage>
                        <dateOfBirth>1670-12-01</dateOfBirth>
                        <title>Ms</title>
                        <mainAddress>
                            <countryCode>GB</countryCode>
                            <postalCode>$deceasedSpousePostCode</postalCode>
                            <addressLine1>addr1</addressLine1>
                            <addressLine2>addr2</addressLine2>
                        </mainAddress>
                        <nino>$deceasedSpouseNino</nino>
                    </spouse>
                </deceasedSpouses>
                <totalNilRateBandTransferred>100</totalNilRateBandTransferred>
            </transferOfNilRateBand>
        </deceased>
       <trusts>
         <trustAssets>
           <assetCode>9097</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>17</assetTotalValue>
           <howheld>Standard</howheld>
           <assetDescription>Rolled up trust assets</assetDescription>
         </trustAssets>
         <trustName>Deceased Trust</trustName>
       </trusts>
       <acknowledgmentReference>111222333444</acknowledgmentReference>
       <freeEstate>
         <estateAssets>
           <yearsLeftOntenancyAgreement>0</yearsLeftOntenancyAgreement>
           <assetCode>0017</assetCode>
           <yearsLeftOnLease>0</yearsLeftOnLease>
           <propertyAddress>
             <address>
               <countryCode>GB</countryCode>
               <postalCode>AA1 1AA</postalCode>
               <addressLine1>addr1</addressLine1>
               <addressLine2>addr2</addressLine2>
             </address>
           </propertyAddress>
           <assetID>null</assetID>
           <liabilities>
             <liabilityOwner/>
             <liabilityAmount>150</liabilityAmount>
             <liabilityType>Mortgage</liabilityType>
           </liabilities>
           <tenancyType>Vacant Possession</tenancyType>
           <assetTotalValue>200</assetTotalValue>
           <howheld>Joint - Beneficial Joint Tenants</howheld>
           <assetDescription>Other residential property</assetDescription>
           <tenure>Leasehold</tenure>
         </estateAssets>
         <estateAssets>
           <assetCode>9001</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>1</assetTotalValue>
           <howheld>Standard</howheld>
           <assetDescription>
             Rolled up bank and building society accounts
           </assetDescription>
         </estateAssets>
         <estateAssets>
           <assetCode>9015</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>19</assetTotalValue>
           <howheld>Standard</howheld>
           <assetDescription>Rolled up other assets</assetDescription>
         </estateAssets>
         <estateAssets>
           <assetCode>9006</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>12</assetTotalValue>
           <howheld>Standard</howheld>
           <assetDescription>Rolled up life assurance policies</assetDescription>
         </estateAssets>
         <estateAssets>
           <assetCode>9099</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>16</assetTotalValue>
           <howheld>Nominated</howheld>
           <assetDescription>Rolled up nominated assets</assetDescription>
         </estateAssets>
         <estateAssets>
           <assetCode>9004</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>10</assetTotalValue>
           <howheld>Joint - Beneficial Joint Tenants</howheld>
           <assetDescription>Rolled up household and personal goods</assetDescription>
         </estateAssets>
         <estateAssets>
           <yearsLeftOntenancyAgreement>0</yearsLeftOntenancyAgreement>
           <assetCode>0018</assetCode>
           <yearsLeftOnLease>0</yearsLeftOnLease>
           <propertyAddress>
             <address>
               <countryCode>GB</countryCode>
               <postalCode>AA1 1AA</postalCode>
               <addressLine1>addr1</addressLine1>
               <addressLine2>addr2</addressLine2>
             </address>
           </propertyAddress>
           <assetID>null</assetID>
           <tenancyType>Vacant Possession</tenancyType>
           <assetTotalValue>300</assetTotalValue>
           <howheld>Joint - Tenants In Common</howheld>
           <assetDescription>Other land and buildings</assetDescription>
           <tenure>Leasehold</tenure>
         </estateAssets>
         <estateAssets>
           <assetCode>9006</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>13</assetTotalValue>
           <howheld>Joint - Beneficial Joint Tenants</howheld>
           <assetDescription>Rolled up life assurance policies</assetDescription>
         </estateAssets>
         <estateAssets>
           <assetCode>9004</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>8</assetTotalValue>
           <howheld>Standard</howheld>
           <assetDescription>Rolled up household and personal goods</assetDescription>
         </estateAssets>
         <estateAssets>
           <assetCode>9008</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>10</assetTotalValue>
           <howheld>Standard</howheld>
           <assetDescription>Rolled up quoted stocks and shares</assetDescription>
         </estateAssets>
         <estateAssets>
           <assetCode>9005</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>7</assetTotalValue>
           <howheld>Standard</howheld>
           <assetDescription>Rolled up pensions</assetDescription>
         </estateAssets>
         <estateAssets>
           <assetCode>9001</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>2</assetTotalValue>
           <howheld>Joint - Beneficial Joint Tenants</howheld>
           <assetDescription>
             Rolled up bank and building society accounts
           </assetDescription>
         </estateAssets>
         <estateAssets>
           <assetCode>9021</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>14</assetTotalValue>
           <howheld>Standard</howheld>
           <assetDescription>Rolled up business assets</assetDescription>
         </estateAssets>
         <estateAssets>
           <assetCode>9098</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>18</assetTotalValue>
           <howheld>Foreign</howheld>
           <assetDescription>Rolled up foreign assets</assetDescription>
         </estateAssets>
         <estateAssets>
           <assetCode>9013</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>15</assetTotalValue>
           <howheld>Standard</howheld>
           <assetDescription>Rolled up money owed to deceased</assetDescription>
         </estateAssets>
         <estateAssets>
           <yearsLeftOntenancyAgreement>0</yearsLeftOntenancyAgreement>
           <assetCode>0016</assetCode>
           <yearsLeftOnLease>0</yearsLeftOnLease>
           <propertyAddress>
             <address>
               <countryCode>GB</countryCode>
               <postalCode>AA1 1AA</postalCode>
               <addressLine1>addr1</addressLine1>
               <addressLine2>addr2</addressLine2>
             </address>
           </propertyAddress>
           <assetID>null</assetID>
           <liabilities>
             <liabilityOwner/>
             <liabilityAmount>80</liabilityAmount>
             <liabilityType>Mortgage</liabilityType>
           </liabilities>
           <tenancyType>Vacant Possession</tenancyType>
           <assetTotalValue>100</assetTotalValue>
           <howheld>Standard</howheld>
           <assetDescription>Deceased's residence</assetDescription>
           <tenure>Freehold</tenure>
         </estateAssets>
         <estateAssets>
           <assetCode>9010</assetCode>
           <assetID>null</assetID>
           <assetTotalValue>9</assetTotalValue>
           <howheld>Standard</howheld>
           <assetDescription>Rolled up unlisted stocks and shares</assetDescription>
         </estateAssets>
         <estateLiabilities>
           <liabilityOwner/>
           <liabilityAmount>20</liabilityAmount>
           <liabilityType>Funeral Expenses</liabilityType>
         </estateLiabilities>
         <estateLiabilities>
           <liabilityOwner/>
           <liabilityAmount>90</liabilityAmount>
           <liabilityType>Other</liabilityType>
         </estateLiabilities>
         <estateExemptions>
           <exemptionType>Charity</exemptionType>
           <overrideValue>27</overrideValue>
         </estateExemptions>
         <estateExemptions>
           <exemptionType>Charity</exemptionType>
           <overrideValue>28</overrideValue>
         </estateExemptions>
         <estateExemptions>
           <exemptionType>GNCP</exemptionType>
           <overrideValue>30</overrideValue>
         </estateExemptions>
         <estateExemptions>
           <exemptionType>GNCP</exemptionType>
           <overrideValue>31</overrideValue>
         </estateExemptions>
         <estateExemptions>
           <exemptionType>Spouse</exemptionType>
           <overrideValue>25</overrideValue>
         </estateExemptions>
         <estateExemptions>
           <exemptionType>Charity</exemptionType>
           <overrideValue>28</overrideValue>
         </estateExemptions>
       </freeEstate>
       <declaration>
         <coExecutorsAccepted>true</coExecutorsAccepted>
         <declarationDate>2016-06-13</declarationDate>
         <reasonForBeingBelowLimit>Excepted Estate</reasonForBeingBelowLimit>
         <declarationAccepted>true</declarationAccepted>
       </declaration>
         <gifts>
            <array>
              <assetCode>9095</assetCode>
              <valueRetained>0</valueRetained>
              <assetID>null</assetID>
              <percentageSharePrevOwned>100</percentageSharePrevOwned>
              <percentageRetained>0</percentageRetained>
              <assetTotalValue>7000</assetTotalValue>
              <howheld>Standard</howheld>
              <assetDescription>Rolled up gifts</assetDescription>
              <lossToEstate>7000</lossToEstate>
              <valuePrevOwned>7000</valuePrevOwned>
              <dateOfGift>2011-04-05</dateOfGift>
            </array>
            <array>
              <assetCode>9095</assetCode>
              <valueRetained>0</valueRetained>
              <assetID>null</assetID>
              <percentageSharePrevOwned>100</percentageSharePrevOwned>
              <percentageRetained>0</percentageRetained>
              <assetTotalValue>6000</assetTotalValue>
              <howheld>Standard</howheld>
              <assetDescription>Rolled up gifts</assetDescription>
              <lossToEstate>6000</lossToEstate>
              <valuePrevOwned>6000</valuePrevOwned>
              <dateOfGift>2010-04-05</dateOfGift>
            </array>
            <array>
              <assetCode>9095</assetCode>
              <valueRetained>0</valueRetained>
              <assetID>null</assetID>
              <percentageSharePrevOwned>100</percentageSharePrevOwned>
              <percentageRetained>0</percentageRetained>
              <assetTotalValue>5000</assetTotalValue>
              <howheld>Standard</howheld>
              <assetDescription>Rolled up gifts</assetDescription>
              <lossToEstate>5000</lossToEstate>
              <valuePrevOwned>5000</valuePrevOwned>
              <dateOfGift>2009-04-05</dateOfGift>
            </array>
            <array>
              <assetCode>9095</assetCode>
              <valueRetained>0</valueRetained>
              <assetID>null</assetID>
              <percentageSharePrevOwned>100</percentageSharePrevOwned>
              <percentageRetained>0</percentageRetained>
              <assetTotalValue>4000</assetTotalValue>
              <howheld>Standard</howheld>
              <assetDescription>Rolled up gifts</assetDescription>
              <lossToEstate>4000</lossToEstate>
              <valuePrevOwned>4000</valuePrevOwned>
              <dateOfGift>2008-04-05</dateOfGift>
            </array>
            <array>
              <assetCode>9095</assetCode>
              <valueRetained>0</valueRetained>
              <assetID>null</assetID>
              <percentageSharePrevOwned>100</percentageSharePrevOwned>
              <percentageRetained>0</percentageRetained>
              <assetTotalValue>3000</assetTotalValue>
              <howheld>Standard</howheld>
              <assetDescription>Rolled up gifts</assetDescription>
              <lossToEstate>3000</lossToEstate>
              <valuePrevOwned>3000</valuePrevOwned>
              <dateOfGift>2007-04-05</dateOfGift>
            </array>
            <array>
              <assetCode>9095</assetCode>
              <valueRetained>0</valueRetained>
              <assetID>null</assetID>
              <percentageSharePrevOwned>100</percentageSharePrevOwned>
              <percentageRetained>0</percentageRetained>
              <assetTotalValue>1800</assetTotalValue>
              <howheld>Standard</howheld>
              <assetDescription>Rolled up gifts minus exemption of £200</assetDescription>
              <lossToEstate>1800</lossToEstate>
              <valuePrevOwned>2000</valuePrevOwned>
              <dateOfGift>2006-04-05</dateOfGift>
            </array>
            <array>
              <assetCode>9095</assetCode>
              <valueRetained>0</valueRetained>
              <assetID>null</assetID>
              <percentageSharePrevOwned>100</percentageSharePrevOwned>
              <percentageRetained>0</percentageRetained>
              <assetTotalValue>1000</assetTotalValue>
              <howheld>Standard</howheld>
              <assetDescription>Rolled up gifts</assetDescription>
              <lossToEstate>1000</lossToEstate>
              <valuePrevOwned>1000</valuePrevOwned>
              <dateOfGift>2005-04-05</dateOfGift>
            </array>
          </gifts>
     </IHTReturn>"""
    .stripMargin
}

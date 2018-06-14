<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<#escape x as x?xml>

    <fo:table table-layout="fixed" width="100%" space-before="0.2in">
	<#--  party name, fund type, comments, amount-->

    <fo:table-column column-width="60mm"/>
    <fo:table-column column-width="80mm"/>
    <fo:table-column column-width="40mm"/>

    
    <fo:table-header height="14px">
      <fo:table-row border-bottom-style="solid" border-bottom-width="thin" border-bottom-color="black">
        <fo:table-cell>
          <fo:block font-weight="bold">${uiLabelMap.ContributionPartyName}</fo:block>
        </fo:table-cell>
        <fo:table-cell>
          <fo:block font-weight="bold">${uiLabelMap.ContributionFund}</fo:block>
        </fo:table-cell>
        <#--fo:table-cell>
          <fo:block font-weight="bold" text-align="right">${uiLabelMap.CommonComments}</fo:block>
        </fo:table-cell-->
        <fo:table-cell>
          <fo:block font-weight="bold" text-align="right">${uiLabelMap.CommonAmount}</fo:block>
        </fo:table-cell>
      </fo:table-row>
    </fo:table-header>


    <fo:table-body font-size="10pt">
        <#-- if the item has a description, then use its description.  Otherwise, use the description of the invoiceItemType -->
        <#list contributionItems as contributionItem>
            
            <#if contributionItem.partyId?has_content>
            	<#assign party = contributionItem.getRelatedOne("Party",false)?if_exists>
	            <#assign partyName = Static["org.ofbiz.party.party.PartyHelper"].getPartyName(party)>
	            <#assign finAccount = contributionItem.getRelatedOne("FinAccount",false)?if_exists>
	            <#assign fund = finAccount.getRelatedOne("Fund",false)?if_exists>
            </#if>

            <#if contributionItem.partyId?has_content & contributionItem.statusId != "FINACT_TRNS_CANCELED">
                <fo:table-row height="14px" space-start=".15in">
                    <fo:table-cell>
                        <fo:block text-align="left">${partyName?if_exists}</fo:block>
                    </fo:table-cell>
                    <fo:table-cell border-top-style="solid" border-top-width="thin" border-top-color="black">
                        <fo:block text-align="left">${fund.fundName?if_exists}</fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="right">
                        <fo:block> <#if contributionItem.amount?exists><@ofbizCurrency amount=contributionItem.amount?if_exists isoCode=finAccount.currencyUomId?if_exists/></#if> </fo:block>
                    </fo:table-cell>
                    <#--fo:table-cell border-top-style="solid" border-top-width="thin" border-top-color="black">
                        <fo:block text-align="left">${contributionItem.comments?if_exists}</fo:block>
                    </fo:table-cell-->

                </fo:table-row>
                
           </#if>
        </#list>
        
        <#-- the grand total -->
        <fo:table-row>
           <fo:table-cell number-columns-spanned="2">
              <fo:block font-weight="bold">${uiLabelMap.AccountingTotalCapital}</fo:block>
           </fo:table-cell>
           <fo:table-cell text-align="right" border-top-style="solid" border-top-width="thin" border-top-color="black">
              <fo:block  font-weight="bold"><@ofbizCurrency amount=contribution.totalAmount/></fo:block>
           </fo:table-cell>
        </fo:table-row>

        <#-- blank line -->
        <fo:table-row height="7px">
            <fo:table-cell number-columns-spanned="3"><fo:block><#-- blank line --></fo:block></fo:table-cell>
        </fo:table-row>

    </fo:table-body>
 </fo:table>

</#escape>

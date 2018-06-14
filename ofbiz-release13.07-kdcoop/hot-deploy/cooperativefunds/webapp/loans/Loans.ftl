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

<div id="loans" class="screenlet">
  <div class="screenlet-title-bar">
	<ul>
	  <li class="h3">${uiLabelMap.Loans}</li>
	  <#if security.hasEntityPermission("PARTYMGR", "_NOTE", session)>
	    <li><a href="<@ofbizUrl>newLoanRequest?partyId=${partyId}</@ofbizUrl>">${uiLabelMap.CommonCreateNew}</a></li>
	  </#if>
	</ul>
	<br class="clear" />
  </div>
  <div class="screenlet-body">
    <#if loans?has_content>
      <table class="basic-table" cellspacing="0">
        <tr class="header-row">
          <td>${uiLabelMap.loanId}</td>
          <td>${uiLabelMap.initialPrincipalAmount}</td>
          <td>${uiLabelMap.noOfInstallments}</td>
          <td>${uiLabelMap.statusId}</td>
        </tr>
        <#list loans as loan>
        	<#assign statusItem = delegator.findOne("StatusItem", {"statusId" : loan.statusId}, true) />
          <tr>
            <td class="button-col">
              <a href="/catalog/control/FindProductStoreRoles?partyId=${loan.partyId}&amp;productStoreId=${productStore.productStoreId}">${productStore.storeName?default("${uiLabelMap.ProductNoDescription}")} (${productStore.productStoreId})</a>
            </td>
            <td class="button-col">
              <a href="/catalog/control/FindProductStoreRoles?partyId=${loan.initialPrincipalAmount}&amp;productStoreId=${productStore.productStoreId}">${productStore.storeName?default("${uiLabelMap.ProductNoDescription}")} (${productStore.productStoreId})</a>
            </td>
            <td class="button-col">
              <a href="/catalog/control/FindProductStoreRoles?partyId=${loan.noOfInstallments}&amp;productStoreId=${productStore.productStoreId}">${productStore.storeName?default("${uiLabelMap.ProductNoDescription}")} (${productStore.productStoreId})</a>
            </td>
            <td class="button-col">
              <a href="/catalog/control/FindProductStoreRoles?partyId=${statusItem.description}&amp;productStoreId=${productStore.productStoreId}">${productStore.storeName?default("${uiLabelMap.ProductNoDescription}")} (${productStore.productStoreId})</a>
            </td>
          </tr>
        </#list>
      </table>
    <#else>
      ${uiLabelMap.LoansNoLoansFoundForThisParty}
    </#if>
  </div>
</div>
 <#if userLogin?has_content>
    <ul class="nav nav-tabs ">
        <li class="nav-item">
            <a class="nav-link"  href="<@ofbizUrl>requestAccountStatement</@ofbizUrl>">Account Statement</a>
        </li>
        <li class="nav-item">
            <a class="nav-link"  href="<@ofbizUrl>main</@ofbizUrl>">Loan Request</a>
        </li>
        <li class="nav-item">
            <a class="nav-link"  href="<@ofbizUrl>main</@ofbizUrl>">Loan Statement</a>
        </li>
    </ul> 
</#if>
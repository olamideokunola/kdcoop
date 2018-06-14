 <p>
 <div class="row">
  <div class="col-sm-3"></div>
  <div class="col-sm-6">
    <div class="card">
    <div class="card-header">Account Statement Request</div>
    <div class="card-body">
        <form method="post" action="<@ofbizUrl>requestAccountStatement</@ofbizUrl>">
          <div class="form-group">
            <label for="finAccountId">Fund:</label>
            <select name="finAccountId">
              <option value="0"></option>
              <#if finAccountList?has_content>
                <#list finAccountList as finAccount>
                  <option value="${finAccount.finAccountId}">${finAccount.finAccountName}</option>
                </#list>
              </#if>
            </select> 

            <!--input type="text" class="form-control" id="fund" name="fund"-->
          </div>
          <div class="form-group">
            <label for="fromDate">From:</label>
            <input type="date" class="form-control" id="fromDate" name="fromDate" >
          </div>
          <div class="form-group">
            <label for="toDate">To:</label>
            <input type="date" class="form-control" id="toDate" name="toDate" >
          </div>
          <button type="submit" class="btn btn-primary">Submit</button>
        </form> 
    </div> 
  </div>
  </div>
  <div class="col-sm-3"></div>
</div> 
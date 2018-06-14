
<p></p> 
<#if finAccountTransList?has_content>
<div class="container">
  <h2>Account Statement</h2>
  <p></p>            
  <table class="table table-hover">
    <thead>
      <tr>
        <th>entryDate</th>
        <th>comments</th>
        <th>amount</th>
      </tr>
    </thead>
    <tbody>
    	<#list finAccountTransList as finAccountTrans>
      		<tr>
	      
	      	<td>${finAccountTrans.entryDate}</td>
        	<td>${finAccountTrans.comments}</td>
        	<td>${finAccountTrans.amount}</td>
	 
      		</tr>
      	</#list>
    </tbody>
  </table>
</div>
<p>Grand Total: ${grandTotal}</p>
</#if>
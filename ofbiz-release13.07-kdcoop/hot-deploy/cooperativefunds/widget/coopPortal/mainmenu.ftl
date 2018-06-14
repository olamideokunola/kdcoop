	

	<nav class="navbar navbar-expand-sm navbar-dark fixed-top">
		
		  <div class="navbar-header">
			  <a class="navbar-brand" href="#">
			  	<img src="<@ofbizContentUrl>/images/teamwork.png</@ofbizContentUrl>" alt="Logo">
					kdCoop
	          </a>
          </div>
          
          <!-- Toggler/collapsibe Button -->
		  <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#collapsibleNavbar">
		    <span class="navbar-toggler-icon"></span>
		  </button>
		  
		  <!-- Links -->
			<div class="collapse navbar-collapse" id="collapsibleNavbar">
			  <ul class="navbar-nav navbar-right">
	            <li class="nav-item">
				  <a class="nav-link" href="<@ofbizUrl>main</@ofbizUrl>">Home</a>
				</li>
				<li class="nav-item">
				  <a class="nav-link" href="<@ofbizUrl>financials</@ofbizUrl>">Financials</a>
				</li>
				<li class="nav-item">
				  <a class="nav-link" href="<@ofbizUrl>aboutus</@ofbizUrl>">About Us</a>
				</li>
				<li class="nav-item">
				  <a class="nav-link" href="<@ofbizUrl>contact</@ofbizUrl>">Contact</a>
				</li>
	      		<#if userLogin?has_content>
						<li class="nav-item">
							<a class="nav-link" href="<@ofbizUrl>logout</@ofbizUrl>"> <span class="glyphicon glyphicon-log-in"></span>Logout</a>
						</li>
				<#else>
						<li class="nav-item">
							<a class="nav-link" href="<@ofbizUrl>login</@ofbizUrl>"> <span class="glyphicon glyphicon-log-in"></span>Login</a>
						</li>
				</#if>
			  </ul>
			</div>
		
	</nav>
		
		<div class="container-fluid  text-center">
		  <h3>Cooperative Society - Kaduna Brewery</h1>
		  <p><em>helping our people achieve financial stability ...</em></p>
		</div>
		
<style>
.jumbotron {
    background-color: #003366; /* Orange */
    color: #fff;
    padding: 100px 25px;
}

.container-fluid {
   	background-color: #EFF4F9; /* Orange */
    color: #2d2d30;
    padding: 100px 0px 25px;
}

/* Add a dark background color with a little bit see-through */
.navbar {
    margin-bottom: 0;
    background-color: #003366;
    border: 0;
    font-size: 14px !important;
    letter-spacing: 2px;
    opacity:1;
}

.navbar-brand {
	color: #E4F1F6 !important;
}


/* The active link */
.navbar-nav li.active a {
    color: #fff !important;
    background-color:#29292c !important;
}
</style>		      

		
		 	
		
		
		

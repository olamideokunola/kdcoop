 <div class="row">
  <div class="col-sm-4"></div>
  <div class="col-sm-4">
    <div class="card">
    <div class="card-header">Member Login</div>
    <div class="card-body">
        <form method="post" action="<@ofbizUrl>login</@ofbizUrl>">
            <div class="form-group">
                <label for="email">Username:</label>
                <input type="text" class="form-control" id="usr" name="USERNAME" >
            </div>
            <div class="form-group">
                <label for="pwd">Password:</label>
                <input type="password" class="form-control" id="pwd" name="PASSWORD" >
            </div>
            <div class="form-group form-check">
                <label class="form-check-label">
                <input class="form-check-input" type="checkbox"> Remember me
                </label>
            </div>
            <button type="submit" class="btn btn-primary">Login</button>
        </form> 
    </div> 
  </div>
  </div>
  <div class="col-sm-4"></div>
</div> 
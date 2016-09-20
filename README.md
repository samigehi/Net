# Net

* Simple light-weight HTTP client based on HttpUrlConnection
* easy to use RESTful web-service communication
* run request on background thread and notify to UI thread when request is completed
  
# Why use this 
  * no dependancy, built on top of URLConnection native.
  * light-weight only single file 10kb add in project and start networking task,
  * In recent version of android (Marshmallow) apache HttpClient has been removed, it made request difficult, other networking libraries obsolete 
  * make request, download any type of file, upload file, image loading, json request, etc.
  
  # GET
 
 simple GET request 
 * create a new request with defined url
 
            Net<JsonArray> request = Net.create(this, "http://127.0.0.1/Login/user/auth");
            request.execute(new Callback<JsonArray>() {
            @Override
            public void onComplete(JsonArray result, Exception e) {
                if (result != null) {
                    // something with result here
                } else {
                    // print error in logcat
                    e.printStackTrace();
                }
            }
           });
    
    
  * or u can define URL and just pass controler name
  * append base url with controler
  
          Net.setBaseURL("http://127.0.0.1");
          
            Net<JsonArray> request = Net.with(this, "Login/user/auth");
            request.execute(new Callback<JsonArray>() {
            @Override
            public void onComplete(JsonArray result, Exception e) {
                if (result != null) {
                    // something with result here
                } else {
                    // print error in logcat
                    e.printStackTrace();
                }
            }
           });
           
           
           
  
  # POST
          
            Net<JsonArray> request = Net.with(this, "Login/user/auth").add("key", "value").add("username", "sumeet")
                .add("password", "xxxxxxxxx").execute(new Callback<JsonArray>() {
            @Override
            public void onComplete(JsonArray result, Exception e) {
                if (result != null) {
                    // something with result here
                } else {
                    // print error in logcat
                    e.printStackTrace();
                }
            }
           });
    or
    
              Net<JsonArray> request = Net.with(this, "Login/user/auth")
              request.add("key", "value");
              request.add("username", "abc");
              request.execute(new Callback<JsonArray>(....));
            
            
  
  # parameterized objects
  * respone with define parameterized objects
  * get request response as Drawable/Bitmap object or other
  
          Net<Drawable> request = Net.create(this, "https://github.com/fluidicon.png");
          request.asDrawable(true);
          request.execute(new Callback<Drawable>() {
              @Override
              public void onComplete(Drawable result, Exception e) {
                  if (result != null) {
                      image.setBackgroundDrawable(result);
                      Utils.log("Load Drawable", "size ****  width = " + result.getIntrinsicWidth() + ", height = "
                              + result.getIntrinsicHeight());
                  } else {
                      e.printStackTrace();
                  }
              }
          });
            

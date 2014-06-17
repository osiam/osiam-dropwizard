osiam-dropwizard
================

OSIAM Authentication module for Dropwizard

Usage:
Add the OSIAM Dropwizard module to your project dependencies.

In your Dropwizard main class add following to the run() method:

        OsiamDropwizard osiam =new OsiamDropwizard(configuration.getOsiam(), new RedirectUriProvider(){

            @Override
            public URI getRedirectUri(String state) {
                try {
                    return new URI("https://mydropwizardservice.net/"+state);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });
        osiam.configure(environment);

Your configuration class should have an OSIAM configuration:
    @JsonProperty
    private OsiamConfiguration osiam;

Example OSIAM section of config.yaml

osiam:
  endpoint: http://localhost:8080
  clientId: example-client
  clientSecret: secret
  clientRedirectUri: https://localhost:7443/api/osiam/oauth2


Your resource methods can now be protected like this:
 @Timed
 @UnitOfWork
 @POST
 @Path("/all")
 public void saveConfig(@RestrictedTo({"admin"}) OsiamContext oc, VidioConfig config) {
        acd.save(config);
        LOGGER.debug("Saved config: " + config);
 }

Only logged in users which are member of group "admin" can access the saveConfig method. If you require users just to be logged, annotate the OsiamContext parameter just with @RestrictedTo()

The client calling one of the protected methods must send the OAuth2 access token in the HTTP header. Here an example with JavaScript and JQuery:

	function getJSONSync(accessToken, url, callback) {
		$.ajax({
			type : 'GET',
			url : url,
			dataType : 'json',
			success : callback,
			data : {},
			async : false,
			beforeSend: function(xhr) {
			    xhr.setRequestHeader('Authorization', 'bearer '+accessToken);
			}
		});
	}

Example, how to login:


    function login(state) {
        var result = false;
        getJSONSync(null, '/api/osiam/login?state='+state, function(data){
            if(data.accessToken) {
                result = data.accessToken;
            } else if(data.redirectUri) {
                location.replace(data.redirectUri);
                result = false;
            }

        });
        return result;
    }

The method redirects to the OSIAM login page to login if user is not logged in yet. The state value is also passed to the RedirectProvider, which is called after login succeeds. If the user is logged in, the login method returns the access token.


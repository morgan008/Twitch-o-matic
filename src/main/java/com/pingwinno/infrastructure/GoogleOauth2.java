package com.pingwinno.infrastructure;

    import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfoplus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

    /**
     * Command-line sample for the Google OAuth2 API described at <a
     * href="http://code.google.com/apis/accounts/docs/OAuth2Login.html">Using OAuth 2.0 for Login
     * (Experimental)</a>.
     *
     * @author Yaniv Inbar
     */
    public class GoogleOauth2 {

        /**
         * Be sure to specify the name of your application. If the application name is {@code null} or
         * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
         */
        private static final String APPLICATION_NAME = "Twitch-o-matic_test";

        /** Directory to store user credentials. */
        private static final java.io.File DATA_STORE_DIR =
                new java.io.File(System.getProperty("./datastore"), ".store/oauth2_sample");

        /**
         * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
         * globally shared instance across your application.
         */
        private static FileDataStoreFactory dataStoreFactory;

        /** Global instance of the HTTP transport. */
        private static HttpTransport httpTransport;

        /** Global instance of the JSON factory. */
        private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

        /** OAuth 2.0 scopes. */
        private static final List<String> SCOPES = Arrays.asList(
                "https://www.googleapis.com/auth/drive");

        private static Oauth2 oauth2;
        private static GoogleClientSecrets clientSecrets;

        /** Authorizes the installed application to access user's protected data. */
        private static Credential authorize() throws Exception {
            // load client secrets
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new InputStreamReader(GoogleOauth2.class.getResourceAsStream("/client_secrets.json")));
            if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                    || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
                System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/ "
                        + "into oauth2-cmdline-sample/src/main/resources/client_secrets.json");
                System.exit(1);
            }
            // set up authorization code flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(
                    dataStoreFactory).build();
            // authorize
            return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        }

        public String startAuth() {
            Credential credential = null;
            try {
                httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
                // authorization
                credential = authorize();
                // set up global Oauth2 instance
                oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
                        APPLICATION_NAME).build();
                // run commands
                tokenInfo(credential.getAccessToken());

                // success!

            } catch (IOException e) {
                System.err.println(e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return credential.getAccessToken();
        }

        private static void tokenInfo(String accessToken) throws IOException {
            header("Validating a token");
            Tokeninfo tokeninfo = oauth2.tokeninfo().setAccessToken(accessToken).execute();
            System.out.println(tokeninfo.toPrettyString());
            if (!tokeninfo.getAudience().equals(clientSecrets.getDetails().getClientId())) {
                System.err.println("ERROR: audience does not match our client ID!");
            }
        }


        static void header(String name) {
            System.out.println();
            System.out.println("================== " + name + " ==================");
            System.out.println();
        }
    }


// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.ads.googleads.examples.basicoperations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v10.errors.GoogleAdsError;
import com.google.ads.googleads.v10.errors.GoogleAdsException;
import com.google.ads.googleads.v10.services.GoogleAdsRow;
import com.google.ads.googleads.v10.services.GoogleAdsServiceClient;
import com.google.ads.googleads.v10.services.SearchGoogleAdsStreamRequest;
import com.google.ads.googleads.v10.services.SearchGoogleAdsStreamResponse;
import com.google.api.gax.rpc.ServerStream;

/** Gets all campaigns. To add campaigns, run AddCampaigns.java. */
public class GetCampaigns {

    /**
     * Application Entrypoint.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Properties config = new Properties();
        config.load(
            new FileInputStream(
                new File("base.properties")));
        Long customerId = Long.valueOf(
            config.getProperty("api.googleads.clientCustomerId"));
        GoogleAdsClient googleAdsClient = null;
        try {
            googleAdsClient = GoogleAdsClient.newBuilder().fromPropertiesFile(new File("/src/main/java/com/google/ads/googleads/examples/basicoperations/base.properties")).build();
        } catch (FileNotFoundException fnfe) {
            System.err.printf(
                    "Failed to load GoogleAdsClient configuration from file. Exception: %s%n", fnfe);
            System.exit(1);
        } catch (IOException ioe) {
            System.err.printf("Failed to create GoogleAdsClient. Exception: %s%n", ioe);
            System.exit(1);
        }

        try {
            new GetCampaigns().runExample(googleAdsClient, customerId);
        } catch (GoogleAdsException gae) {
            // GoogleAdsException is the base class for most exceptions thrown by an API
            // request.
            // Instances of this exception have a message and a GoogleAdsFailure that
            // contains a
            // collection of GoogleAdsErrors that indicate the underlying causes of the
            // GoogleAdsException.
            System.err.printf(
                    "Request ID %s failed due to GoogleAdsException. Underlying errors:%n",
                    gae.getRequestId());
            int i = 0;
            for (GoogleAdsError googleAdsError : gae.getGoogleAdsFailure().getErrorsList()) {
                System.err.printf("  Error %d: %s%n", i++, googleAdsError);
            }
            System.exit(1);
        }
    }

    /**
     * Runs the example.
     *
     * @param googleAdsClient the Google Ads API client.
     * @param customerId      the client customer ID.
     * @throws GoogleAdsException if an API request failed with one or more service
     *                            errors.
     */
    private void runExample(GoogleAdsClient googleAdsClient, long customerId) {
        try (GoogleAdsServiceClient googleAdsServiceClient = googleAdsClient.getLatestVersion()
                .createGoogleAdsServiceClient()) {
            String query = "SELECT campaign.id, campaign.name FROM campaign ORDER BY campaign.id";
            // Constructs the SearchGoogleAdsStreamRequest.
            SearchGoogleAdsStreamRequest request = SearchGoogleAdsStreamRequest.newBuilder()
                    .setCustomerId(Long.toString(customerId))
                    .setQuery(query)
                    .build();

            // Creates and issues a search Google Ads stream request that will retrieve all
            // campaigns.
            ServerStream<SearchGoogleAdsStreamResponse> stream = googleAdsServiceClient.searchStreamCallable()
                    .call(request);

            // Iterates through and prints all of the results in the stream response.
            for (SearchGoogleAdsStreamResponse response : stream) {
                for (GoogleAdsRow googleAdsRow : response.getResultsList()) {
                    System.out.printf(
                            "Campaign with ID %d and name '%s' was found.%n",
                            googleAdsRow.getCampaign().getId(), googleAdsRow.getCampaign().getName());
                }
            }
        }
    }
}

package com.google.ads.googleads.examples.basicoperations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v10.errors.GoogleAdsError;
import com.google.ads.googleads.v10.errors.GoogleAdsException;
import com.google.ads.googleads.v10.services.GoogleAdsServiceClient;
import com.google.ads.googleads.v10.services.KeywordPlanOperation;
import com.google.ads.googleads.v10.services.MutateKeywordPlansResponse;

public class RemoveKeywordPlan {
    public static void main(String[] args) {
        String keywordPlanToBeRemoved = "customers/5546374685/keywordPlans/403975311";
        Properties config = new Properties();
        try {
            config.load(
            new FileInputStream(
                new File("base.properties")));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        Long customerId = Long.valueOf(
            config.getProperty("api.googleads.clientCustomerId"));
        GoogleAdsClient googleAdsClient = null;
        try {
            googleAdsClient = GoogleAdsClient.newBuilder().fromPropertiesFile(new File("base.properties")).build();
        } catch (FileNotFoundException fnfe) {
            System.err.printf(
                    "Failed to load GoogleAdsClient configuration from file. Exception: %s%n", fnfe);
            System.exit(1);
        } catch (IOException ioe) {
            System.err.printf("Failed to create GoogleAdsClient. Exception: %s%n", ioe);
            System.exit(1);
        }

        try (GoogleAdsServiceClient serviceClient = googleAdsClient
                .getLatestVersion()
                .createGoogleAdsServiceClient()){
            KeywordPlanOperation removeOp = KeywordPlanOperation.newBuilder()
                .setRemove(keywordPlanToBeRemoved)
                .build();
            MutateKeywordPlansResponse response =
                googleAdsClient
                    .getLatestVersion()
                    .createKeywordPlanServiceClient()
                    .mutateKeywordPlans(
                        String.valueOf(customerId),
                        Arrays.asList(removeOp));
            System.out.println(response.getResultsCount());
        } catch (GoogleAdsException gae) {
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
}

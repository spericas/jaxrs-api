/*
 * Copyright (c) 2011, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package jaxrs.examples.async;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.AsyncResponse;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Long-running asynchronous processing examples.
 *
 * @author Marek Potociar
 */
@Path("/async/longRunning")
@Produces("text/plain")
public class LongRunningAsyncOperationResource {

    @GET
    @Path("sync")
    public String basicSyncExample() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LongRunningAsyncOperationResource.class.getName()).log(Level.SEVERE, "Response processing interrupted", ex);
        }
        return "Hello async world!";
    }

    @GET
    @Path("async")
    public void asyncExample(AsyncResponse ar) {
        ar.setTimeout(15, SECONDS);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LongRunningAsyncOperationResource.class.getName()).log(Level.SEVERE, "Response processing interrupted", ex);
            }
            ar.resume("Hello async world!");
        });
    }

    @GET
    @Path("asyncSelective")
    public void selectiveSuspend(@QueryParam("query") final String query, AsyncResponse ar) {
        if (!isComplex(query)) {
            // process simple queries synchronously
            ar.resume("Simple result for " + query);
        } else {
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(LongRunningAsyncOperationResource.class.getName())
                            .log(Level.SEVERE, "Response processing interrupted", ex);
                }
                ar.resume("Complex result for " + query);
            });
        }
    }

    private boolean isComplex(String query) {
        return new Random(query.hashCode()).nextBoolean();
    }

    @GET
    @Path("asyncTimeoutOverride")
    public void overriddenTimeoutAsync(@QueryParam("timeOut") Long timeOut, @QueryParam("timeUnit") TimeUnit timeUnit,
            AsyncResponse ar) {
        if (timeOut != null && timeUnit != null) {
            ar.setTimeout(timeOut, timeUnit);
        } else {
            ar.setTimeout(15, SECONDS);
        }

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LongRunningAsyncOperationResource.class.getName())
                        .log(Level.SEVERE, "Response processing interrupted", ex);
            }
            ar.resume("Hello async world!");
        });
    }

    @GET
    @Path("asyncHandleUsage")
    public void suspendHandleUsageExample(AsyncResponse ar) {
        ar.setTimeout(15, SECONDS);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LongRunningAsyncOperationResource.class.getName())
                        .log(Level.SEVERE, "Response processing interrupted", ex);
            }
            ar.resume("Hello async world!");
        });
        ar.setTimeoutHandler(r -> Logger.getLogger(LongRunningAsyncOperationResource.class.getName())
                .log(Level.INFO, "Context resumed with a response!"));
    }
}

/*
 * Copyright (c) 2016, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.TXT file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.emp.connector;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import it.fastweb.edh.jms.JmsSink;
import it.fastweb.edh.jms.WeblogicHelper;
import org.eclipse.jetty.util.ajax.JSON;

import javax.naming.InitialContext;

import static org.cometd.bayeux.Channel.*;

/**
 * An example of using the EMP connector
 *
 * @author hal.hildebrand
 * @since API v37.0
 */
public class SFDCLogin {

    public static void main(String[] argv) throws Throwable {
        if (argv.length < 4 || argv.length > 5) {
            System.err.println("Usage: SFDCLogin url username password topic [replayFrom]");
            System.exit(1);
        }


        String JNDI_FACTORY="weblogic.jndi.WLInitialContextFactory";
        String JNDI_URL = "t3://...";
        String QUEUE = "jms/QueueName";

        //retrieve initial context
        InitialContext ic = WeblogicHelper.getInitialContext(JNDI_FACTORY,JNDI_URL);

        JmsSink sink = new JmsSink();
        sink.init(ic, QUEUE);


        Consumer<Map<String, Object>> consumer = event -> {
            String jsonEvent = JSON.toString(event);
            System.out.println(String.format("Received from salesforce:\n%s", jsonEvent));
            try {
                sink.send(jsonEvent);
                }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println(String.format("Sent to jms queue"));
        };



        //BearerTokenProvider class reauthenticates the user if the authentication becomes invalid,
        BearerTokenProvider tokenProvider = new BearerTokenProvider(() -> {
            try {
                return LoginHelper.login(new URL(argv[0]), argv[1], argv[2]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        BayeuxParameters params = tokenProvider.login();

        EmpConnector connector = new EmpConnector(params);
        LoggingListener loggingListener = new LoggingListener(true, true);

        connector.addListener(META_HANDSHAKE, loggingListener)
                .addListener(META_CONNECT, loggingListener)
                .addListener(META_DISCONNECT, loggingListener)
                .addListener(META_SUBSCRIBE, loggingListener)
                .addListener(META_UNSUBSCRIBE, loggingListener);

        connector.setBearerTokenProvider(tokenProvider);

        connector.start().get(5, TimeUnit.SECONDS);

        long replayFrom = EmpConnector.REPLAY_FROM_EARLIEST;
        if (argv.length == 5) {
            replayFrom = Long.parseLong(argv[4]);
        }
        TopicSubscription subscription;
        try {
            subscription = connector.subscribe(argv[3], replayFrom, consumer).get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            System.err.println(e.getCause().toString());
            System.exit(1);
            throw e.getCause();
        } catch (TimeoutException e) {
            System.err.println("Timed out subscribing");
            System.exit(1);
            throw e.getCause();
        }

        System.out.println(String.format("Subscribed: %s", subscription));
    }
}

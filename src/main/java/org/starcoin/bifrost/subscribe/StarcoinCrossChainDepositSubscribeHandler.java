package org.starcoin.bifrost.subscribe;

import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.starcoin.bifrost.service.StarcoinHandleEventService;
import org.web3j.protocol.websocket.WebSocketService;

import java.net.ConnectException;

public class StarcoinCrossChainDepositSubscribeHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StarcoinCrossChainDepositSubscribeHandler.class);

    private final String webSocketSeed;

    //private String network;

    private final StarcoinHandleEventService starcoinHandleEventService;

    private final String fromAddress;

    private final String crossChainDepositEventTypeTag;

    public StarcoinCrossChainDepositSubscribeHandler(String seed,
                                                     StarcoinHandleEventService starcoinHandleEventService,
                                                     String fromAddress, String crossChainDepositEventTypeTag) {
        this.webSocketSeed = seed;
        //this.network = network;
        this.starcoinHandleEventService = starcoinHandleEventService;
        this.fromAddress = fromAddress;
        this.crossChainDepositEventTypeTag = crossChainDepositEventTypeTag;
    }


    private String getWebSocketSeed() {
        String wsUrl = webSocketSeed;
        String wsPrefix = "ws://";
        if (!wsUrl.startsWith(wsPrefix)) {
            wsUrl = wsPrefix + wsUrl;
        }
        if (wsUrl.lastIndexOf(":") == wsUrl.indexOf(":")) {
            wsUrl = wsUrl + ":9870";
        }
        LOG.debug("Get WebSocket URL: " + wsUrl);
        return wsUrl;
    }

    @Override
    public void run() {
        try {
            WebSocketService service = new WebSocketService(getWebSocketSeed(), true);
            service.connect();
            StarcoinCrossChainDepositSubscriber subscriber = new StarcoinCrossChainDepositSubscriber(service, fromAddress, crossChainDepositEventTypeTag);
            Flowable<StarcoinCrossChainDepositSubscriber.EventNotification> flowableEvents = subscriber.eventNotificationFlowable();

            for (StarcoinCrossChainDepositSubscriber.EventNotification notification : flowableEvents.blockingIterable()) {
                if (notification.getParams() == null || notification.getParams().getResult() == null) {
                    continue;
                }
                StarcoinEvent event = notification.getParams().getResult();
                if (LOG.isDebugEnabled()) LOG.debug("Received event: " + event);
                starcoinHandleEventService.handle(event);
            }
        } catch (ConnectException e) {
            LOG.info("handle subscribe exception", e);
        }
    }
}

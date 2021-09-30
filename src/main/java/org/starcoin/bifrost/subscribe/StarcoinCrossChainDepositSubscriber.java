package org.starcoin.bifrost.subscribe;


import io.reactivex.Flowable;
import org.starcoin.bean.Kind;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthSubscribe;
import org.web3j.protocol.websocket.events.Notification;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StarcoinCrossChainDepositSubscriber {

    public static final Kind SUBSCRIBE_KIND_EVENTS = new Kind(1, "events");
    private final String crossChainDepositEventTypeTag;// = "0x569AB535990a17Ac9Afd1bc57Faec683::Bifrost::CrossChainDepositEvent";
    private final String fromAddress;//= "0x569AB535990a17Ac9Afd1bc57Faec683";
    private final Web3jService web3jService;

    public StarcoinCrossChainDepositSubscriber(Web3jService web3jService, String fromAddress, String crossChainDepositEventTypeTag) {
        this.web3jService = web3jService;
        this.fromAddress = fromAddress;
        this.crossChainDepositEventTypeTag = crossChainDepositEventTypeTag;
    }

    public Flowable<EventNotification> eventNotificationFlowable() {
        Map<String, Object> eventFilter = createEventFilter();
        return web3jService.subscribe(
                new Request<>(
                        "starcoin_subscribe",
                        Arrays.asList(SUBSCRIBE_KIND_EVENTS, eventFilter),
                        web3jService,
                        EthSubscribe.class),
                "starcoin_unsubscribe",
                EventNotification.class);
    }


    private Map<String, Object> createEventFilter() {
        Map<String, Object> eventFilter = new HashMap<>();
        eventFilter.put("addr", fromAddress);
        eventFilter.put("type_tags", Collections.singletonList(crossChainDepositEventTypeTag));
        eventFilter.put("decode", false);
        return eventFilter;
    }

    public static class EventNotification extends Notification<StarcoinEvent> {
        //
    }

}

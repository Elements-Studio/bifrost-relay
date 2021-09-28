package org.starcoin.bifrost.subscribe;

import io.reactivex.Flowable;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthSubscribe;
import org.web3j.protocol.websocket.events.LogNotification;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EthereumWithdrawSubscriber {

    public static final String TOPIC_CROSS_CHAIN_WITHDRAW_EVENT = "0x" + Hex.toHexString(new Keccak.Digest256()
            .digest("CrossChainWithdrawEvent(address,bytes20,address,uint256,uint8)".getBytes()));

    private final Web3jService web3jService;
    private final String logFilterAddress;

    public EthereumWithdrawSubscriber(Web3jService web3jService, String logFilterAddress) {
        this.web3jService = web3jService;
        this.logFilterAddress = logFilterAddress;
    }

    public Flowable<LogNotification> logNotificationFlowable() {
        Map<String, Object> filter = new HashMap<>();
        // object with the following (optional) fields
        //   address, either an address or an array of addresses. Only logs that are created from these addresses are returned (optional)
        //   topics, only logs which match the specified topics (optional)
        filter.put("address", logFilterAddress);
        filter.put("topics", Collections.singletonList(TOPIC_CROSS_CHAIN_WITHDRAW_EVENT));
        return web3jService.subscribe(
                new Request<>(
                        "eth_subscribe",
                        Arrays.asList("logs", filter),
                        web3jService,
                        EthSubscribe.class),
                "eth_unsubscribe",
                LogNotification.class);
    }


}

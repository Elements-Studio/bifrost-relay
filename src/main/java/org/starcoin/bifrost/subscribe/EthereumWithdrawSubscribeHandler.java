package org.starcoin.bifrost.subscribe;

import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.starcoin.bifrost.service.EthereumHandleLogService;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.protocol.websocket.events.Log;
import org.web3j.protocol.websocket.events.LogNotification;

import java.math.BigInteger;
import java.net.ConnectException;
import java.util.List;

import static org.starcoin.utils.HexUtils.hexToBigInteger;

public class EthereumWithdrawSubscribeHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(EthereumWithdrawSubscribeHandler.class);

    private final String webSocketServiceUrl;

    private final String logFilterAddress;

    private final EthereumHandleLogService ethereumHandleLogService;

    public EthereumWithdrawSubscribeHandler(String webSocketServiceUrl, String logFilterAddress,
                                            EthereumHandleLogService ethereumHandleLogService) {
        this.webSocketServiceUrl = webSocketServiceUrl;
        this.logFilterAddress = logFilterAddress;
        this.ethereumHandleLogService = ethereumHandleLogService;
    }


    private static EthereumHandleLogService.LogWrapper getLogWrapper(Log log) {
        return new EthereumHandleLogService.LogWrapper() {
            public String getAddress() {
                return log.getAddress();
            }

            public String getBlockHash() {
                return log.getBlockHash();
            }

            public String getTransactionHash() {
                return log.getTransactionHash();
            }

            public BigInteger getTransactionIndex() {
                return hexToBigInteger(log.getTransactionIndex());
            }

            public BigInteger getLogIndex() {
                return hexToBigInteger(log.getLogIndex());
            }

            public String getData() {
                return log.getData();
            }

            public List<String> getTopics() {
                return log.getTopics();
            }

            public BigInteger getBlockNumber() {
                return hexToBigInteger(log.getBlockNumber());
            }
        };
    }

    private String getWebSocketServiceUrl() {
        return this.webSocketServiceUrl;
    }

    @Override
    public void run() {
        try {
            WebSocketService service = new WebSocketService(getWebSocketServiceUrl(), false);
            service.connect();
            EthereumWithdrawSubscriber subscriber = new EthereumWithdrawSubscriber(service, logFilterAddress);
            Flowable<LogNotification> logNotificationFlowable = subscriber.logNotificationFlowable();
            for (LogNotification notification : logNotificationFlowable.blockingIterable()) {
                if (notification.getParams() == null || notification.getParams().getResult() == null) {
                    continue;
                }
                final Log log = notification.getParams().getResult();
//                if (LOG.isDebugEnabled()) {
//                    String message = "Received log"
//                            + ", blockHash: " + log.getBlockHash()
//                            + ", transactionHash: " + log.getTransactionHash()
//                            + ", logIndex: " + log.getLogIndex()
//                            + ", address: " + log.getAddress()
//                            + ", data: " + log.getData()
//                            + ", topics: " + log.getTopics();
//                    LOG.debug(message);
//                }
                // 0x000000000000000000000000000000000000000000000000000000003b9aca000000000000000000000000000000000000000000000000000000000000000001
                ethereumHandleLogService.handle(getLogWrapper(log));
            }
        } catch (ConnectException e) {
            LOG.info("handle subscribe exception", e);
        }
    }

}

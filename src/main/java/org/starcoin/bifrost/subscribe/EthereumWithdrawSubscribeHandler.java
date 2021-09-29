package org.starcoin.bifrost.subscribe;

import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.starcoin.bifrost.data.model.EthereumWithdrawStc;
import org.starcoin.bifrost.data.utils.IdUtils;
import org.starcoin.bifrost.ethereum.model.STC;
import org.starcoin.bifrost.service.EthereumLogService;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.protocol.websocket.events.Log;
import org.web3j.protocol.websocket.events.LogNotification;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.net.ConnectException;
import java.util.List;

import static org.starcoin.bifrost.utils.HexUtils.hexToBigInteger;

public class EthereumWithdrawSubscribeHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(EthereumWithdrawSubscribeHandler.class);

    private final String webSocketServiceUrl;

    private final String logFilterAddress;

    private final EthereumLogService ethereumLogService;

    public EthereumWithdrawSubscribeHandler(String webSocketServiceUrl, String logFilterAddress,
                                            EthereumLogService ethereumLogService) {
        this.webSocketServiceUrl = webSocketServiceUrl;
        this.logFilterAddress = logFilterAddress;
        this.ethereumLogService = ethereumLogService;
    }

    public static EthereumWithdrawStc decodeLog(LogWrapper log) {
        List<Type> data = FunctionReturnDecoder.decode(log.getData(), STC.CROSSCHAINWITHDRAWEVENT_EVENT.getNonIndexedParameters());
        BigInteger amount = (BigInteger) data.get(1).getValue();
        BigInteger fromChain = (BigInteger) data.get(2).getValue();
        byte[] toAddress = (byte[]) data.get(0).getValue();
        String fromAddress = FunctionReturnDecoder.decode(log.getTopics().get(1), STC.CROSSCHAINWITHDRAWEVENT_EVENT.getIndexedParameters().subList(0, 1)).get(0).toString();
        //String toAddress = FunctionReturnDecoder.decode(log.getTopics().get(2), STC.CROSSCHAINWITHDRAWEVENT_EVENT.getIndexedParameters().subList(1, 2)).get(0).toString();
        String ownerAddress = FunctionReturnDecoder.decode(log.getTopics().get(2), STC.CROSSCHAINWITHDRAWEVENT_EVENT.getIndexedParameters().subList(1, 2)).get(0).toString();
        //String ownerAddress = FunctionReturnDecoder.decode(log.getTopics().get(3), STC.CROSSCHAINWITHDRAWEVENT_EVENT.getIndexedParameters().subList(2, 3)).get(0).toString();
        EthereumWithdrawStc withdrawStc = createEthereumWithdrawStc(
                log,
                Numeric.toHexString(toAddress), amount, fromAddress, ownerAddress, fromChain);
        return withdrawStc;
    }

    private static EthereumWithdrawStc createEthereumWithdrawStc(LogWrapper log,
                                                                 String toAddress, BigInteger amount,
                                                                 String fromAddress, String ownerAddress,
                                                                 BigInteger fromChain) {
        EthereumWithdrawStc withdrawStc = new EthereumWithdrawStc();
        withdrawStc.setWithdrawAmount(amount);
        withdrawStc.setFromAccount(fromAddress);
        withdrawStc.setToAccount(toAddress);
        withdrawStc.setOwnerAccount(ownerAddress);
        withdrawStc.setFromChain(fromChain);
        withdrawStc.setAddress(log.getAddress());
        withdrawStc.setBlockHash(log.getBlockHash());
        withdrawStc.setTransactionHash(log.getTransactionHash());
        withdrawStc.setTransactionIndex(log.getTransactionIndex());
        withdrawStc.setLogIndex(log.getLogIndex());
        withdrawStc.setData(log.getData());
        withdrawStc.setTopics(log.getTopics());
        withdrawStc.setBlockNumber(log.getBlockNumber());
        withdrawStc.setLogId(IdUtils.generateLogId(withdrawStc));
        withdrawStc.setCreatedAt(System.currentTimeMillis());
        withdrawStc.setCreatedBy("ADMIN");
        withdrawStc.setUpdatedAt(withdrawStc.getCreatedAt());
        withdrawStc.setUpdatedBy(withdrawStc.getCreatedBy());
        return withdrawStc;
    }

    private static EthereumWithdrawStc getEthereumWithdrawStc(Log log) {
        return decodeLog(
                new LogWrapper() {
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
                }
        );
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
                EthereumWithdrawStc withdrawStc = getEthereumWithdrawStc(log);
                ethereumLogService.trySave(withdrawStc);
            }
        } catch (ConnectException e) {
            LOG.info("handle subscribe exception", e);
        }
    }

    public interface LogWrapper {

        String getAddress();

        String getBlockHash();

        String getTransactionHash();

        BigInteger getTransactionIndex();

        BigInteger getLogIndex();

        String getData();

        List<String> getTopics();

        BigInteger getBlockNumber();
    }
}

package org.starcoin.bifrost.taskservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.bifrost.data.model.StarcoinEvent;
import org.starcoin.bifrost.data.model.StcToEthereum;
import org.starcoin.bifrost.data.repo.StarcoinEventRepository;
import org.starcoin.bifrost.service.EthereumTransactionOnChainService;
import org.starcoin.bifrost.service.EthereumTransactionServiceFacade;
import org.starcoin.bifrost.service.StarcoinEventService;
import org.starcoin.jsonrpc.client.JSONRPC2Session;
import org.starcoin.jsonrpc.client.JSONRPC2SessionException;
import org.starcoin.utils.JsonRpcClient;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.starcoin.utils.StarcoinOnChainUtils.getLatestBlockNumber;

@Component
public class StarcoinEventConfirmTaskService {
    private static final Logger LOG = LoggerFactory.getLogger(StarcoinEventConfirmTaskService.class);
    private final String jsonRpcUrl;
    private final JSONRPC2Session jsonRpcSession;
    @Value("${starcoin.needed-block-confirmations}")
    private Integer neededBlockConfirmations;
    @Value("${starcoin.event-confirm-task-service.confirm-event-created-before-seconds}")
    private Long confirmEventCreatedBeforeSeconds;// = 5L;

    @Autowired
    private StarcoinEventRepository starcoinEventRepository;

    @Autowired
    private EthereumTransactionServiceFacade ethereumTransactionServiceFacade;

    @Autowired
    private EthereumTransactionOnChainService ethereumTransactionOnChainService;

    @Autowired
    private StarcoinEventService starcoinEventService;

    public StarcoinEventConfirmTaskService(@Value("${starcoin.json-rpc-url}") String jsonRpcUrl) throws MalformedURLException {
        this.jsonRpcUrl = jsonRpcUrl;
        this.jsonRpcSession = new JSONRPC2Session(new URL(this.jsonRpcUrl));
    }

    @Scheduled(fixedDelayString = "${starcoin.event-confirm-task-service.fixed-delay}")
    public void task() {
        Long createdBefore = System.currentTimeMillis() - confirmEventCreatedBeforeSeconds * 1000;
        List<StarcoinEvent> events = starcoinEventRepository.findByStatusEqualsAndCreatedAtLessThan(StarcoinEvent.STATUS_CREATED, createdBefore);
        if (events == null) {
            return;
        }
        for (StarcoinEvent e : events) {
            if (!(e instanceof StcToEthereum)) {
                continue;
            }
            StcToEthereum stcToEthereum = (StcToEthereum) e;
            try {
                if (!isTransactionExecuted(stcToEthereum.getTransactionHash(),
                        stcToEthereum.getBlockHash(), stcToEthereum.getBlockNumber())) {
                    LOG.error("Check transaction status failed. " + e);
                    continue;
                }
            } catch (RuntimeException runtimeException) {
                LOG.error("Get transaction info error. " + runtimeException);
                continue;
            }
            BigInteger transactionBlockNumber = stcToEthereum.getBlockNumber();
            BigInteger latestBlockNumber;
            try {
                latestBlockNumber = getLatestBlockNumber(jsonRpcSession);
            } catch (RuntimeException | JSONRPC2SessionException | JsonProcessingException runtimeException) {
                LOG.error("Get latest block info error. " + runtimeException);
                continue;
            }
            if (latestBlockNumber.compareTo(transactionBlockNumber.add(BigInteger.valueOf(neededBlockConfirmations))) < 0) {
                LOG.debug("Transaction '" + e.getTransactionHash() + "' not confirmed yet.");
                // ------------------------------------------
                continue;
            }
            try {
                // Confirmed!
                starcoinEventService.confirm(e);
                // create transaction and send.
                starcoinEventService.complementGasDataAndSave(stcToEthereum);
                ethereumTransactionServiceFacade.createMintStcTransactionAndSend(stcToEthereum.getMintAccount(),
                        stcToEthereum.getMintAmount(), stcToEthereum.getEventId(),
                        ethereumTransactionOnChainService.getOnChainGasPrice()
                );
            } catch (IOException | RuntimeException exception) {
                LOG.error("Create Mint STC Transaction error.", exception);
            }
        }
    }


    private boolean isTransactionExecuted(String transactionHash, String blockHash, BigInteger blockNumber) {
        String method = "chain.get_transaction_info";
        Map<String, Object> resultMap = new JsonRpcClient(jsonRpcSession).sendJsonRpc(method,
                Arrays.asList(transactionHash), new TypeReference<Map<String, Object>>() {
                });
        return blockHash.equals(resultMap.get("block_hash")) &&
                blockNumber.compareTo(new BigInteger(resultMap.get("block_number").toString())) == 0 &&
                "Executed".equalsIgnoreCase(String.valueOf(resultMap.get("status")));
    }


}

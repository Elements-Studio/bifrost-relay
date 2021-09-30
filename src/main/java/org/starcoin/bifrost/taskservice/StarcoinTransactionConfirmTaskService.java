package org.starcoin.bifrost.taskservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.bifrost.data.model.AbstractEthereumTransaction;
import org.starcoin.bifrost.data.model.AbstractStarcoinTransaction;
import org.starcoin.bifrost.data.repo.StarcoinTransactionRepository;
import org.starcoin.utils.JsonRpcClient;
import org.starcoin.bifrost.service.StarcoinAccountService;
import org.starcoin.jsonrpc.client.JSONRPC2Session;
import org.starcoin.jsonrpc.client.JSONRPC2SessionException;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.starcoin.utils.StarcoinOnChainUtils.getLatestBlockNumber;

@Component
public class StarcoinTransactionConfirmTaskService {
    private static final Logger LOG = LoggerFactory.getLogger(StarcoinTransactionConfirmTaskService.class);
    private final String jsonRpcUrl;
    private final JSONRPC2Session jsonRpcSession;
    @Value("${starcoin.needed-block-confirmations}")
    private Integer neededBlockConfirmations;
    @Value("${starcoin.transaction-confirm-task-service.confirm-Transaction-created-before-seconds}")
    private Long confirmTransactionCreatedBeforeSeconds;// = 5L;
    @Autowired
    private StarcoinTransactionRepository starcoinTransactionRepository;

    @Autowired
    private StarcoinAccountService starcoinAccountService;

    public StarcoinTransactionConfirmTaskService(@Value("${starcoin.json-rpc-url}") String jsonRpcUrl) throws MalformedURLException {
        this.jsonRpcUrl = jsonRpcUrl;
        this.jsonRpcSession = new JSONRPC2Session(new URL(this.jsonRpcUrl));
    }

    @Scheduled(fixedDelayString = "${starcoin.transaction-confirm-task-service.fixed-delay}")
    public void task() {
        Long updatedBefore = System.currentTimeMillis() - confirmTransactionCreatedBeforeSeconds * 1000;
        List<AbstractStarcoinTransaction> transactions = starcoinTransactionRepository.findByStatusEqualsAndUpdatedAtLessThan(AbstractEthereumTransaction.STATUS_SENT, updatedBefore);
        if (transactions == null) {
            return;
        }
        for (AbstractStarcoinTransaction t : transactions) {
            Map<String, Object> resultMap = new JsonRpcClient(jsonRpcSession).sendJsonRpc("chain.get_transaction_info",
                    Arrays.asList(t.getTransactionHash()), new TypeReference<Map<String, Object>>() {
                    });
            if (resultMap == null || !resultMap.containsKey("block_hash")) {
                LOG.error("Get transaction info error." + resultMap);
                continue;
            }
            BigInteger transactionBlockNumber = new BigInteger(resultMap.get("block_number").toString());
            BigInteger latestBlockNumber;
            try {
                latestBlockNumber = getLatestBlockNumber(jsonRpcSession);
            } catch (RuntimeException | JSONRPC2SessionException | JsonProcessingException e) {
                LOG.error("Get block error.", e);
                continue;
            }
            if (latestBlockNumber.compareTo(transactionBlockNumber.add(BigInteger.valueOf(neededBlockConfirmations))) < 0) {
                LOG.debug("Transaction '" + t.getTransactionHash() + "' not confirmed yet.");
                // ------------------------------------------
                // Update transaction block info...
                updateTransactionBlockAndAccountSequenceNumber(t, resultMap);
                // ------------------------------------------
                continue;
            }
            try {
                // Confirmed!
                t.confirmed();
                updateTransactionBlockAndAccountSequenceNumber(t, resultMap);
            } catch (RuntimeException exception) {
                LOG.error("Update starcoin transaction error.", exception);
                //continue;
            }
        }
    }

    private void updateTransactionBlockAndAccountSequenceNumber(AbstractStarcoinTransaction t, Map<String, Object> onChainTransactionInfo) {
        t.setBlockHash(onChainTransactionInfo.get("block_hash").toString());
        t.setBlockNumber(new BigInteger(onChainTransactionInfo.get("block_number").toString()));
        t.setTransactionIndex(new BigInteger(onChainTransactionInfo.get("transaction_index").toString()));
        t.setUpdatedAt(System.currentTimeMillis());
        t.setUpdatedBy("ADMIN");
        starcoinTransactionRepository.save(t);
        // Update account sequence number(transaction counter) ...
        starcoinAccountService.confirmSequenceNumberOnChain(t.getAccountAddress(), t.getAccountSequenceNumber());
    }
}

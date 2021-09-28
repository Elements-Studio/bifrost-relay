package org.starcoin.bifrost.taskservice;

import com.novi.serde.SerializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.bifrost.data.model.EthereumLog;
import org.starcoin.bifrost.data.model.EthereumWithdrawStc;
import org.starcoin.bifrost.data.model.StarcoinEvent;
import org.starcoin.bifrost.data.repo.EthereumLogRepository;
import org.starcoin.bifrost.service.StarcoinTransactionOnChainService;
import org.starcoin.bifrost.service.StarcoinTransactionServiceFacade;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;


@Component
public class EthereumLogConfirmTaskService {
    private static final Logger LOG = LoggerFactory.getLogger(EthereumLogConfirmTaskService.class);

    @Value("${ethereum.needed-block-confirmations}")
    private Integer neededBlockConfirmations;

    @Value("${ethereum.log-confirm-task-service.confirm-log-created-before-seconds}")
    private Long confirmLogCreatedBeforeSeconds;// = 5L;

    @Autowired
    private Web3j web3j;

    @Autowired
    private EthereumLogRepository ethereumLogRepository;

    @Autowired
    private StarcoinTransactionServiceFacade starcoinTransactionServiceFacade;

    @Autowired
    private StarcoinTransactionOnChainService starcoinTransactionOnChainService;

    @Scheduled(fixedDelayString = "${ethereum.log-confirm-task-service.fixed-delay}")
    public void task() {
        Long createdBefore = System.currentTimeMillis() - confirmLogCreatedBeforeSeconds * 1000;
        List<EthereumLog> events = ethereumLogRepository.findByStatusEqualsAndCreatedAtLessThan(StarcoinEvent.STATUS_CREATED, createdBefore);
        if (events == null) {
            return;
        }
        for (EthereumLog e : events) {
            if (!(e instanceof EthereumWithdrawStc)) {
                continue;
            }
            EthereumWithdrawStc ethereumWithdrawStc = (EthereumWithdrawStc) e;
            try {
                if (!isEthereumTransactionStillThere(ethereumWithdrawStc.getTransactionHash(),
                        ethereumWithdrawStc.getBlockHash(), ethereumWithdrawStc.getBlockNumber())) {
                    LOG.error("Get transaction info error. ", e);
                    continue;
                }
            } catch (IOException ioException) {
                LOG.error("Get transaction info error.", e);
            }
            BigInteger transactionBlockNumber = ethereumWithdrawStc.getBlockNumber();
            BigInteger latestBlockNumber = null;
            try {
                latestBlockNumber = getEthereumLatestBlockNumber();
            } catch (IOException ioException) {
                LOG.error("Get latest block error.", ioException);
                continue;
            }
            if (latestBlockNumber.compareTo(transactionBlockNumber.add(BigInteger.valueOf(neededBlockConfirmations))) < 0) {
                LOG.debug("Transaction '" + e.getTransactionHash() + "' not confirmed yet.");
                // ------------------------------------------
                continue;
            }
            try {
                // Confirmed!
                e.confirmed();
                e.setUpdatedAt(System.currentTimeMillis());
                e.setUpdatedBy("ADMIN");
                ethereumLogRepository.save(e);
                // create transaction and send.
                starcoinTransactionServiceFacade.createDepositStcTransactionAndSend(ethereumWithdrawStc);
            } catch (SerializationError serializationError) {
                LOG.error("Create Starcoin deposit STC Transaction error.", serializationError);
            } catch (RuntimeException runtimeException) {
                LOG.error("Update event or Create Starcoin deposit STC Transaction error.", runtimeException);
            }
        }
    }


    private BigInteger getEthereumLatestBlockNumber() throws IOException {
        EthBlock ethLatestBlock = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
        return ethLatestBlock.getBlock().getNumber();
    }

    private boolean isEthereumTransactionStillThere(String transactionHash, String blockHash, BigInteger blockNumber) throws IOException {
        EthGetTransactionReceipt ethGetTransactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
        TransactionReceipt transactionReceipt = ethGetTransactionReceipt.getTransactionReceipt()
                .orElseThrow(() -> new RuntimeException("CANNOT get transaction: " + transactionHash));
        return blockHash.equals(transactionReceipt.getBlockHash()) &&
                blockNumber.compareTo(transactionReceipt.getBlockNumber()) == 0;
    }


}

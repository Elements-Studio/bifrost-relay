package org.starcoin.bifrost.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.bifrost.data.model.AbstractEthereumTransaction;
import org.starcoin.bifrost.data.repo.EthereumTransactionRepository;
import org.starcoin.bifrost.service.EthereumAccountService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@Component
public class EthereumTransactionConfirmTaskService {
    private static final Logger LOG = LoggerFactory.getLogger(EthereumTransactionConfirmTaskService.class);

    @Value("${ethereum.needed-block-confirmations}")
    private Integer neededBlockConfirmations;

    @Value("${ethereum.transaction-confirm-task-service.confirm-Transaction-created-before-seconds}")
    private Long confirmTransactionCreatedBeforeSeconds;// = 5L;

    @Autowired
    private Web3j web3j;

    @Autowired
    private EthereumTransactionRepository ethereumTransactionRepository;

    @Autowired
    private EthereumAccountService ethereumAccountService;

    @Scheduled(fixedDelayString = "${ethereum.transaction-confirm-task-service.fixed-delay}")
    public void task() {
        Long updatedBefore = System.currentTimeMillis() - confirmTransactionCreatedBeforeSeconds * 1000;
        List<AbstractEthereumTransaction> transactions = ethereumTransactionRepository.findByStatusEqualsAndUpdatedAtLessThan(AbstractEthereumTransaction.STATUS_SENT, updatedBefore);
        if (transactions == null) {
            return;
        }
        for (AbstractEthereumTransaction t : transactions) {
            EthGetTransactionReceipt ethGetTransactionReceipt;
            try {
                ethGetTransactionReceipt = web3j.ethGetTransactionReceipt(t.getTransactionHash()).send();
            } catch (IOException e) {
                LOG.error("Get transaction receipt error.", e);
                continue;
            }
            TransactionReceipt receipt = ethGetTransactionReceipt.getTransactionReceipt().orElse(null);
            if (receipt == null) {
                continue;
            }
            BigInteger transactionBlockNumber = receipt.getBlockNumber();
            EthBlock ethLatestBlock = null;
            try {
                ethLatestBlock = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
            } catch (IOException e) {
                LOG.error("Get block error.", e);
                continue;
            }
            BigInteger latestBlockNumber = ethLatestBlock.getBlock().getNumber();
            if (latestBlockNumber.compareTo(transactionBlockNumber.add(BigInteger.valueOf(neededBlockConfirmations))) < 0) {
                LOG.debug("Transaction '" + t.getTransactionHash() + "' not confirmed yet.");
                // ------------------------------------------
                // Update transaction block info...
                updateTransactionBlockAndAccountNonce(t, receipt);
                // ------------------------------------------
                continue;
            }
            try {
                // Confirmed!
                t.confirmed();
                updateTransactionBlockAndAccountNonce(t, receipt);
            } catch (RuntimeException exception) {
                LOG.error("Update TransactionBlockAndAccountNonce error.", exception);
            }
        }
    }

    private void updateTransactionBlockAndAccountNonce(AbstractEthereumTransaction t, TransactionReceipt receipt) {
        t.setBlockHash(receipt.getBlockHash());
        t.setBlockNumber(receipt.getBlockNumber());
        t.setTransactionIndex(receipt.getTransactionIndex());
        t.setUpdatedAt(System.currentTimeMillis());
        t.setUpdatedBy("ADMIN");
        ethereumTransactionRepository.save(t);
        // Update account nonce(transaction counter) ...
        ethereumAccountService.confirmTransactionCountOnChain(t.getAccountAddress(), t.getAccountNonce());
    }
}

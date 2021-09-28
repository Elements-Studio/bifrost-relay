package org.starcoin.bifrost.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.bifrost.data.model.AbstractEthereumTransaction;
import org.starcoin.bifrost.data.model.EthereumMintStc;
import org.starcoin.bifrost.data.repo.EthereumTransactionRepository;
import org.starcoin.bifrost.service.EthereumTransactionServiceFacade;
import org.web3j.protocol.Web3j;

import java.util.List;

@Component
public class EthereumTransactionResendTaskService {
    private static final Logger LOG = LoggerFactory.getLogger(EthereumTransactionResendTaskService.class);

    @Value("${ethereum.transaction-resend-task-service.resend-created-before-seconds}")
    private Long resendCreatedBeforeSeconds;// = 5L;

    @Autowired
    private Web3j web3j;

    @Autowired
    private EthereumTransactionRepository ethereumTransactionRepository;

    @Autowired
    private EthereumTransactionServiceFacade ethereumTransactionServiceFacade;

    @Scheduled(fixedDelayString = "${ethereum.transaction-resend-task-service.fixed-delay}")
    public void task() {
        Long createdBefore = System.currentTimeMillis() - resendCreatedBeforeSeconds * 1000;
        List<AbstractEthereumTransaction> transactions = ethereumTransactionRepository
                .findByBlockNumberIsNullAndStatusInAndCreatedAtLessThanOrderByCreatedAt(new String[] {
                        AbstractEthereumTransaction.STATUS_SENT,
                        AbstractEthereumTransaction.STATUS_CREATED
                },createdBefore);
        if (transactions == null) {
            return;
        }
        for (AbstractEthereumTransaction t : transactions) {
            LOG.debug("Find created or sent transaction which without receipt and created before "
                    + createdBefore + " seconds: " + t.getTransactionHash());
            if (t instanceof EthereumMintStc) {
                try {
                    ethereumTransactionServiceFacade.updateMintStcTransactionStatusAndSend(t.getTransactionHash());
                } catch (RuntimeException exception) {
                    LOG.error("Update Mint STC Transaction error or send error.", exception);
                }
            }
        }
    }

}

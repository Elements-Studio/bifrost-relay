package org.starcoin.bifrost.taskservice;

import com.novi.serde.SerializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.bifrost.data.model.AbstractEthereumTransaction;
import org.starcoin.bifrost.data.model.AbstractStarcoinTransaction;
import org.starcoin.bifrost.data.model.StarcoinDepositStc;
import org.starcoin.bifrost.data.repo.StarcoinTransactionRepository;
import org.starcoin.bifrost.service.StarcoinTransactionServiceFacade;

import java.util.List;

@Component
public class StarcoinTransactionResendTaskService {
    private static final Logger LOG = LoggerFactory.getLogger(StarcoinTransactionResendTaskService.class);

    @Value("${starcoin.transaction-resend-task-service.resend-created-before-seconds}")
    private Long resendCreatedBeforeSeconds;// = 5L;

    @Autowired
    private StarcoinTransactionRepository starcoinTransactionRepository;

    @Autowired
    private StarcoinTransactionServiceFacade starcoinTransactionServiceFacade;

    @Scheduled(fixedDelayString = "${starcoin.transaction-resend-task-service.fixed-delay}")
    public void task() {
        Long createdBefore = System.currentTimeMillis() - resendCreatedBeforeSeconds * 1000;
        List<AbstractStarcoinTransaction> transactions = starcoinTransactionRepository
                .findByBlockNumberIsNullAndStatusInAndCreatedAtLessThanOrderByCreatedAt(new String[] {
                        AbstractEthereumTransaction.STATUS_SENT,
                        AbstractEthereumTransaction.STATUS_CREATED
                },createdBefore);
        if (transactions == null) {
            return;
        }
        for (AbstractStarcoinTransaction t : transactions) {
            LOG.debug("Find created or sent transaction which without receipt and created before "
                    + createdBefore + " seconds: " + t.getTransactionHash());
            if (t instanceof StarcoinDepositStc) {
                try {
                    starcoinTransactionServiceFacade.updateDepositTransactionStatusAndSend(t.getTransactionHash());
                } catch (SerializationError | RuntimeException exception) {
                    LOG.error("Update deposit transaction error.");
                    continue;
                }
            }
        }
    }

}

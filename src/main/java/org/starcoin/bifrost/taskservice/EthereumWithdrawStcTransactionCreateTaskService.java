package org.starcoin.bifrost.taskservice;

import com.novi.serde.SerializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.bifrost.data.model.EthereumWithdrawStc;
import org.starcoin.bifrost.data.repo.EthereumLogRepository;
import org.starcoin.bifrost.service.StarcoinTransactionOnChainService;
import org.starcoin.bifrost.service.StarcoinTransactionServiceFacade;

import java.util.List;

@Component
public class EthereumWithdrawStcTransactionCreateTaskService {
    private static final Logger LOG = LoggerFactory.getLogger(EthereumWithdrawStcTransactionCreateTaskService.class);

    @Value("${ethereum.to-starcoin.create-transaction-for-log-confirmed-before-seconds}")
    private Long createForConfirmedBeforeSeconds;// = 5L;

    @Autowired
    private EthereumLogRepository ethereumLogRepository;

    @Autowired
    private StarcoinTransactionServiceFacade starcoinTransactionServiceFacade;

    @Autowired
    private StarcoinTransactionOnChainService starcoinTransactionOnChainService;

    @Scheduled(fixedDelayString = "${ethereum.to-starcoin.transaction-create-fixed-delay}")
    public void task() {
        Long confirmedBefore = System.currentTimeMillis() - createForConfirmedBeforeSeconds;
        List<EthereumWithdrawStc> events = ethereumLogRepository
                .findEthereumWithdrawStcLogsByTransactionNotExistsAndConfirmedBefore(confirmedBefore);
        for (EthereumWithdrawStc e : events) {
            LOG.debug("Find confirmed EthereumWithdrawStc log: " + e);
            try {
                starcoinTransactionServiceFacade.createDepositStcTransactionAndSend(e);
            } catch (SerializationError | RuntimeException exception) {
                LOG.error("Create Mint STC Transaction error.", exception);
            }
        }
    }
}

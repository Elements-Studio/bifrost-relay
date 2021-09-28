package org.starcoin.bifrost.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.bifrost.data.model.StcToEthereum;
import org.starcoin.bifrost.data.repo.StarcoinEventRepository;
import org.starcoin.bifrost.service.EthereumTransactionOnChainService;
import org.starcoin.bifrost.service.EthereumTransactionServiceFacade;
import org.starcoin.bifrost.service.StarcoinEventService;

import java.io.IOException;
import java.util.List;

@Component
public class StcToEthereumTransactionCreateTaskService {
    private static final Logger LOG = LoggerFactory.getLogger(StcToEthereumTransactionCreateTaskService.class);

    @Value("${starcoin.to-ethereum.create-transaction-for-event-confirmed-before-seconds}")
    private Long createForConfirmedBeforeSeconds;// = 5L;

    @Autowired
    private StarcoinEventRepository starcoinEventRepository;

    @Autowired
    private EthereumTransactionServiceFacade ethereumTransactionServiceFacade;

    @Autowired
    private EthereumTransactionOnChainService ethereumTransactionOnChainService;

    @Autowired
    private StarcoinEventService starcoinEventService;

    @Scheduled(fixedDelayString = "${starcoin.to-ethereum.transaction-create-fixed-delay}")
    public void task() {
        Long confirmedBefore = System.currentTimeMillis() - createForConfirmedBeforeSeconds;
        List<StcToEthereum> events = starcoinEventRepository.findStcToEthereumEventsByTransactionNotExistsAndConfirmedBefore(confirmedBefore);
        for (StcToEthereum stcToEthereum : events) {
            LOG.debug("Find confirmed StcToEthereum event: " + stcToEthereum);
            try {
                starcoinEventService.complementGasDataAndSave(stcToEthereum);
                ethereumTransactionServiceFacade.createMintStcTransactionAndSend(stcToEthereum.getMintAccount(),
                        stcToEthereum.getMintAmount(), stcToEthereum.getEventId(),
                        ethereumTransactionOnChainService.getOnChainGasPrice());
            } catch (IOException | RuntimeException exception) {
                LOG.error("Create Mint STC Transaction error.", exception);
            }
        }
    }
}

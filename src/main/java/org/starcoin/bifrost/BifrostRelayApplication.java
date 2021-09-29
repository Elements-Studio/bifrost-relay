package org.starcoin.bifrost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.starcoin.bifrost.service.EthereumLogService;
import org.starcoin.bifrost.service.EthereumTransactionServiceFacade;
import org.starcoin.bifrost.service.StarcoinEventService;
import org.starcoin.bifrost.service.StarcoinTransactionServiceFacade;
import org.starcoin.bifrost.subscribe.EthereumWithdrawSubscribeHandler;
import org.starcoin.bifrost.subscribe.StarcoinCrossChainDepositSubscribeHandler;
import springfox.documentation.oas.annotations.EnableOpenApi;

import java.io.IOException;

@SpringBootApplication
@EnableOpenApi
@EnableScheduling
@EnableAsync
public class BifrostRelayApplication {
    private static final Logger LOG = LoggerFactory.getLogger(BifrostRelayApplication.class);

    @Value("${ethereum.websocket-service-url}")
    private String ethereumWebSocketServiceUrl;// = "wss://mainnet.infura.io/ws/v3/72637bfa15a940dcadcec25a6fe0fca1";

    @Value("${ethereum.withdraw-log-filter-address}")
    private String ethereumWithdrawLogFilterAddress;// = "0xF21aF28b798E40B1b8734655C3662B360367914e";

    //    @Value("${starcoin.websocket-service-url}")
    //    private String starcoinWebSocketServiceUrl;

    @Value("${starcoin.seeds}")
    private String[] starcoinSeeds;


    @Value("${starcoin.event-filter.from-address}")
    private String starcoinEventFilterAddress;

    @Value("${starcoin.event-filter.cross-chain-deposit-event-type-tag}")
    private String starcoinCrossChainDepositEventTypeTag;

    @Autowired
    private EthereumLogService ethereumLogService;

    @Autowired
    private EthereumTransactionServiceFacade ethereumTransactionServiceFacade;

    @Autowired
    private StarcoinTransactionServiceFacade starcoinTransactionServiceFacade;

    @Autowired
    private StarcoinEventService starcoinEventService;


    public static void main(String[] args) {
        SpringApplication.run(BifrostRelayApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    void runEthereumWithdrawSubscribeHandler() {
        LOG.info("EXECUTING : EthereumWithdrawSubscribeHandler");
        Thread handlerThread = new Thread(new EthereumWithdrawSubscribeHandler(ethereumWebSocketServiceUrl,
                ethereumWithdrawLogFilterAddress, ethereumLogService));
        handlerThread.start();
    }

    @EventListener(ApplicationReadyEvent.class)
    void runStarcoinCrossChainDepositSubscribeHandler() {
        for (String seed : starcoinSeeds) {
            LOG.info("EXECUTING : StarcoinCrossChainDepositSubscribeHandler, seed: " + seed);
            Thread handlerThread = new Thread(new StarcoinCrossChainDepositSubscribeHandler(seed,
                    starcoinEventService, starcoinEventFilterAddress,
                    starcoinCrossChainDepositEventTypeTag));
            handlerThread.start();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    void initEthereumTransactionSenderAccount() {
        try {
            ethereumTransactionServiceFacade.createSenderAccountIfNoExists();
        } catch (IOException e) {
            LOG.error("Create ethereum sender account error.", e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    void initStarcoinTransactionSenderAccount() {
        try {
            starcoinTransactionServiceFacade.createSenderAccountIfNoExists();
        } catch (RuntimeException e) {
            LOG.error("Create starcoin sender account error.", e);
        }
    }
}

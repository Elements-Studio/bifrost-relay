package org.starcoin.bifrost.taskservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.starcoin.bifrost.data.model.EthereumPullingLogTask;
import org.starcoin.bifrost.data.model.EthereumWithdrawStc;
import org.starcoin.bifrost.data.repo.EthereumNodeHeartbeatRepository;
import org.starcoin.bifrost.service.EthereumLogService;
import org.starcoin.bifrost.service.EthereumNodeHeartbeatService;
import org.starcoin.bifrost.service.EthereumPullingLogTaskService;
import org.starcoin.bifrost.subscribe.EthereumWithdrawSubscribeHandler;
import org.starcoin.bifrost.subscribe.EthereumWithdrawSubscriber;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.starcoin.bifrost.subscribe.EthereumWithdrawSubscribeHandler.decodeLog;

@Component
public class EthereumPullingLogTaskExecuteTaskService {
    private static final Logger LOG = LoggerFactory.getLogger(EthereumPullingLogTaskExecuteTaskService.class);

    @Value("${ethereum.withdraw-log-filter-address}")
    private String ethereumWithdrawLogFilterAddress;

    @Autowired
    private Web3j web3j;

    @Autowired
    private EthereumLogService ethereumLogService;

    @Autowired
    private EthereumPullingLogTaskService ethereumPullingLogTaskService;

    @Autowired
    private EthereumNodeHeartbeatRepository ethereumNodeHeartbeatRepository;

    private static EthereumWithdrawStc getEthereumWithdrawStc(Log log) {
        return decodeLog(
                new EthereumWithdrawSubscribeHandler.LogWrapper() {
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
                        return log.getTransactionIndex();
                    }

                    public BigInteger getLogIndex() {
                        return log.getLogIndex();
                    }

                    public String getData() {
                        return log.getData();
                    }

                    public List<String> getTopics() {
                        return log.getTopics();
                    }

                    public BigInteger getBlockNumber() {
                        return log.getBlockNumber();
                    }
                }
        );
    }

    @Scheduled(fixedDelayString = "${ethereum.pulling-log-task-execute-task-service.fixed-delay}")
    public void task() {
        List<EthereumPullingLogTask> pullingEventTasks = ethereumPullingLogTaskService.getPullingTaskToProcess();
        if (pullingEventTasks == null || pullingEventTasks.isEmpty()) {
            return;
        }
        for (EthereumPullingLogTask t : pullingEventTasks) {
            executeTask(t);
        }
    }

    private void executeTask(EthereumPullingLogTask t) {
        DefaultBlockParameter fromBlock = new DefaultBlockParameterNumber(t.getFromBlockNumber());
        DefaultBlockParameter toBlock = new DefaultBlockParameterNumber(t.getToBlockNumber());
        EthFilter filter = new EthFilter(fromBlock, toBlock, ethereumWithdrawLogFilterAddress)
                .addSingleTopic(EthereumWithdrawSubscriber.TOPIC_CROSS_CHAIN_WITHDRAW_EVENT);
        List<Log> logs = new ArrayList<>();
        try {
            EthLog ethLog = web3j.ethGetLogs(filter).send();
            for (EthLog.LogResult l : ethLog.getLogs()) {
                logs.add((Log) l);
            }
        } catch (IOException | RuntimeException exception) {
            LOG.error("Web3j ethGetLogs error.", exception);
            return;
        }
        // use a new individual nodeId to record heartbeats.
        EthereumNodeHeartbeatService nodeHeartbeatService = new EthereumNodeHeartbeatService(ethereumNodeHeartbeatRepository);
        nodeHeartbeatService.beat(t.getFromBlockNumber());
        for (Log log : logs) {
            EthereumWithdrawStc withdrawStc = getEthereumWithdrawStc(log);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Withdraw STC on ethereum chain: " + withdrawStc);
            }
            ethereumLogService.trySave(withdrawStc);

        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("End of pulling and saved.");
        }
        ethereumPullingLogTaskService.updateStatusDone(t);
        nodeHeartbeatService.beat(t.getToBlockNumber());
    }
}

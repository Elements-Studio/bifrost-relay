package org.starcoin.bifrost.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.EthereumLog;
import org.starcoin.bifrost.data.model.EthereumWithdrawStc;
import org.starcoin.bifrost.data.repo.EthereumLogRepository;

@Service
public class EthereumLogService {
    private static final Logger LOG = LoggerFactory.getLogger(EthereumLogService.class);

    @Autowired
    private EthereumLogRepository ethereumLogRepository;


    public boolean trySave(EthereumWithdrawStc withdrawStc) {
        boolean eventHandled;
        EthereumLog ethereumLog = ethereumLogRepository.findById(withdrawStc.getLogId()).orElse(null);
        if (ethereumLog != null) {
            eventHandled = true;
        } else {
            try {
                ethereumLogRepository.save(withdrawStc);
                eventHandled = true;
            } catch (RuntimeException e) {
                LOG.error("Save ethereum withdraw STC log error.", e);
                eventHandled = false;
            }
        }
        return eventHandled;
    }

}

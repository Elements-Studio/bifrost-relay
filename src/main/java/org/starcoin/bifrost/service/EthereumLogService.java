package org.starcoin.bifrost.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.EthereumLog;
import org.starcoin.bifrost.data.model.EthereumWithdrawStc;
import org.starcoin.bifrost.data.repo.EthereumLogRepository;

@Service
public class EthereumLogService {

    @Autowired
    private EthereumLogRepository ethereumLogRepository;

    public boolean trySave(EthereumWithdrawStc withdrawStc) {
        EthereumLog log = ethereumLogRepository.findById(withdrawStc.getLogId()).orElse(null);
        if (log != null) {
            return false;
        }
        ethereumLogRepository.save(withdrawStc);
        return true;
    }

}

package org.starcoin.bifrost.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.starcoin.bifrost.data.UnknownDataIntegrityViolationException;

import java.math.BigInteger;

public abstract class EthereumTransactionDataIntegrityViolationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(EthereumMintStcDataIntegrityViolationHandler.class);
    protected final EthereumTransactionService ethereumTransactionService;
    //private final EthereumAccountService ethereumAccountService;
    protected final String senderAddress;
    protected final BigInteger accountNonce;
    protected final String triggerEventId;

    public EthereumTransactionDataIntegrityViolationHandler(EthereumTransactionService ethereumTransactionService,
                                                            String senderAddress, BigInteger accountNonce,
                                                            String triggerEventId) {
        this.ethereumTransactionService = ethereumTransactionService;
        this.senderAddress = senderAddress;
        this.accountNonce = accountNonce;
        this.triggerEventId = triggerEventId;
    }

    public void handleKnownViolation(Runnable runnable) throws UnknownDataIntegrityViolationException {
        try {
            runnable.run();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            LOG.debug("Handle date integrity violation.", e);
            String transactionHash = getTransactionHash();
            if (transactionHash != null) {
                // Log and ignore???
                LOG.info("Transaction hash exists: " + transactionHash);
            } else if (ethereumTransactionService.isAccountNodeUsed(senderAddress, accountNonce)) {
                // Log and ignore???
                LOG.info("Account nonce already used, " + senderAddress + "::" + accountNonce);
                //ethereumAccountService.setTransactionCountGreaterThan(senderAddress, accountNonce);
            } else if (ethereumTransactionService.isSourceEventTriggered(triggerEventId)) {
                // Log and ignore???
                LOG.info("Event already triggered, triggerEventId: " + triggerEventId);
            } else {
                String msg = "Unknown DATA INTEGRITY VIOLATION! TriggerEventId: " + triggerEventId;
                LOG.error(msg, e);
                throw new UnknownDataIntegrityViolationException(msg, e);
            }
        }
    }

    protected abstract String getTransactionHash();
}

package org.starcoin.bifrost.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.StarcoinEvent;
import org.starcoin.bifrost.data.model.StcToEthereum;
import org.starcoin.bifrost.data.repo.StarcoinEventRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class StarcoinEventService {
    private static final Logger LOG = LoggerFactory.getLogger(StarcoinEventService.class);

    @Autowired
    private StarcoinEventRepository starcoinEventRepository;

    @Autowired
    private EthereumTransactionOnChainService ethereumTransactionOnChainService;

    @Autowired
    private EthereumTransactionService ethereumTransactionService;

    @Autowired
    private TokenPriceService tokenPriceService;

    @Transactional
    public boolean trySave(StcToEthereum stcToEthereum) {
        boolean eventHandled;
        StarcoinEvent starcoinEvent = starcoinEventRepository.findById(stcToEthereum.getEventId()).orElse(null);
        if (starcoinEvent != null) {
            eventHandled = true;
        } else {
            try {
                starcoinEventRepository.save(stcToEthereum);
                eventHandled = true;
            } catch (RuntimeException e) {
                LOG.error("Save StcToEthereum event error.", e);
                eventHandled = false;
            }
        }
        return eventHandled;
    }

    @Transactional
    public void confirm(StarcoinEvent e) {
        e.confirmed();
        e.setUpdatedAt(System.currentTimeMillis());
        e.setUpdatedBy("ADMIN");
        starcoinEventRepository.save(e);
    }

    @Transactional
    public void complementGasDataAndSave(StcToEthereum e) throws IOException {
        if (!(e.getGasPriceInWei() == null
                || e.getEstimatedGas() == null
                || e.getWeiToNanoStcExchangeRate() == null
                || e.getMintAmount() == null)) {
            return;
        }
        e.setGasPriceInWei(ethereumTransactionOnChainService.getOnChainGasPrice());
        e.setEstimatedGas(ethereumTransactionOnChainService.estimateMintStcGas(
                e.getMintAccount(), e.getDepositAmount(), // todo should be e.getMintAmount(),???
                ethereumTransactionService.getAccountNonce(), e.getGasPriceInWei()));
        e.setWeiToNanoStcExchangeRate(tokenPriceService.getWeiToNanoStcExchangeRate());
        BigInteger gasFee = e.getWeiToNanoStcExchangeRate().multiply(new BigDecimal(
                e.getGasPriceInWei().multiply(e.getEstimatedGas()))).toBigInteger();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Estimated gas fee(in NanoSTC): " + gasFee + ", event Id: " + e.getEventId());
        }
        BigInteger mintAmount = e.getDepositAmount().subtract(gasFee);
        if (mintAmount.signum() == -1) {
            String msg = "Insufficient gas fee. Estimated gas fee(in NanoSTC): " + gasFee
                    + ", deposit amount: " + e.getDepositAmount()
                    + ", event Id: " + e.getEventId();
            LOG.error(msg); //todo This record SHOULD NOT try again?
            throw new RuntimeException(msg);
        }
        e.setMintAmount(mintAmount);

        starcoinEventRepository.save(e);
    }
}

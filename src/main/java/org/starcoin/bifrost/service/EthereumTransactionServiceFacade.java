package org.starcoin.bifrost.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.EthereumMintStc;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class EthereumTransactionServiceFacade {
    @Autowired
    private EthereumTransactionService ethereumTransactionService;

    @Autowired
    private EthereumTransactionOnChainService ethereumTransactionOnChainService;

    @Autowired
    private TokenPriceService tokenPriceService;

    @Autowired
    private EthereumAccountService ethereumAccountService;

    /**
     * Get current WEI to NanoSTC exchange rate.
     */
    public BigDecimal getWeiToNanoStcExchangeRate() {
        //BigDecimal ethToStc = getTestEthToStcExchangeRate();
        //return ethToStc.divide(ETH_TO_WEI, 18, BigDecimal.ROUND_HALF_UP).multiply(STC_TO_NANOSTC);
        return tokenPriceService.getWeiToNanoStcExchangeRate();
    }

    //    public BigDecimal getTestEthToStcExchangeRate() {
    //        BigDecimal ethToUsd = new BigDecimal("3217.02");
    //        BigDecimal stcToUsd = new BigDecimal("0.1578");
    //        BigDecimal ethToStc = ethToUsd.divide(stcToUsd, 18, BigDecimal.ROUND_HALF_UP);
    //        return ethToStc;
    //    }

    public BigInteger estimateDepositFromStarcoinGas(String to, BigInteger amount, BigInteger gasPrice) throws IOException {
        BigInteger accountNonce = ethereumTransactionService.getAccountNonce();
        return ethereumTransactionOnChainService.estimateMintStcGas(to, amount, accountNonce, gasPrice);
    }

    public String createMintStcTransactionAndSend(String mintAccount, BigInteger mintAmount, String triggerEventId,
                                                  BigInteger gasPrice) {
        EthereumMintStc mintStc = ethereumTransactionService.createMintStcTransactionSent(mintAccount, mintAmount, triggerEventId, gasPrice);
        byte[] signedMessage = mintStc.getSignedMessage();
        if (signedMessage == null) {
            throw new RuntimeException("CANNOT get signed message.");
        }
        ethereumTransactionOnChainService.sendMintStcTransaction(mintStc, signedMessage);
        return mintStc.getTransactionHash();
    }

    public void updateMintStcTransactionStatusAndSend(String transactionHash) {
        EthereumMintStc mintStc = ethereumTransactionService.updateMintStcTransactionSent(transactionHash);
        ethereumTransactionOnChainService.sendMintStcTransaction(mintStc);
    }

    public void createSenderAccountIfNoExists() throws IOException {
        String address = ethereumTransactionOnChainService.getSenderAddress();
        if (ethereumAccountService.getEthereumAccountOrElseNull(address) == null) {
            ethereumAccountService.resetByOnChainTransactionCount(address);
        }
    }

}

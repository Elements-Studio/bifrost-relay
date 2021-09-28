package org.starcoin.bifrost.service;

import java.math.BigInteger;

public class EthereumMintStcDataIntegrityViolationHandler extends EthereumTransactionDataIntegrityViolationHandler {

    private final String mintAccount;
    private final BigInteger mintAmount;
    private final BigInteger gasPrice;

    public EthereumMintStcDataIntegrityViolationHandler(EthereumTransactionService ethereumTransactionService,
                                                        String senderAddress,
                                                        BigInteger accountNonce, BigInteger gasPrice,
                                                        String triggerEventId,
                                                        String mintAccount, BigInteger mintAmount) {
        super(ethereumTransactionService, senderAddress, accountNonce, triggerEventId);
        this.mintAccount = mintAccount;
        this.mintAmount = mintAmount;
        this.gasPrice = gasPrice;
    }

    @Override
    protected String getTransactionHash() {
        return ethereumTransactionService.getMintStcHashIfTransactionExists(mintAccount, mintAmount, accountNonce, gasPrice);
    }
}

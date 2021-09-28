package org.starcoin.bifrost.service;

import com.novi.serde.SerializationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.EthereumWithdrawStc;
import org.starcoin.bifrost.data.model.StarcoinDepositStc;

import java.math.BigInteger;

import static org.starcoin.bifrost.service.StarcoinTransactionOnChainService.TRANSACTION_EXPIRATION_SECONDS;
import static org.starcoin.bifrost.utils.StarcoinAccountAddressUtils.trimAddress;

@Service
public class StarcoinTransactionServiceFacade {
    @Autowired
    private StarcoinTransactionService starcoinTransactionService;

    @Autowired
    private StarcoinTransactionOnChainService starcoinTransactionOnChainService;

    @Autowired
    private StarcoinAccountService starcoinAccountService;

    public String createDepositStcTransactionAndSend(String depositAccount, BigInteger depositAmount,
                                                     String fromAccount, BigInteger fromChain,
                                                     String triggerEventId,
                                                     BigInteger gasPrice, Long expirationTimestampSecs) throws SerializationError {
        StarcoinDepositStc depositStc = starcoinTransactionService.createDepositStcTransactionSent(
                depositAccount, depositAmount, fromAccount, fromChain, triggerEventId, gasPrice, expirationTimestampSecs);
        byte[] signedMessage = depositStc.getSignedMessage();
        if (signedMessage == null) {
            throw new RuntimeException("CANNOT get signed message.");
        }
        starcoinTransactionOnChainService.sendDepositStcTransaction(depositStc, signedMessage);
        return depositStc.getTransactionHash();
    }

    public void updateDepositTransactionStatusAndSend(String transactionHash) throws SerializationError {
        StarcoinDepositStc depositStc = starcoinTransactionService.updateDepositTransactionStatusSent(transactionHash);
        starcoinTransactionOnChainService.sendDepositStcTransaction(depositStc);
    }

    /**
     * Create starcoin transaction from ethereum log(event), deducting gas fee.
     *
     * @param ethereumWithdrawStc ethereum log(event)
     * @throws SerializationError Serialization Error
     */
    public void createDepositStcTransactionAndSend(EthereumWithdrawStc ethereumWithdrawStc) throws SerializationError {
        BigInteger gasPrice = starcoinTransactionOnChainService.getOnChainGasPrice();
        BigInteger gasAmount = starcoinTransactionOnChainService.estimateDepositStcGas(
                ethereumWithdrawStc.getFromAccount(), ethereumWithdrawStc.getToAccount(),
                ethereumWithdrawStc.getWithdrawAmount(), // todo Is this ok?
                ethereumWithdrawStc.getFromChain().intValue()
        );
        BigInteger gasFee = gasPrice.multiply(gasAmount);
        BigInteger depositAmount = ethereumWithdrawStc.getWithdrawAmount().subtract(gasFee);
        createDepositStcTransactionAndSend(
                trimAddress(ethereumWithdrawStc.getToAccount()),
                depositAmount,
                ethereumWithdrawStc.getFromAccount(),
                ethereumWithdrawStc.getFromChain(),
                ethereumWithdrawStc.getLogId(),
                gasPrice,
                starcoinTransactionOnChainService.getNowSeconds() + TRANSACTION_EXPIRATION_SECONDS
        );
    }

    public void createSenderAccountIfNoExists() {
        String address = starcoinTransactionOnChainService.getSenderAddress();
        if (starcoinAccountService.getStarcoinAccountOrElseNull(address) == null) {
            starcoinAccountService.resetByOnChainSequenceNumber(address);
        }
    }
}
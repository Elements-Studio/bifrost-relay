package org.starcoin.bifrost.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.DomainError;
import org.starcoin.bifrost.data.model.EthereumAccount;
import org.starcoin.bifrost.data.repo.EthereumAccountRepository;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigInteger;

import static org.starcoin.bifrost.DomainError.INVALID_ENTITY_ID;

@Service
public class EthereumAccountService {

    @Autowired
    private EthereumAccountRepository ethereumAccountRepository;

    @Autowired
    private Web3j web3j;

    @Transactional
    public void resetByOnChainTransactionCount(String address) throws IOException {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameter.valueOf("latest")).send();
        BigInteger txnCount = ethGetTransactionCount.getTransactionCount();
        resetTransactionCount(address, txnCount);
    }

    @Transactional
    public void resetTransactionCount(String address, BigInteger txnCount) {
        EthereumAccount ethereumAccount = ethereumAccountRepository.findById(address).orElse(null);
        if (ethereumAccount == null) {
            ethereumAccount = new EthereumAccount();
            ethereumAccount.setAddress(address);
            ethereumAccount.setCreatedBy("ADMIN");
            ethereumAccount.setCreatedAt(System.currentTimeMillis());
        }
        ethereumAccount.setConfirmedTransactionCount(txnCount);
        ethereumAccount.setTransactionCount(txnCount.subtract(BigInteger.ONE));
        ethereumAccount.setUpdatedBy("ADMIN");
        ethereumAccount.setUpdatedAt(System.currentTimeMillis());
        ethereumAccountRepository.save(ethereumAccount);
    }

    @Transactional
    public BigInteger getTransactionCountAndIncrease(String address) {
        EthereumAccount ethereumAccount = assertAccountAddress(address);
        BigInteger transactionCount = ethereumAccount.getTransactionCount();
        ethereumAccount.setTransactionCount(ethereumAccount.getTransactionCount().add(BigInteger.ONE));
        ethereumAccountRepository.save(ethereumAccount);
        return transactionCount;
    }

    @Transactional
    public BigInteger getTransactionCount(String address) {
        EthereumAccount ethereumAccount = assertAccountAddress(address);
        return ethereumAccount.getTransactionCount();
    }

    /**
     * Get confirmed(on-chained) transaction count.
     * @param address account address
     */
    @Transactional
    public BigInteger getConfirmedTransactionCount(String address) {
        EthereumAccount ethereumAccount = assertAccountAddress(address);
        return ethereumAccount.getConfirmedTransactionCount();
    }

    @Transactional
    public void confirmTransactionCountOnChain(String address, BigInteger count) {
        EthereumAccount ethereumAccount = assertAccountAddress(address);
        if (ethereumAccount.getConfirmedTransactionCount().compareTo(count) < 0) {
            ethereumAccount.setConfirmedTransactionCount(count);
            if (ethereumAccount.getTransactionCount().compareTo(count.add(BigInteger.ONE)) < 0) {
                ethereumAccount.setTransactionCount(count.add(BigInteger.ONE));
            }
            ethereumAccountRepository.save(ethereumAccount);
        }
    }

    public EthereumAccount getEthereumAccountOrElseNull(String address) {
        return ethereumAccountRepository.findById(address).orElse(null);
    }

    private EthereumAccount assertAccountAddress(String address) {
        EthereumAccount ethereumAccount = ethereumAccountRepository.findById(address).orElse(null);
        if (ethereumAccount == null) {
            throw DomainError.named(INVALID_ENTITY_ID, "CANNOT find account by address: " + address);
        }
        return ethereumAccount;
    }

}

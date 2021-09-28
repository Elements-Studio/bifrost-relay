package org.starcoin.bifrost.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.*;
import org.starcoin.bifrost.data.repo.DroppedEthereumTransactionRepository;
import org.starcoin.bifrost.data.repo.EthereumTransactionRepository;
import org.starcoin.bifrost.data.repo.UnexpectedEthereumTransactionRepository;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigInteger;

import static org.starcoin.bifrost.service.EthereumTransactionOnChainService.MINT_STC_GAS_LIMIT;

@Service
public class EthereumTransactionService {
    static final Logger LOG = LoggerFactory.getLogger(EthereumTransactionService.class);
    //private static final BigInteger DEFAULT_GAS_PRICE = DefaultGasProvider.GAS_PRICE;

    @Autowired
    private EthereumTransactionRepository ethereumTransactionRepository;

//    @Autowired
//    private Executor taskExecutor;

    @Autowired
    private EthereumAccountService ethereumAccountService;

    @Autowired
    private UnexpectedEthereumTransactionRepository unexpectedEthereumTransactionRepository;

    @Autowired
    private DroppedEthereumTransactionRepository droppedEthereumTransactionRepository;

    @Autowired
    private EthereumTransactionOnChainService ethereumTransactionOnChainService;

    public BigInteger getAccountNonceAndIncrease() {
        return ethereumAccountService.getTransactionCountAndIncrease(ethereumTransactionOnChainService.getSenderAddress());
    }

    public BigInteger getAccountNonce() {
        return ethereumAccountService.getTransactionCount(ethereumTransactionOnChainService.getSenderAddress());
    }

    @Transactional
    public boolean isAccountNodeUsed(String accountAddress, BigInteger nonce) {
        AbstractEthereumTransaction t = ethereumTransactionRepository.findFirstByAccountAddressAndAccountNonce(accountAddress, nonce);
        return t != null;
    }

    @Transactional
    public boolean isSourceEventTriggered(String eventId) {
        AbstractEthereumTransaction transaction = ethereumTransactionRepository.findFirstByTriggerEventId(eventId);
        return transaction != null;
    }

    @Transactional
    public EthereumMintStc createMintStcTransactionSent(String mintAccount, BigInteger mintAmount, String triggerEventId,
                                                  BigInteger gasPrice) {
        BigInteger accountNonce = getAccountNonceAndIncrease();
        EthereumMintStc mintStc = doCreateMintStcTransactionAndSave(mintAccount, mintAmount, triggerEventId, accountNonce, gasPrice);
        //RawTransaction rawTransaction = (RawTransaction) mintStc.getRawTransaction();
        //Credentials credentials = (Credentials) mintStc.getCredentials();
        updateMintStcTransactionSent(mintStc);
        return mintStc;
    }

    /**
     * This mothod is only for TEST!
     */
    @Transactional
    public String createMintStcTransaction(String mintAccount, BigInteger mintAmount,
                                           String triggerEventId, BigInteger gasPrice, BigInteger accountNonce) {
        EthereumMintStc mintStc = doCreateMintStcTransactionAndSave(mintAccount, mintAmount, triggerEventId, accountNonce, gasPrice);
        return mintStc.getTransactionHash();
    }

    @Transactional
    public String createMintStcTransaction(String mintAccount, BigInteger mintAmount,
                                           String triggerEventId, BigInteger gasPrice) {
        BigInteger accountNonce = getAccountNonceAndIncrease();
        EthereumMintStc mintStc = doCreateMintStcTransactionAndSave(mintAccount, mintAmount, triggerEventId, accountNonce, gasPrice);
        return mintStc.getTransactionHash();
    }

    @Transactional
    public EthereumMintStc updateMintStcTransactionSent(String transactionHash) {
        EthereumMintStc mintStc = assertEthereumMintStcTransactionHash(transactionHash);
        //String encodedFunction = encodeFunctionCall(mintStc.getMintAccount(), mintStc.getMintAmount());
        updateMintStcTransactionSent(mintStc);
        return mintStc;
    }

    @Transactional // use local database transaction?
    public void dropTransactionBecauseOfUnexpectedTransaction(String transactionHash, String unexpectedTransactionHash) throws IOException {
        Transaction onChainTransaction = ethereumTransactionOnChainService.getOnChainTransaction(unexpectedTransactionHash);
        EthereumMintStc ethereumMintStc = assertEthereumMintStcTransactionHash(transactionHash);
        ethereumMintStc.dropped(unexpectedTransactionHash, onChainTransaction.getFrom(), onChainTransaction.getNonce());
        ethereumMintStc.setUpdatedBy("ADMIN");
        ethereumMintStc.setUpdatedAt(System.currentTimeMillis());
        ethereumTransactionRepository.save(ethereumMintStc);
        // create unexpected transaction record...
        UnexpectedEthereumTransaction unexpectedTxn = createUnexpectedEthereumTransaction(onChainTransaction);
        unexpectedEthereumTransactionRepository.save(unexpectedTxn);
        // Increase account transaction count?
        ethereumAccountService.confirmTransactionCountOnChain(ethereumMintStc.getAccountAddress(), ethereumMintStc.getAccountNonce());
    }

    /**
     * Remove dropped transaction to individual table.
     */
    @Transactional
    public void removeTransaction(String transactionHash) {
        AbstractEthereumTransaction transaction = assertEthereumTransactionHash(transactionHash);
        if (!AbstractEthereumTransaction.STATUS_DROPPED.equals(transaction.getStatus())) {
            throw new IllegalArgumentException("Transaction is not dropped: " + transactionHash);
        }
        DroppedEthereumTransaction droppedTransaction = new DroppedEthereumTransaction();
        BeanUtils.copyProperties(transaction, droppedTransaction);
        droppedTransaction.setUpdatedAt(System.currentTimeMillis());
        droppedTransaction.setUpdatedBy("ADMIN");
        ethereumTransactionRepository.delete(transaction);
        droppedEthereumTransactionRepository.save(droppedTransaction);
    }

    private UnexpectedEthereumTransaction createUnexpectedEthereumTransaction(Transaction onChainTransaction) {
        UnexpectedEthereumTransaction unexpectedTxn = new UnexpectedEthereumTransaction();
        unexpectedTxn.setTransactionHash(onChainTransaction.getHash());
        unexpectedTxn.setBlockHash(onChainTransaction.getBlockHash());
        unexpectedTxn.setBlockNumber(onChainTransaction.getBlockNumber());
        unexpectedTxn.setTransactionIndex(onChainTransaction.getTransactionIndex());
        unexpectedTxn.setAccountAddress(onChainTransaction.getFrom());
        unexpectedTxn.setAccountNonce(onChainTransaction.getNonce());
        unexpectedTxn.setRecipient(onChainTransaction.getTo());
        unexpectedTxn.setValue(onChainTransaction.getValue());
        unexpectedTxn.setGasPrice(onChainTransaction.getGasPrice());
        unexpectedTxn.setGasLimit(onChainTransaction.getGas());
        unexpectedTxn.setInput(onChainTransaction.getInput());
        unexpectedTxn.setV(onChainTransaction.getV());
        unexpectedTxn.setR(onChainTransaction.getR());
        unexpectedTxn.setS(onChainTransaction.getS());
        unexpectedTxn.setCreatedAt(System.currentTimeMillis());
        unexpectedTxn.setCreatedBy("ADMIN");
        unexpectedTxn.setUpdatedAt(unexpectedTxn.getCreatedAt());
        unexpectedTxn.setUpdatedBy(unexpectedTxn.getCreatedBy());
        return unexpectedTxn;
    }

    @Transactional
    public String getMintStcHashIfTransactionExists(String mintAccount, BigInteger mintAmount,
                                                    BigInteger accountNonce, BigInteger gasPrice) {
        String encodedFunction = ethereumTransactionOnChainService.encodeMintStcFunctionCall(mintAccount, mintAmount);
        //BigInteger gasPrice = DEFAULT_GAS_PRICE;
        BigInteger gasLimit = MINT_STC_GAS_LIMIT;

        RawTransaction rawTransaction = RawTransaction.createTransaction(accountNonce,
                gasPrice, gasLimit,
                ethereumTransactionOnChainService.getMintStcContractAddress(), encodedFunction);
        // ///////////////////////////////////////
        Credentials credentials = Credentials.create(ethereumTransactionOnChainService.getSenderPrivateKey());
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, ethereumTransactionOnChainService.getChainId(), credentials);
        String txnHash = Numeric.toHexString(Hash.sha3(signedMessage));
        //String txnHash = TransactionUtils.generateTransactionHashHexEncoded(rawTransaction, getChainId(), credentials);
        LOG.debug("TransactionHash: " + txnHash);
        EthereumTransaction ethereumTransaction = ethereumTransactionRepository.findById(txnHash).orElse(null);
        if (ethereumTransaction != null) {
            return txnHash;
        }
        return null;
    }

    private EthereumMintStc doCreateMintStcTransactionAndSave(String mintAccount, BigInteger mintAmount,
                                                              String triggerEventId,
                                                              BigInteger accountNonce, BigInteger gasPrice) {
        EthereumMintStc mintStc = ethereumTransactionOnChainService.doCreateMintStcTransaction(mintAccount, mintAmount, triggerEventId, accountNonce, gasPrice);
        ethereumTransactionRepository.save(mintStc);
        ethereumTransactionRepository.flush();
        return mintStc;
    }

    private void updateMintStcTransactionSent(EthereumMintStc mintStc) {
        mintStc.sent();
        mintStc.setUpdatedBy("ADMIN");
        mintStc.setUpdatedAt(System.currentTimeMillis());
        ethereumTransactionRepository.save(mintStc);
        ethereumTransactionRepository.flush();
    }

    private EthereumMintStc assertEthereumMintStcTransactionHash(String transactionHash) {
        EthereumMintStc mintStc = (EthereumMintStc) ethereumTransactionRepository.findById(transactionHash).orElse(null);
        if (mintStc == null) {
            LOG.error("CANNOT find MintStc transaction by hash: " + transactionHash);
            throw new IllegalArgumentException("Illegal transaction hash: " + transactionHash);
        }
        return mintStc;
    }

    private AbstractEthereumTransaction assertEthereumTransactionHash(String transactionHash) {
        AbstractEthereumTransaction transaction = ethereumTransactionRepository.findById(transactionHash).orElse(null);
        if (transaction == null) {
            LOG.error("CANNOT find transaction by hash: " + transactionHash);
            throw new IllegalArgumentException("Illegal transaction hash: " + transactionHash);
        }
        return transaction;
    }
}

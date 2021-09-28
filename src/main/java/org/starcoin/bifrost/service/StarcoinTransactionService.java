package org.starcoin.bifrost.service;

import com.novi.serde.SerializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.*;
import org.starcoin.bifrost.data.repo.DroppedStarcoinTransactionRepository;
import org.starcoin.bifrost.data.repo.StarcoinTransactionRepository;
import org.starcoin.bifrost.data.repo.UnexpectedStarcoinTransactionRepository;
import org.starcoin.bifrost.utils.StarcoinOnChainUtils;

import javax.transaction.Transactional;
import java.math.BigInteger;

@Service
public class StarcoinTransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(StarcoinTransactionService.class);

    @Autowired
    private StarcoinTransactionOnChainService starcoinTransactionOnChainService;

    @Autowired
    private StarcoinTransactionRepository starcoinTransactionRepository;

    @Autowired
    private UnexpectedStarcoinTransactionRepository unexpectedStarcoinTransactionRepository;

    @Autowired
    private StarcoinAccountService starcoinAccountService;

    @Autowired
    private DroppedStarcoinTransactionRepository droppedStarcoinTransactionRepository;

    public StarcoinTransactionService() {
    }

    @Transactional
    public boolean isSourceEventTriggered(String eventId) {
        AbstractStarcoinTransaction transaction = starcoinTransactionRepository.findFirstByTriggerEventId(eventId);
        return transaction != null;
    }

    @Transactional
    public StarcoinDepositStc createDepositStcTransactionSent(String depositAccount, BigInteger depositAmount,
                                                              String fromAccount, BigInteger fromChain,
                                                              String triggerEventId,
                                                              BigInteger gasPrice, Long expirationTimestampSecs) throws SerializationError {
        BigInteger accountSeqNumber = getAccountSequenceNumberAndIncrease();
        StarcoinDepositStc depositStc = doCreateDepositStcTransactionAndSave(depositAccount, depositAmount,
                fromAccount, fromChain,
                triggerEventId, accountSeqNumber, gasPrice, expirationTimestampSecs);

        updateDepositStcTransactionSent(depositStc);
        return depositStc;
    }

    @Transactional
    public String createDepositStcTransaction(String depositAccount, BigInteger depositAmount,
                                              String fromAccount, BigInteger fromChain,
                                              String triggerEventId, BigInteger gasPrice,
                                              Long expirationTimestampSecs) throws SerializationError {
        BigInteger accountSeqNumber = getAccountSequenceNumberAndIncrease();
        StarcoinDepositStc depositStc = doCreateDepositStcTransactionAndSave(depositAccount, depositAmount,
                fromAccount, fromChain,
                triggerEventId, accountSeqNumber, gasPrice, expirationTimestampSecs);
        return depositStc.getTransactionHash();
    }

    /**
     * This mothod is only for TEST!
     */
    @Transactional
    public String createDepositStcTransaction(String depositAccount, BigInteger depositAmount,
                                              String fromAccount, BigInteger fromChain,
                                              String triggerEventId, BigInteger gasPrice,
                                              Long expirationTimestampSecs, BigInteger accountSeqNumber) {
        StarcoinDepositStc depositStc = null;
        try {
            depositStc = doCreateDepositStcTransactionAndSave(depositAccount, depositAmount,
                    fromAccount, fromChain,
                    triggerEventId, accountSeqNumber, gasPrice, expirationTimestampSecs);
        } catch (SerializationError error) {
            LOG.error("Create transaction SerializationError.", error);
            throw new RuntimeException(error);
        }
        return depositStc.getTransactionHash();
    }

    @Transactional
    public StarcoinDepositStc updateDepositTransactionStatusSent(String transactionHash) {
        StarcoinDepositStc depositStc = assertStarcoinDepositStcTransactionHash(transactionHash);
        updateDepositStcTransactionSent(depositStc);
        return depositStc;
    }

    private StarcoinDepositStc assertStarcoinDepositStcTransactionHash(String transactionHash) {
        StarcoinDepositStc depositStc = (StarcoinDepositStc) starcoinTransactionRepository.findById(transactionHash).orElse(null);
        if (depositStc == null) {
            LOG.error("CANNOT find DepositSTC transaction by hash: " + transactionHash);
            throw new IllegalArgumentException("Illegal transaction hash: " + transactionHash);
        }
        return depositStc;
    }

    private void updateDepositStcTransactionSent(StarcoinDepositStc depositStc) {
        depositStc.sent();
        depositStc.setUpdatedBy("ADMIN");
        depositStc.setUpdatedAt(System.currentTimeMillis());
        starcoinTransactionRepository.save(depositStc);
        starcoinTransactionRepository.flush();
    }

    private StarcoinDepositStc doCreateDepositStcTransactionAndSave(String depositAccount, BigInteger depositAmount,
                                                                    String fromAccount, BigInteger fromChain,
                                                                    String triggerEventId,
                                                                    BigInteger accountSeqNumber, BigInteger gasPrice,
                                                                    Long expirationTimestampSecs) throws SerializationError {
        StarcoinDepositStc depositStc = starcoinTransactionOnChainService.doCreateDepositStcTransaction(
                depositAccount, depositAmount, fromAccount, fromChain, triggerEventId,
                accountSeqNumber, gasPrice, expirationTimestampSecs);
        starcoinTransactionRepository.save(depositStc);
        starcoinTransactionRepository.flush();
        return depositStc;
    }

    public BigInteger getAccountSequenceNumberAndIncrease() {
        return starcoinAccountService.getSequenceNumberAndIncrease(starcoinTransactionOnChainService.getSenderAddress());
    }

    @Transactional // using local transaction??
    public void dropTransactionBecauseOfUnexpectedTransaction(String transactionHash, String unexpectedTransactionHash) {
        StarcoinOnChainUtils.OnChainTransaction onChainTransaction = starcoinTransactionOnChainService.getOnChainTransaction(unexpectedTransactionHash);
        StarcoinDepositStc depositStc = assertStarcoinDepositStcTransactionHash(transactionHash);
        depositStc.dropped(unexpectedTransactionHash, onChainTransaction.user_transaction.raw_txn.sender,
                new BigInteger(onChainTransaction.user_transaction.raw_txn.sequence_number));
        depositStc.setUpdatedBy("ADMIN");
        depositStc.setUpdatedAt(System.currentTimeMillis());
        starcoinTransactionRepository.save(depositStc);
        // create unexpected transaction record...
        UnexpectedStarcoinTransaction unexpectedTxn = createUnexpectedStarcoinTransaction(onChainTransaction);
        unexpectedStarcoinTransactionRepository.save(unexpectedTxn);
        // Increase account sequence number
        starcoinAccountService.confirmSequenceNumberOnChain(depositStc.getAccountAddress(), depositStc.getAccountSequenceNumber());
    }

    private UnexpectedStarcoinTransaction createUnexpectedStarcoinTransaction(StarcoinOnChainUtils.OnChainTransaction onChainTransaction) {
        UnexpectedStarcoinTransaction unexpectedTxn = new UnexpectedStarcoinTransaction();
        unexpectedTxn.setTransactionHash(onChainTransaction.transaction_hash);
        unexpectedTxn.setBlockHash(onChainTransaction.block_hash);
        unexpectedTxn.setBlockNumber(new BigInteger(onChainTransaction.block_number));
        unexpectedTxn.setTransactionIndex(BigInteger.valueOf(onChainTransaction.transaction_index));
        unexpectedTxn.setAccountAddress(onChainTransaction.user_transaction.raw_txn.sender);
        unexpectedTxn.setAccountSequenceNumber(new BigInteger(onChainTransaction.user_transaction.raw_txn.sequence_number));
        //unexpectedTxn.setRecipient(onChainTransaction.getTo());
        //unexpectedTxn.setValue(onChainTransaction.getValue());
        //unexpectedTxn.setGasPrice(onChainTransaction.getGasPrice());
        //unexpectedTxn.setGasLimit(onChainTransaction.getGas());
        unexpectedTxn.setPayload(onChainTransaction.user_transaction.raw_txn.payload);
        //unexpectedTxn.setV(onChainTransaction.getV());
        //unexpectedTxn.setR(onChainTransaction.getR());
        //unexpectedTxn.setS(onChainTransaction.getS());
        unexpectedTxn.setCreatedAt(System.currentTimeMillis());
        unexpectedTxn.setCreatedBy("ADMIN");
        unexpectedTxn.setUpdatedAt(unexpectedTxn.getCreatedAt());
        unexpectedTxn.setUpdatedBy(unexpectedTxn.getCreatedBy());
        return unexpectedTxn;
    }

    public void removeTransaction(String transactionHash) {
        AbstractStarcoinTransaction transaction = assertStarcoinTransactionHash(transactionHash);
        if (!Transaction.STATUS_DROPPED.equals(transaction.getStatus())) {
            throw new IllegalArgumentException("Transaction is not dropped: " + transactionHash);
        }
        DroppedStarcoinTransaction droppedTransaction = new DroppedStarcoinTransaction();
        BeanUtils.copyProperties(transaction, droppedTransaction);
        droppedTransaction.setUpdatedAt(System.currentTimeMillis());
        droppedTransaction.setUpdatedBy("ADMIN");
        starcoinTransactionRepository.delete(transaction);
        droppedStarcoinTransactionRepository.save(droppedTransaction);
    }

    private AbstractStarcoinTransaction assertStarcoinTransactionHash(String transactionHash) {
        AbstractStarcoinTransaction transaction = starcoinTransactionRepository.findById(transactionHash).orElse(null);
        if (transaction == null) {
            LOG.error("CANNOT find transaction by hash: " + transactionHash);
            throw new IllegalArgumentException("Illegal transaction hash: " + transactionHash);
        }
        return transaction;
    }
}

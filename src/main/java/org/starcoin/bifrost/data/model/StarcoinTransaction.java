package org.starcoin.bifrost.data.model;

import java.math.BigInteger;

public interface StarcoinTransaction extends Transaction {

    String getTransactionHash();

    void setTransactionHash(String transactionHash);

    String getBlockHash();

    void setBlockHash(String blockHash);

    BigInteger getBlockNumber();

    void setBlockNumber(BigInteger blockNumber);

    BigInteger getTransactionIndex();

    void setTransactionIndex(BigInteger transactionIndex);

//    String getTriggerEventId();
//
//    void setTriggerEventId(String triggerEventId);

    String getAccountAddress();

    void setAccountAddress(String accountAddress);

    BigInteger getAccountSequenceNumber();

    void setAccountSequenceNumber(BigInteger accountSequenceNumber);

}

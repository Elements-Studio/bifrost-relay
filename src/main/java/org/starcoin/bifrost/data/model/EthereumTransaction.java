package org.starcoin.bifrost.data.model;

import java.math.BigInteger;

public interface EthereumTransaction extends Transaction {

    String getAccountAddress();

    void setAccountAddress(String accountAddress);

    BigInteger getAccountNonce();

    void setAccountNonce(BigInteger accountNonce);

    String getRecipient();

    void setRecipient(String recipient);

    String getBlockHash();

    void setBlockHash(String blockHash);

    BigInteger getBlockNumber();

    void setBlockNumber(BigInteger blockNumber);

    BigInteger getGasPrice();

    void setGasPrice(BigInteger gasPrice);

    BigInteger getGasLimit();

    void setGasLimit(BigInteger gasLimit);

    BigInteger getTransactionIndex();

    void setTransactionIndex(BigInteger transactionIndex);

    String getType();

    void setType(String type);

    String getInput();

    void setInput(String input);

    BigInteger getValue();

    void setValue(BigInteger value);

    Long getV();

    void setV(Long v);

    String getR();

    void setR(String r);

    String getS();

    void setS(String s);

    String getTransactionHash();

    void setTransactionHash(String transactionHash);

    String getCreatedBy();

    void setCreatedBy(String createdBy);

    String getUpdatedBy();

    void setUpdatedBy(String updatedBy);

    Long getCreatedAt();

    void setCreatedAt(Long createdAt);

    Long getUpdatedAt();

    void setUpdatedAt(Long updatedAt);

    //    String getMintAccount();
    //
    //    void setMintAccount(String mintAccount);
    //
    //    BigInteger getMintAmount();
    //
    //    void setMintAmount(BigInteger mintAmount);

}

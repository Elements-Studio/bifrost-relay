package org.starcoin.bifrost.data.model;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigInteger;

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "unexpected_ethereum_transaction")
public class UnexpectedEthereumTransaction implements EthereumTransaction {

    @Id
    @Column(length = 66)
    private String transactionHash;

//    @Column(name = "trigger_event_id", length = 66, nullable = false)
//    private String triggerEventId;

    /**
     * recipient.
     */
    @Column(length = 42)
    private String recipient;

    @Column(length = 66)
    private String blockHash;

    @Column(precision = 50, scale = 0)
    private BigInteger blockNumber;

    @Column(precision = 50, scale = 0)
    private BigInteger gasPrice;

    @Column(precision = 50, scale = 0)
    private BigInteger gasLimit;

    @Column(precision = 50, scale = 0)
    private BigInteger transactionIndex;

    @Column
    private String type;

    /**
     * payload.
     */
    @Column(length = 1000)
    private String input;

    /**
     * amount(value in wei).
     */
    @Column(precision = 50, scale = 0)
    private BigInteger value; //String or BigInteger???

    @Column
    private Long v;

    @Column(length = 66)
    private String r;

    @Column(length = 66)
    private String s;

//    @Column(length = 20, nullable = false)
//    private String status;

    @Column(length = 70, nullable = false)
    private String createdBy;

    @Column(length = 70, nullable = false)
    private String updatedBy;

    @Column(nullable = false)
    private Long createdAt;

    @Column(nullable = false)
    private Long updatedAt;

    @Column(name = "account_address", length = 42)
    private String accountAddress;

    @Column(name = "account_nonce", precision = 50, scale = 0, nullable = false)
    private BigInteger accountNonce;

    // ///////////////////////////////////////
    // subclass "Mint STC" properties.
    //    @Column(length = 42)
    //    private String mintAccount;
    //
    //    @Column(precision = 21, scale = 0)
    //    private BigInteger mintAmount;

    @Override
    public String getTransactionHash() {
        return transactionHash;
    }

    @Override
    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    @Override
    public String getRecipient() {
        return recipient;
    }

    @Override
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public String getBlockHash() {
        return blockHash;
    }

    @Override
    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    @Override
    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    @Override
    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    @Override
    public BigInteger getGasPrice() {
        return gasPrice;
    }

    @Override
    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    @Override
    public BigInteger getGasLimit() {
        return gasLimit;
    }

    @Override
    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    @Override
    public BigInteger getTransactionIndex() {
        return transactionIndex;
    }

    @Override
    public void setTransactionIndex(BigInteger transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getInput() {
        return input;
    }

    @Override
    public void setInput(String input) {
        this.input = input;
        // todo Is this OK?
        if (this.input.length() > 1000) {
            this.input = this.input.substring(0, 1000);
        }
    }

    @Override
    public BigInteger getValue() {
        return value;
    }

    @Override
    public void setValue(BigInteger value) {
        this.value = value;
    }

    @Override
    public Long getV() {
        return v;
    }

    @Override
    public void setV(Long v) {
        this.v = v;
    }

    @Override
    public String getR() {
        return r;
    }

    @Override
    public void setR(String r) {
        this.r = r;
    }

    @Override
    public String getS() {
        return s;
    }

    @Override
    public void setS(String s) {
        this.s = s;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public Long getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Long getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String getAccountAddress() {
        return accountAddress;
    }

    @Override
    public void setAccountAddress(String accountAddress) {
        this.accountAddress = accountAddress;
    }

    @Override
    public BigInteger getAccountNonce() {
        return accountNonce;
    }

    @Override
    public void setAccountNonce(BigInteger accountNonce) {
        this.accountNonce = accountNonce;
    }


}

package org.starcoin.bifrost.data.model;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "dropped_starcoin_transaction")
public class DroppedStarcoinTransaction implements StarcoinTransaction {

    @Id
    @Column(length = 66)
    private String transactionHash;

    @Column(length = 66)
    private String blockHash;

    @Column(precision = 50, scale = 0)
    private BigInteger blockNumber;

    @Column(precision = 50, scale = 0)
    private BigInteger transactionIndex;

    @Column(name = "trigger_event_id", length = 66, nullable = false)
    private String triggerEventId;

    @Column(name = "account_address", length = 42)
    private String accountAddress;

    @Column(name = "account_sequence_number", precision = 50, scale = 0, nullable = false)
    private BigInteger accountSequenceNumber;

    /**
     * payload.
     */
    @Column(length = 1000)
    private String payload;

    @Column(length = 66)
    private String accountPublicKey; //0x3517cf661eb9ec48ad86639db66ea463b871b7d10c52bb37461570aef68f8c36

    @Column(precision = 50, scale = 0, nullable = false)
    private BigInteger gasUnitPrice;

    @Column(precision = 50, scale = 0, nullable = false)
    private BigInteger maxGasAmount;

    @Column(precision = 50, scale = 0)
    private BigInteger gasUsed;

    @Column(length = 200)
    private String gasTokenCode;// "0x1::STC::STC",

    @Column(nullable = false)
    private Long expirationTimestampSecs;

    // ///////////////////////////////////////
    // subclass "Withdraw STC from Ethereum" properties.

    @Column(length = 42)
    private String depositAccount;

    @Column(precision = 50, scale = 0)
    private BigInteger depositAmount;


    @Column(length = 42)//, nullable = false)
    private String fromAccount;

    @Column
    private BigInteger fromChain;

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public BigInteger getFromChain() {
        return fromChain;
    }

    public void setFromChain(BigInteger fromChain) {
        this.fromChain = fromChain;
    }

    // ///////////////////////////////////////

    /**
     * if dropped, this unexpected transaction took place of it.
     */
    @Column(length = 66)
    private String unexpectedTransactionHash;

    @Version
    private Long version;

//    @Column(name = "status", length = 20, nullable = false)
//    private String status = STATUS_CREATED;

//    @Transient
//    private Object rawTransaction;
//
//    @Transient
//    private Object credentials;
//
//    @Transient
//    private byte[] signedMessage;


    @Column(length = 70, nullable = false)
    private String createdBy;

    @Column(length = 70, nullable = false)
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Override
    public String getTransactionHash() {
        return transactionHash;
    }

    @Override
    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
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
    public BigInteger getTransactionIndex() {
        return transactionIndex;
    }

    @Override
    public void setTransactionIndex(BigInteger transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getTriggerEventId() {
        return triggerEventId;
    }

    public void setTriggerEventId(String triggerEventId) {
        this.triggerEventId = triggerEventId;
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
    public BigInteger getAccountSequenceNumber() {
        return accountSequenceNumber;
    }

    @Override
    public void setAccountSequenceNumber(BigInteger accountSequenceNumber) {
        this.accountSequenceNumber = accountSequenceNumber;
    }

    public String getAccountPublicKey() {
        return accountPublicKey;
    }

    public void setAccountPublicKey(String accountPublicKey) {
        this.accountPublicKey = accountPublicKey;
    }

    public String getDepositAccount() {
        return depositAccount;
    }

    public void setDepositAccount(String depositAccount) {
        this.depositAccount = depositAccount;
    }

    public BigInteger getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigInteger depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getUnexpectedTransactionHash() {
        return unexpectedTransactionHash;
    }

    public void setUnexpectedTransactionHash(String unexpectedTransactionHash) {
        this.unexpectedTransactionHash = unexpectedTransactionHash;
    }

    public Long getExpirationTimestampSecs() {
        return expirationTimestampSecs;
    }

    public void setExpirationTimestampSecs(Long expirationTimestampSecs) {
        this.expirationTimestampSecs = expirationTimestampSecs;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public BigInteger getGasUnitPrice() {
        return gasUnitPrice;
    }

    public void setGasUnitPrice(BigInteger gasUnitPrice) {
        this.gasUnitPrice = gasUnitPrice;
    }

    public BigInteger getMaxGasAmount() {
        return maxGasAmount;
    }

    public void setMaxGasAmount(BigInteger maxGasAmount) {
        this.maxGasAmount = maxGasAmount;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getGasTokenCode() {
        return gasTokenCode;
    }

    public void setGasTokenCode(String gasTokenCode) {
        this.gasTokenCode = gasTokenCode;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}

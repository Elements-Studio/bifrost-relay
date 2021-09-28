package org.starcoin.bifrost.data.model;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.math.BigInteger;

@Entity
@DynamicInsert
@DynamicUpdate
public class EthereumAccount {

    @Id
    @Column(length = 42) //0x414d6e7B82E9Ce949d468d502057483195C69df9
    private String address;

    /**
     * Count of transactions which have receipts.
     */
    @Column(precision = 50, scale = 0, nullable = false)
    private BigInteger confirmedTransactionCount;

    @Column(precision = 50, scale = 0, nullable = false)
    private BigInteger transactionCount;

    @Column(length = 70, nullable = false)
    private String createdBy;

    @Column(length = 70, nullable = false)
    private String updatedBy;

    @Column(nullable = false)
    private Long createdAt;

    @Column(nullable = false)
    private Long updatedAt;

    @Version
    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigInteger getConfirmedTransactionCount() {
        return confirmedTransactionCount;
    }

    public void setConfirmedTransactionCount(BigInteger confirmedTransactionCount) {
        this.confirmedTransactionCount = confirmedTransactionCount;
    }

    public BigInteger getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(BigInteger transactionCount) {
        this.transactionCount = transactionCount;
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

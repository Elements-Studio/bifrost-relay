package org.starcoin.bifrost.data.model;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.starcoin.bifrost.DomainError;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.List;

import static org.starcoin.bifrost.DomainError.INVALID_STATUS;

@Entity
@DynamicInsert
@DynamicUpdate
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "log_type")
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "UniqueEthereumLog", columnNames = {"block_hash", "transaction_index", "log_index"})
}, indexes = {
        @Index(name = "EthLogStatusCreatedAt", columnList = "status, created_at"),
        @Index(name = "EthLogStatusUpdatedAt", columnList = "status, updated_at")
})
public abstract class EthereumLog {

    public static final String STATUS_CREATED = "CREATED";

    public static final String STATUS_CONFIRMED = "CONFIRMED";

    public static final String STATUS_DROPPED = "DROPPED";

    @Id
    @Column(length = 66, nullable = false)
    private String logId;

    @Column(length = 42, nullable = false)
    private String address;

    @Column(name = "block_hash", length = 66, nullable = false)
    private String blockHash;

    @Column(precision = 50, scale = 0)
    private BigInteger blockNumber;

    @Column(length = 500)
    private String data;

    @Column(name = "log_index", precision = 50, scale = 0)
    private BigInteger logIndex;

    @Column(length = 66, nullable = false)
    private String transactionHash;

    @Column(name = "transaction_index", precision = 50, scale = 0)
    private BigInteger transactionIndex;

    @Transient
    private List<String> topics;

    @Column(name = "status", length = 20, nullable = false)
    private String status = STATUS_CREATED;

    @Column(length = 70, nullable = false)
    private String createdBy;

    @Column(length = 70, nullable = false)
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Version
    private Long version;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public BigInteger getLogIndex() {
        return logIndex;
    }

    public void setLogIndex(BigInteger logIndex) {
        this.logIndex = logIndex;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public BigInteger getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(BigInteger transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public String getStatus() {
        return status;
    }

    protected void setStatus(String status) {
        this.status = status;
    }

    public void confirmed() {
        if (!STATUS_CREATED.equals(this.status)) {
            throw DomainError.named(INVALID_STATUS,
                    "Can not confirm, invalid status of log '%1$s', '%2$s'.", this.logId, this.status);
        }
        this.setStatus(STATUS_CONFIRMED);
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "EthereumLog{" +
                "logId='" + logId + '\'' +
                ", address='" + address + '\'' +
                ", blockHash='" + blockHash + '\'' +
                ", blockNumber=" + blockNumber +
                ", data='" + data + '\'' +
                ", logIndex=" + logIndex +
                ", transactionHash='" + transactionHash + '\'' +
                ", transactionIndex='" + transactionIndex + '\'' +
                ", topics=" + topics +
                ", status='" + status + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}

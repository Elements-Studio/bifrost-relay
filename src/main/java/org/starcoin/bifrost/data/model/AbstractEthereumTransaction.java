package org.starcoin.bifrost.data.model;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.starcoin.bifrost.DomainError;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Arrays;

import static org.starcoin.bifrost.DomainError.INVALID_STATUS;

@Entity
@DynamicInsert
@DynamicUpdate
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transaction_type")
@Table(name = "ethereum_transaction", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueAccountAddressNonce", columnNames = {"account_address", "account_nonce"}),
        @UniqueConstraint(name = "UniqueTrxTypeTriggerEvent", columnNames = {"transaction_type", "trigger_event_id"})
}, indexes = {
        @Index(name = "EthTrxStatusCreatedAt", columnList = "status, created_at"),
        @Index(name = "EthTrxStatusUpdatedAt", columnList = "status, updated_at")
})
public abstract class AbstractEthereumTransaction implements EthereumTransaction {

    /**
     * {
     *   blockHash: '0xfff9837150294ff0f4434dc907a89be3cb4874bc624f4914ebdcb4948979e3c6',
     *   blockNumber: 12997369,
     *   from: '0x9696f59E4d72E237BE84fFD425DCaD154Bf96976',
     *   gas: 207128,
     *   gasPrice: '60000000000',
     *   hash: '0x12dd62179190b59143097696caaf1c3d1bc0302671a0b3b9f6a30b2cdc0bb0e2',
     *   input: '0x',
     *   nonce: 758453,
     *   r: '0x20e407243c5f625496a778fb7b2ae5a1d0b9048c364c412b34ef42f5a50cbc09',
     *   s: '0x4941e3537685e552d0e9a870b57484a0999797543b388e572e220837df5096f3',
     *   to: '0xc673cc127D463b0C8091336a3bdfbce88c738C5A',
     *   transactionIndex: 0,
     *   type: 0,
     *   v: '0x26',
     *   value: '206290000000000000'
     * }
     */

    /**
     * The constituents of signedTransaction are
     * <p>
     * nonce
     * gas price
     * gas limit
     * to
     * value in wei
     * data
     * ecdsaV
     * ecdsaR
     * ecdsaS
     */

    @Id
    @Column(length = 66)
    private String transactionHash;

    @Column(name = "trigger_event_id", length = 66, nullable = false)
    private String triggerEventId;

    /**
     * recipient.
     */
    @Column(length = 42)
    private String recipient;

    @Column(length = 66)
    private String blockHash;

    @Column(precision = 50, scale = 0)
    private BigInteger blockNumber;

    /**
     * gas price in wei.
     */
    @Column(precision = 50, scale = 0, nullable = false)
    private BigInteger gasPrice;

    @Column(precision = 50, scale = 0, nullable = false)
    private BigInteger gasLimit;

    @Column(precision = 50, scale = 0)
    private BigInteger gasUsed;

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
    @Column(precision = 50, scale = 0, nullable = true)
    private BigInteger value = BigInteger.ZERO; //String or BigInteger???

    @Column
    private Long v;

    @Column(length = 66)
    private String r; // 0x37a376dfd31377348ceb2f87f0aecefe44b9fb060f12a37185012df5fab7ad6d

    @Column(length = 66)
    private String s; // 0x2a72adfb545f449e90498d2d82be4e11d17686beb1b280bec01b082aec47602c

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

    @Column(name = "account_address", length = 42)
    private String accountAddress;

    @Column(name = "account_nonce", precision = 50, scale = 0, nullable = false)
    private BigInteger accountNonce;

    // ///////////////////////////////////////
    // subclass "Mint STC" properties.

    @Column(length = 42)
    private String mintAccount;

    @Column(precision = 50, scale = 0)
    private BigInteger mintAmount;

    // ///////////////////////////////////////

    /**
     * if dropped, this unexpected transaction took place of it.
     */
    @Column(length = 66)
    private String unexpectedTransactionHash;

    @Version
    private Long version;

    @Transient
    private Object rawTransaction;

    @Transient
    private Object credentials;

    @Transient
    private byte[] signedMessage;

    public byte[] getSignedMessage() {
        return signedMessage;
    }

    public void setSignedMessage(byte[] signedMessage) {
        this.signedMessage = signedMessage;
    }

    public Object getCredentials() {
        return credentials;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

    public Object getRawTransaction() {
        return rawTransaction;
    }

    public void setRawTransaction(Object rawTransaction) {
        this.rawTransaction = rawTransaction;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }


    @Override
    public String getTransactionHash() {
        return transactionHash;
    }

    @Override
    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
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

    public String getStatus() {
        return status;
    }

    protected void setStatus(String status) {
        this.status = status;
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

    public String getTriggerEventId() {
        return triggerEventId;
    }

    public void setTriggerEventId(String triggerEventId) {
        this.triggerEventId = triggerEventId;
    }

    public String getMintAccount() {
        return mintAccount;
    }

    public void setMintAccount(String mintAccount) {
        this.mintAccount = mintAccount;
    }

    public BigInteger getMintAmount() {
        return mintAmount;
    }

    public void setMintAmount(BigInteger mintAmount) {
        this.mintAmount = mintAmount;
    }

    public String getUnexpectedTransactionHash() {
        return unexpectedTransactionHash;
    }

    public void setUnexpectedTransactionHash(String unexpectedTransactionHash) {
        this.unexpectedTransactionHash = unexpectedTransactionHash;
    }

    public void sent() {
        if (!STATUS_CREATED.equals(this.status) && !STATUS_SENT.equals(this.status)) {
            throw DomainError.named(INVALID_STATUS,
                    "Can not send, invalid status of transaction '%1$s', '%2$s'.", this.transactionHash, this.status);
        }
        this.setStatus(STATUS_SENT);
    }

    public void confirmed() {
        if (!STATUS_SENT.equals(this.status)) {
            throw DomainError.named(INVALID_STATUS,
                    "Can not confirm, invalid status of transaction '%1$s', '%2$s'.", this.transactionHash, this.status);
        }
        this.setStatus(STATUS_CONFIRMED);
    }

    public void dropped(String unexpectedTransactionHash, String accountAddress, BigInteger accountNonce) {
        if (!STATUS_CREATED.equals(this.status) && !STATUS_SENT.equals(this.status)) {
            throw DomainError.named(INVALID_STATUS,
                    "Cannot drop, invalid status of transaction '%1$s', '%2$s'.", this.transactionHash, this.status);
        }
        if (!(this.getAccountAddress().equalsIgnoreCase(accountAddress)
                && this.getAccountNonce().compareTo(accountNonce) == 0)) {
            throw new RuntimeException(String.format("The account address must be '%1$s', nonce must be '%2$s'.",
                    this.getAccountAddress(), this.getAccountNonce()));
        }
        if (this.getTransactionHash().equals(unexpectedTransactionHash)) {
            throw new RuntimeException("Illegal same unexpected transaction hash: " + unexpectedTransactionHash);
        }
        this.setUnexpectedTransactionHash(unexpectedTransactionHash);
        this.setStatus(STATUS_DROPPED);
    }

    /**
     * Cancel transaction(not sent).
     */
    public void canceled() {
        if (!STATUS_CREATED.equals(this.status)) {
            throw DomainError.named(INVALID_STATUS,
                    "Can not cancel, invalid status of transaction '%1$s', '%2$s'.", this.transactionHash, this.status);
        }
        this.setStatus(STATUS_CANCELED);
    }


    /**
     * Set the transaction immovable, stay in the table forever.
     */
    public void tombstoned() {
        if (!STATUS_SENT.equals(this.status)) {
            throw DomainError.named(INVALID_STATUS,
                    "Can not tombstone, invalid status of transaction '%1$s', '%2$s'.", this.transactionHash, this.status);
        }
        this.setStatus(STATUS_TOMBSTONED);
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    @Override
    public String toString() {
        return "AbstractEthereumTransaction{" +
                "transactionHash='" + transactionHash + '\'' +
                ", triggerEventId='" + triggerEventId + '\'' +
                ", recipient='" + recipient + '\'' +
                ", blockHash='" + blockHash + '\'' +
                ", blockNumber=" + blockNumber +
                ", gasPrice=" + gasPrice +
                ", gasLimit=" + gasLimit +
                ", gasUsed=" + gasUsed +
                ", transactionIndex=" + transactionIndex +
                ", type='" + type + '\'' +
                ", input='" + input + '\'' +
                ", value=" + value +
                ", v=" + v +
                ", r='" + r + '\'' +
                ", s='" + s + '\'' +
                ", status='" + status + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", accountAddress='" + accountAddress + '\'' +
                ", accountNonce=" + accountNonce +
                ", mintAccount='" + mintAccount + '\'' +
                ", mintAmount=" + mintAmount +
                ", unexpectedTransactionHash='" + unexpectedTransactionHash + '\'' +
                ", version=" + version +
                ", rawTransaction=" + rawTransaction +
                ", credentials=" + credentials +
                ", signedMessage=" + Arrays.toString(signedMessage) +
                '}';
    }
}

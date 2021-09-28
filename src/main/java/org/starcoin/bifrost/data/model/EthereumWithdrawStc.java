package org.starcoin.bifrost.data.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.math.BigInteger;

@Entity
@DiscriminatorValue("WITHDRAW_STC")
public class EthereumWithdrawStc extends EthereumLog {

    @Column(precision = 50, scale = 0, nullable = false)
    private BigInteger withdrawAmount;

    @Column(length = 42, nullable = false)
    private String fromAccount;

    /**
     * recipient.
     */
    @Column(length = 42, nullable = false)//0x4D19e39322e77b0584BfF62f6cd1F5DB
    private String toAccount;

    @Column(length = 42, nullable = false)
    private String ownerAccount;

    @Column
    private BigInteger fromChain;

//    @Column(precision = 50, scale = 0, nullable = false)
//    private BigInteger depositAmount;

//    @Column(precision = 50, scale = 0, nullable = false)
//    private BigInteger gasPriceInNanoStc;

//    @Column(precision = 50, scale = 0, nullable = false)
//    private BigInteger estimatedGas;

//    @Column(precision = 50, scale = 10, nullable = false)
//    private BigDecimal weiToNanoStcExchangeRate;


    public BigInteger getWithdrawAmount() {
        return withdrawAmount;
    }

    public void setWithdrawAmount(BigInteger withdrawAmount) {
        this.withdrawAmount = withdrawAmount;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public String getOwnerAccount() {
        return ownerAccount;
    }

    public void setOwnerAccount(String ownerAccount) {
        this.ownerAccount = ownerAccount;
    }

    public BigInteger getFromChain() {
        return fromChain;
    }

    public void setFromChain(BigInteger fromChain) {
        this.fromChain = fromChain;
    }

    @Override
    public String toString() {
        return "EthereumWithdrawStc{" +
                "withdrawAmount=" + withdrawAmount +
                ", fromAccount='" + fromAccount + '\'' +
                ", toAccount='" + toAccount + '\'' +
                ", ownerAccount='" + ownerAccount + '\'' +
                ", fromChain=" + fromChain +
                '}' +
                " is " + super.toString();
    }
}

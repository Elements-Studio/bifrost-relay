package org.starcoin.bifrost.data.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.math.BigInteger;

@Entity
@DiscriminatorValue("STC_TO_ETHEREUM")
public class StcToEthereum extends StarcoinEvent {

    @Column(precision = 50, scale = 0)
    private BigInteger mintAmount;

    /**
     * recipient.
     */
    @Column(length = 42, nullable = false)
    private String mintAccount;

    @Column(precision = 50, scale = 0, nullable = false)
    private BigInteger depositAmount;

    @Column(precision = 50, scale = 0)
    private BigInteger gasPriceInWei;

    @Column(precision = 50, scale = 0)
    private BigInteger estimatedGas;

    @Column(precision = 50, scale = 10)
    private BigDecimal weiToNanoStcExchangeRate;

    public BigInteger getMintAmount() {
        return mintAmount;
    }

    public void setMintAmount(BigInteger mintAmount) {
        this.mintAmount = mintAmount;
    }

    public String getMintAccount() {
        return mintAccount;
    }

    public void setMintAccount(String mintAccount) {
        this.mintAccount = mintAccount;
    }

    public BigInteger getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigInteger depositAmount) {
        this.depositAmount = depositAmount;
    }

    public BigInteger getGasPriceInWei() {
        return gasPriceInWei;
    }

    public void setGasPriceInWei(BigInteger gasPriceInWei) {
        this.gasPriceInWei = gasPriceInWei;
    }

    public BigInteger getEstimatedGas() {
        return estimatedGas;
    }

    public void setEstimatedGas(BigInteger estimatedGas) {
        this.estimatedGas = estimatedGas;
    }

    public BigDecimal getWeiToNanoStcExchangeRate() {
        return weiToNanoStcExchangeRate;
    }

    public void setWeiToNanoStcExchangeRate(BigDecimal weiToNanoStcExchangeRate) {
        this.weiToNanoStcExchangeRate = weiToNanoStcExchangeRate;
    }

    @Override
    public String toString() {
        return "StcToEthereum{" +
                "mintAmount=" + mintAmount +
                ", mintAccount='" + mintAccount + '\'' +
                ", depositAmount=" + depositAmount +
                ", gasPriceInWei=" + gasPriceInWei +
                ", estimatedGas=" + estimatedGas +
                ", weiToNanoStcExchangeRate=" + weiToNanoStcExchangeRate +
                '}' +
                " is " + super.toString();
    }
}

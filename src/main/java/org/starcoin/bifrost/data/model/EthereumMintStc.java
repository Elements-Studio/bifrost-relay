package org.starcoin.bifrost.data.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("MINT_STC")
public class EthereumMintStc extends AbstractEthereumTransaction {

    @Override
    public String toString() {
        return "EthereumMintStc{}" +
                " is " + super.toString();
    }

}

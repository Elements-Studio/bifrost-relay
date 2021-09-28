package org.starcoin.bifrost.data.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DEPOSIT_STC")
public class StarcoinDepositStc extends AbstractStarcoinTransaction{

    @Override
    public String toString() {
        return "StarcoinDepositStc{}" +
                " is " + super.toString();
    }

}

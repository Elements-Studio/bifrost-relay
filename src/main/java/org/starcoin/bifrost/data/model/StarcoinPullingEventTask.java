package org.starcoin.bifrost.data.model;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;

@Entity
@DynamicInsert
@DynamicUpdate
public class StarcoinPullingEventTask extends AbstractPullingBlockTask {

    public static final int PULLING_BLOCK_MAX_COUNT = 32;

}

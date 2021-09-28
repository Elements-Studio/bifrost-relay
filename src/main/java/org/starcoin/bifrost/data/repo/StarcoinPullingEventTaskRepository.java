package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.bifrost.data.model.StarcoinPullingEventTask;

import java.math.BigInteger;
import java.util.List;

public interface StarcoinPullingEventTaskRepository extends JpaRepository<StarcoinPullingEventTask, BigInteger> {

    List<StarcoinPullingEventTask> findByStatusEquals(String status);

}

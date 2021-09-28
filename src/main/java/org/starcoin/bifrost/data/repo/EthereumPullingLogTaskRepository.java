package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.bifrost.data.model.EthereumPullingLogTask;

import java.math.BigInteger;
import java.util.List;

public interface EthereumPullingLogTaskRepository extends JpaRepository<EthereumPullingLogTask, BigInteger> {

    List<EthereumPullingLogTask> findByStatusEquals(String status);

}

package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.starcoin.bifrost.data.model.EthereumLog;
import org.starcoin.bifrost.data.model.EthereumWithdrawStc;

import java.util.List;

public interface EthereumLogRepository extends JpaRepository<EthereumLog, String> {

    List<EthereumLog> findByStatusEquals(String status);

    List<EthereumLog> findByStatusEqualsAndCreatedAtLessThan(String status, Long createdBefore);

    @Query(value = "select e from EthereumWithdrawStc e " +
            "left join AbstractStarcoinTransaction t " +
            "on e.logId = t.triggerEventId " +
            "where t.transactionHash is null and e.status = 'CONFIRMED' and e.updatedAt < :confirmedBefore")
    List<EthereumWithdrawStc> findEthereumWithdrawStcLogsByTransactionNotExistsAndConfirmedBefore(Long confirmedBefore);

}

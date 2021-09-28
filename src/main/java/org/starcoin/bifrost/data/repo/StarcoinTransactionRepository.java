package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.bifrost.data.model.AbstractStarcoinTransaction;
import org.starcoin.bifrost.data.model.StarcoinDepositStc;

import java.math.BigInteger;
import java.util.List;

public interface StarcoinTransactionRepository extends JpaRepository<AbstractStarcoinTransaction, String> {

    List<AbstractStarcoinTransaction> findByStatusEquals(String status);

    List<AbstractStarcoinTransaction> findByStatusEqualsAndUpdatedAtLessThan(String status, Long updatedBefore);

    AbstractStarcoinTransaction findFirstByAccountAddressAndAccountSequenceNumber(String address, BigInteger sequenceNumber);

    AbstractStarcoinTransaction findFirstByTriggerEventId(String triggerEventId);

    StarcoinDepositStc findFirstStarcoinDepositStcByTriggerEventId(String triggerEventId);

    List<AbstractStarcoinTransaction> findByBlockNumberIsNullAndStatusInAndCreatedAtLessThanOrderByCreatedAt(
            String[] statuses, Long createdBefore);

    List<AbstractStarcoinTransaction> findByTransactionHashIn(String[] transactionHashes);

}

package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.bifrost.data.model.DroppedStarcoinTransaction;

import java.util.List;

public interface DroppedStarcoinTransactionRepository extends JpaRepository<DroppedStarcoinTransaction, String> {

    List<DroppedStarcoinTransaction> findByTransactionHashIn(String[] transactionHashes);

}

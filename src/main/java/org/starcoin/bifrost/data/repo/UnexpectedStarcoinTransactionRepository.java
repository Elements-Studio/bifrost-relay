package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.bifrost.data.model.UnexpectedStarcoinTransaction;

import java.util.List;

public interface UnexpectedStarcoinTransactionRepository extends JpaRepository<UnexpectedStarcoinTransaction, String> {

    List<UnexpectedStarcoinTransaction> findByTransactionHashIn(String[] transactionHashes);

}

package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.bifrost.data.model.DroppedEthereumTransaction;

import java.util.List;

public interface DroppedEthereumTransactionRepository extends JpaRepository<DroppedEthereumTransaction, String> {

    List<DroppedEthereumTransaction> findByTransactionHashIn(String[] transactionHashes);

}

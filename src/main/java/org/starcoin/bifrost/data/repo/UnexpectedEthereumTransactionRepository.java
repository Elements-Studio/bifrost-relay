package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.bifrost.data.model.UnexpectedEthereumTransaction;

import java.util.List;

public interface UnexpectedEthereumTransactionRepository extends JpaRepository<UnexpectedEthereumTransaction, String> {

    List<UnexpectedEthereumTransaction> findByTransactionHashIn(String[] transactionHashes);

}

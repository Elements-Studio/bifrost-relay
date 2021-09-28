package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.bifrost.data.model.AbstractEthereumTransaction;
import org.starcoin.bifrost.data.model.EthereumMintStc;

import java.math.BigInteger;
import java.util.List;

public interface EthereumTransactionRepository extends JpaRepository<AbstractEthereumTransaction, String> {

    List<AbstractEthereumTransaction> findByStatusEquals(String status);

    List<AbstractEthereumTransaction> findByStatusEqualsAndUpdatedAtLessThan(String status, Long updatedBefore);

    AbstractEthereumTransaction findFirstByAccountAddressAndAccountNonce(String address, BigInteger nonce);

    AbstractEthereumTransaction findFirstByTriggerEventId(String triggerEventId);

    EthereumMintStc findFirstEthereumMintStcByTriggerEventId(String triggerEventId);

    List<AbstractEthereumTransaction> findByBlockNumberIsNullAndStatusInAndCreatedAtLessThanOrderByCreatedAt(
            String[] statuses, Long createdBefore);

    List<AbstractEthereumTransaction> findByTransactionHashIn(String[] transactionHashes);

}

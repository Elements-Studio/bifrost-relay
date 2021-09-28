package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.bifrost.data.model.EthereumAccount;

public interface EthereumAccountRepository extends JpaRepository<EthereumAccount, String> {

}

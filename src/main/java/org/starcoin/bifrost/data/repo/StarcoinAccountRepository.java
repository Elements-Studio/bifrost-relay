package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.starcoin.bifrost.data.model.StarcoinAccount;

public interface StarcoinAccountRepository extends JpaRepository<StarcoinAccount, String> {

}

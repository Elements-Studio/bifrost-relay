package org.starcoin.bifrost.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.starcoin.bifrost.data.model.StarcoinEvent;
import org.starcoin.bifrost.data.model.StcToEthereum;

import java.util.List;

public interface StarcoinEventRepository extends JpaRepository<StarcoinEvent, String> {

    List<StarcoinEvent> findByStatusEquals(String status);

    List<StarcoinEvent> findByStatusEqualsAndCreatedAtLessThan(String status, Long createdBefore);

    @Query(value = "select e from StcToEthereum e " +
            "left join AbstractEthereumTransaction t " +
            "on e.eventId = t.triggerEventId " +
            "where t.transactionHash is null and e.status = 'CONFIRMED' and e.updatedAt < :confirmedBefore")
    /**
     * ' left join EthereumMintStc'
     * produce SQL like this:
     * > select stctoether0_.event_id as event_id2_3_, stctoether0_.block_hash as block_ha3_3_, stctoether0_.block_number as block_nu4_3_, stctoether0_.created_at as created_5_3_, stctoether0_.created_by as created_6_3_, stctoether0_.data as data7_3_, stctoether0_.event_key as event_ke8_3_, stctoether0_.event_sequence_number as event_se9_3_, stctoether0_.status as status10_3_, stctoether0_.transaction_hash as transac11_3_, stctoether0_.transaction_index as transac12_3_, stctoether0_.type_tag as type_ta13_3_, stctoether0_.updated_at as updated14_3_, stctoether0_.updated_by as updated15_3_, stctoether0_.version as version16_3_, stctoether0_.mint_account as mint_ac17_3_, stctoether0_.mint_amount as mint_am18_3_ from starcoin_event stctoether0_ left outer join ethereum_transaction ethereummi1_ on (stctoether0_.event_id=ethereummi1_.trigger_event_id) where stctoether0_.event_type='STC_TO_ETHEREUM' and (ethereummi1_.transaction_hash is null) and stctoether0_.updated_at<?
     * same as
     * ' left join AbstractEthereumTransaction'
     * producing:
     * > select stctoether0_.event_id as event_id2_3_, stctoether0_.block_hash as block_ha3_3_, stctoether0_.block_number as block_nu4_3_, stctoether0_.created_at as created_5_3_, stctoether0_.created_by as created_6_3_, stctoether0_.data as data7_3_, stctoether0_.event_key as event_ke8_3_, stctoether0_.event_sequence_number as event_se9_3_, stctoether0_.status as status10_3_, stctoether0_.transaction_hash as transac11_3_, stctoether0_.transaction_index as transac12_3_, stctoether0_.type_tag as type_ta13_3_, stctoether0_.updated_at as updated14_3_, stctoether0_.updated_by as updated15_3_, stctoether0_.version as version16_3_, stctoether0_.mint_account as mint_ac17_3_, stctoether0_.mint_amount as mint_am18_3_ from starcoin_event stctoether0_ left outer join ethereum_transaction abstractet1_ on (stctoether0_.event_id=abstractet1_.trigger_event_id) where stctoether0_.event_type='STC_TO_ETHEREUM' and (abstractet1_.transaction_hash is null) and stctoether0_.updated_at<?
     */
    List<StcToEthereum> findStcToEthereumEventsByTransactionNotExistsAndConfirmedBefore(Long confirmedBefore);

}

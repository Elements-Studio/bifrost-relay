package org.starcoin.bifrost;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.starcoin.bifrost.data.UnknownDataIntegrityViolationException;
import org.starcoin.bifrost.data.model.AbstractNodeHeartbeat;
import org.starcoin.bifrost.data.model.StarcoinNodeHeartbeat;
import org.starcoin.bifrost.data.model.StcToEthereum;
import org.starcoin.bifrost.data.repo.*;
import org.starcoin.bifrost.data.utils.IdUtils;
import org.starcoin.bifrost.service.*;
import org.starcoin.utils.StarcoinOnChainUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BifrostRelayApplicationTests {

    static String TEST_MINT_ACCOUNT = "0xBf5A160865B70C2047f24eEE94cD04880Ddee1e8";
    // //////////////////////////////////////////////
    static String TEST_STARCOIN_DEPOSIT_ACCOUNT = "0x569AB535990a17Ac9Afd1bc57Faec683";// this account must exist and accept token!
    static int TEST_CROSS_FROM_ETHEREUM_CHAIN_ID = 1;
    static String TEST_CROSS_FROM_ETHEREUM_ACCOUNT = "0x71DFDD2BF49E8Af5226E0078efA31ecf258bC44E";
    @Autowired
    EthereumAccountService ethereumAccountService;
    @Autowired
    EthereumTransactionService ethereumTransactionService;
    @Autowired
    EthereumTransactionOnChainService ethereumTransactionOnChainService;
    @Autowired
    EthereumTransactionServiceFacade ethereumTransactionServiceFacade;
    @Autowired
    EthereumTransactionRepository ethereumTransactionRepository;
    @Autowired
    DroppedEthereumTransactionRepository droppedEthereumTransactionRepository;
    @Autowired
    UnexpectedEthereumTransactionRepository unexpectedEthereumTransactionRepository;
    @Autowired
    StarcoinEventRepository starcoinEventRepository;
    @Autowired
    StarcoinNodeHeartbeatService starcoinNodeHeartbeatService;

//	@Test
//	void contextLoads() throws IOException {
//  }

    //    @Test
//    void testResetEthereumAccount() throws IOException {
//        //ethereumAccountService.resetTransactionCount(ethereumTransactionService.getMintStcSenderAddress());
//    }
    @Autowired
    StarcoinTransactionService starcoinTransactionService;
    @Autowired
    StarcoinTransactionOnChainService starcoinTransactionOnChainService;
    @Autowired
    TokenPriceService tokenPriceService;
    @Autowired
    StarcoinNodeHeartbeatRepository nodeHeartbeatRepository;
    // //////////////////////////////////////////////

    @Test
    @Order(1)
    void testGetExchangeRate() {
        if (true) return;
        BigDecimal weiToNanoStcExRate = tokenPriceService.getWeiToNanoStcExchangeRate();
        System.out.println("weiToNanoStcExRate: " + weiToNanoStcExRate);
    }

    @Test
    @Order(1)
    void testStarcoinTransactionService() {
        if (true) return;
        // test get on-chain information...
        //BigInteger senderSequenceNumber = starcoinTransactionOnChainService.getSenderSequenceNumber();
        BigInteger senderSequenceNumber = starcoinTransactionService.getAccountSequenceNumberAndIncrease();
        System.out.println("Get sequence number: " + senderSequenceNumber);
        BigInteger gasPrice = starcoinTransactionOnChainService.getOnChainGasPrice();
        System.out.println("Get gas price: " + gasPrice);
        BigInteger estimatedGas = starcoinTransactionOnChainService.estimateDepositStcGas(
                TEST_CROSS_FROM_ETHEREUM_ACCOUNT,
                TEST_STARCOIN_DEPOSIT_ACCOUNT,// this account must exist and accept token!
                BigInteger.valueOf(10000000000L), TEST_CROSS_FROM_ETHEREUM_CHAIN_ID);
        System.out.println("Estimated gas: " + estimatedGas);
        //if (true) return;
        Long nowSeconds = starcoinTransactionOnChainService.getNowSeconds();
        System.out.println("Now seconds: " + nowSeconds);
        //BigInteger accountSeqNumber = BigInteger.valueOf(2808);
        //Long expirationTimestampSecs = 18175L;
        Long expirationTimestampSecs = starcoinTransactionOnChainService.getNowSeconds() + 5000;
        String transactionHash_1 = starcoinTransactionService.createDepositStcTransaction(
                TEST_STARCOIN_DEPOSIT_ACCOUNT, BigInteger.valueOf(233),
                TEST_CROSS_FROM_ETHEREUM_ACCOUNT,
                BigInteger.valueOf(TEST_CROSS_FROM_ETHEREUM_CHAIN_ID),
                "TEST" + UUID.randomUUID(), gasPrice, // BigInteger.valueOf(1),
                expirationTimestampSecs, senderSequenceNumber);
        System.out.println("Starcoin transaction created: " + transactionHash_1);

        // send a transaction on chain without saving in database
        senderSequenceNumber = starcoinTransactionService.getAccountSequenceNumberAndIncrease();
        String transactionHash_2 = starcoinTransactionOnChainService.noSavingJustSendDepositStcTransaction(
                TEST_STARCOIN_DEPOSIT_ACCOUNT, BigInteger.valueOf(233333333),
                TEST_CROSS_FROM_ETHEREUM_ACCOUNT,
                BigInteger.valueOf(TEST_CROSS_FROM_ETHEREUM_CHAIN_ID),
                "TEST" + UUID.randomUUID(),
                senderSequenceNumber, gasPrice,
                expirationTimestampSecs);
        System.out.println("Starcoin transaction sent: " + transactionHash_2);
        String transactionBlockHash_2 = null;
        while (transactionBlockHash_2 == null) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            System.out.println("Waiting transaction '" + transactionHash_2 + "' in block.");
            StarcoinOnChainUtils.OnChainTransaction onChainTransaction = starcoinTransactionOnChainService.getOnChainTransaction(transactionHash_2);
            transactionBlockHash_2 = onChainTransaction == null ? null : onChainTransaction.block_hash;
        }
        System.out.println("Starcoin transaction: " + transactionHash_2 + " now in block: " + transactionBlockHash_2);
        // save a transaction in database using OLD sequence number...
        String transactionHash_2_shadowed = starcoinTransactionService.createDepositStcTransaction(
                TEST_STARCOIN_DEPOSIT_ACCOUNT, BigInteger.valueOf(666666666L),
                TEST_CROSS_FROM_ETHEREUM_ACCOUNT,
                BigInteger.valueOf(TEST_CROSS_FROM_ETHEREUM_CHAIN_ID),
                "TEST" + UUID.randomUUID(), gasPrice, expirationTimestampSecs, senderSequenceNumber);
        // drop shadowed transaction
        starcoinTransactionService.dropTransactionBecauseOfUnexpectedTransaction(
                transactionHash_2_shadowed, transactionHash_2);
        // Remove dropped transaction to individual table...
        starcoinTransactionService.removeTransaction(transactionHash_2_shadowed);

    }

    @Test
    @Order(2)
    void testRepositoryAddStarcoinEvents() throws IOException {
        //BigDecimal weiToNanoStc = ethereumTransactionService.getWeiToNanoStcExchangeRate();
        //System.out.println("Current WEI to NanoSTC exchange rate: " + weiToNanoStc);
        if (true) return;
        ethereumAccountService.resetByOnChainTransactionCount(ethereumTransactionOnChainService.getSenderAddress());

        StcToEthereum e = new StcToEthereum();
        e.setBlockHash("0x50c84b1814e8fbc9d7f2736125161a95432874e6067022c7a0703240fba977ef");
        e.setBlockNumber(BigInteger.valueOf(2819));
        e.setTransactionHash("0xae86af4fb271f505c3a63d9cf50b96f7e46e4d610722e5a1fdad799639d30b95");
        e.setTransactionIndex(BigInteger.valueOf(1));
        e.setData("0x00ca9a3b00000000000000000000000000000000000000000000000000000001035354430353544300");
        e.setTypeTag("0x00000000000000000000000000000001::Account::WithdrawEvent");
        e.setEventKey("0x00000000000000000000000000000000000000000a550c18");
        e.setEventSequenceNumber(BigInteger.valueOf(10));
        e.setMintAccount(TEST_MINT_ACCOUNT);
        e.setMintAmount(new BigInteger("20000000009999999999999999999"));
        e.setCreatedAt(System.currentTimeMillis());
        e.setCreatedBy("ADMIN");
        e.setUpdatedAt(e.getCreatedAt());
        e.setUpdatedBy(e.getCreatedBy());
        e.setEventId(IdUtils.generateEventId(e));
        e.setGasPriceInWei(ethereumTransactionOnChainService.getOnChainGasPrice());
        e.setEstimatedGas(ethereumTransactionOnChainService.estimateMintStcGas(e.getMintAccount(), e.getMintAmount(),
                ethereumTransactionService.getAccountNonce(), e.getGasPriceInWei()));
        e.setWeiToNanoStcExchangeRate(tokenPriceService.getWeiToNanoStcExchangeRate());
        e.setDepositAmount(e.getWeiToNanoStcExchangeRate().multiply(new BigDecimal(
                e.getGasPriceInWei().multiply(e.getEstimatedGas()))).toBigInteger().add(e.getMintAmount()));
        starcoinEventRepository.save(e);

        List<StcToEthereum> events = starcoinEventRepository.findStcToEthereumEventsByTransactionNotExistsAndConfirmedBefore(System.currentTimeMillis());
        System.out.println(events.size());
    }

    @Test
    @Order(3)
    void testMintStcTransactions() throws IOException {
        ethereumAccountService.resetByOnChainTransactionCount(ethereumTransactionOnChainService.getSenderAddress());
        if (true) return;
        String senderAddress = ethereumTransactionOnChainService.getSenderAddress();
        BigInteger gasPrice = ethereumTransactionOnChainService.getOnChainGasPrice();

        BigInteger nonce_0 = ethereumAccountService.getTransactionCount(senderAddress);
        // -------------- Let transactions STUCK! ---------------
        BigInteger nonce_1 = nonce_0.add(BigInteger.ONE);
        ethereumAccountService.resetTransactionCount(senderAddress, nonce_1);
        // ------------------------------------------------------
        BigInteger nonce_2 = nonce_1.add(BigInteger.ONE);
        BigInteger nonce_3 = nonce_2.add(BigInteger.ONE);

        String triggerEventId_1 = "TEST:" + UUID.randomUUID();
        String triggerEventId_2 = "TEST:" + UUID.randomUUID();
        String triggerEventId_3 = "TEST:" + UUID.randomUUID();

        BigInteger amount_1 = new BigInteger("19999999999999999999999999999");
        BigInteger amount_2 = new BigInteger("29999999999999999999999999999");
        BigInteger amount_3 = new BigInteger("39999999999999999999999999999");

//        // Estimate GAS ...
//        BigInteger estimatedGas = ethereumTransactionService.estimateMintStcGas(TEST_MINT_ACCOUNT, amount_1, nonce_1);
//        System.out.println("Estimated Gas: " + estimatedGas);
//        if (true) {return;}

        // Account nonce will increase...
        String transactionHash_1 = ethereumTransactionService.createMintStcTransaction(TEST_MINT_ACCOUNT, amount_1, triggerEventId_1, gasPrice, nonce_1);
        String transactionHash_2 = ethereumTransactionService.createMintStcTransaction(TEST_MINT_ACCOUNT, amount_2, triggerEventId_2, gasPrice, nonce_2);
        String transactionHash_3 = ethereumTransactionService.createMintStcTransaction(TEST_MINT_ACCOUNT, amount_3, triggerEventId_3, gasPrice, nonce_3);

        // ----------------- Try to (re)create some violated transaction... ------------------
        // ---------                 RESET TRANSACTION COUNT                         ---------
        ethereumAccountService.resetTransactionCount(senderAddress, nonce_1);
        // Recreate SAME transaction...
        try {
            new EthereumMintStcDataIntegrityViolationHandler(ethereumTransactionService,
                    senderAddress, nonce_1, gasPrice, triggerEventId_1, TEST_MINT_ACCOUNT, amount_1)
                    .handleKnownViolation(() -> {
                        ethereumTransactionService.createMintStcTransaction(TEST_MINT_ACCOUNT,
                                amount_1, triggerEventId_1, gasPrice, nonce_1);
                    });
        } catch (UnknownDataIntegrityViolationException e) {
            e.printStackTrace();
        }
        // Changed transaction hash(using different amount), with USED account nonce...
        BigInteger unusedAmount = amount_3.add(BigInteger.ONE);
        try {
            new EthereumMintStcDataIntegrityViolationHandler(ethereumTransactionService,
                    senderAddress, nonce_2, gasPrice, triggerEventId_2, TEST_MINT_ACCOUNT, unusedAmount)
                    .handleKnownViolation(() -> {
                        ethereumTransactionService.createMintStcTransaction(TEST_MINT_ACCOUNT,
                                unusedAmount, triggerEventId_2, gasPrice, nonce_2);
                    });
        } catch (UnknownDataIntegrityViolationException e) {
            e.printStackTrace();
        }
        // Changed transaction hash, with UNUSED account nonce, BUT WITH USED triggerEventId...
        BigInteger unusedNonce = nonce_3.add(BigInteger.ONE);
        try {
            new EthereumMintStcDataIntegrityViolationHandler(ethereumTransactionService,
                    senderAddress, unusedNonce, gasPrice, triggerEventId_3, TEST_MINT_ACCOUNT, unusedAmount)
                    .handleKnownViolation(() -> {
                        ethereumTransactionService.createMintStcTransaction(TEST_MINT_ACCOUNT,
                                unusedAmount, triggerEventId_3, gasPrice, unusedNonce);
                    });
        } catch (UnknownDataIntegrityViolationException e) {
            // Unknown ERROR!
            e.printStackTrace();
            System.out.println("SHOULD NOT GO HERE!");
            //assertTrue(false);
            throw new RuntimeException(e);
        }

        // ----------- Send created transactions -------------
        ethereumTransactionServiceFacade.updateMintStcTransactionStatusAndSend(transactionHash_1);
        ethereumTransactionServiceFacade.updateMintStcTransactionStatusAndSend(transactionHash_2);
        ethereumTransactionServiceFacade.updateMintStcTransactionStatusAndSend(transactionHash_3);

        // --------- JUMP the queue! Reset account transaction count(nonce) ---------
        ethereumAccountService.resetTransactionCount(senderAddress, nonce_0);
        String triggerEventId_0 = "TEST:" + UUID.randomUUID();
        BigInteger amount_0 = new BigInteger("49999999999999999999999999999");
        String transactionHash_0 = ethereumTransactionServiceFacade.createMintStcTransactionAndSend(
                TEST_MINT_ACCOUNT, amount_0, triggerEventId_0, gasPrice);
        System.out.println("Transaction created and sent: " + transactionHash_0);

        // ------------------------ Waiting account nonce goto right place... ------------------------
        // Confirmed transactionCount will grow
        // because transaction-(re)send and confirmation task services run in background.
        while (!(nonce_3.compareTo(ethereumAccountService.getConfirmedTransactionCount(senderAddress)) <= 0)) {
            try {
                System.out.println("Waiting account transaction count(nonce) goto " + nonce_3 + "...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // ------------------- Just send a transaction, NO SAVING in table... ---------------------
        BigInteger nonce_5 = nonce_3.add(BigInteger.ONE);
        nonce_5 = ethereumAccountService.getTransactionCountAndIncrease(senderAddress);
        //assertEquals(nonce_5, ethereumAccountService.getTransactionCountAndIncrease(senderAddress));
        String triggerEventId_5 = "TEST:" + UUID.randomUUID();
        BigInteger amount_5 = new BigInteger("59999999999999999999999999999");
        String transactionHash_5 = ethereumTransactionOnChainService.noSavingJustSendMintStcTransaction(
                TEST_MINT_ACCOUNT, amount_5, triggerEventId_5, nonce_5, gasPrice);
        System.out.println("Transaction just sent, no saving: " + transactionHash_5);
        // Create a transaction in table with used(and sent) account nonce.
        // This transaction will be shadowed, never be confirmed...
        String triggerEventId_5_shadowed = "TEST:" + UUID.randomUUID();
        BigInteger amount_5_shadowed = new BigInteger("50000000099999999999999999999");
        String transactionHash_5_shadowed = ethereumTransactionService.createMintStcTransaction(
                TEST_MINT_ACCOUNT, amount_5_shadowed, triggerEventId_5_shadowed, gasPrice, nonce_5);
        System.out.println("Transaction created: " + transactionHash_5_shadowed);
        // --------------------------------------------------------------

        // ----- Generally, shadowed transactions should be dropped and recreated ------
        String transactionBlockHash_5 = null;
        // wait transaction into block...
        while (transactionBlockHash_5 == null) {
            transactionBlockHash_5 = ethereumTransactionOnChainService.getOnChainTransaction(transactionHash_5).getBlockHash();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        // Mark transaction as DROPPED...
        ethereumTransactionService.dropTransactionBecauseOfUnexpectedTransaction(
                transactionHash_5_shadowed, transactionHash_5);
        // Remove dropped transaction to individual table...
        ethereumTransactionService.removeTransaction(transactionHash_5_shadowed);
        // Recreate a transaction for shadowed one, using new nonce...
        BigInteger nonce_6 = ethereumAccountService.getTransactionCount(senderAddress);
        System.out.println("The account '" + senderAddress + "' nonce now goto " + nonce_6);
        String triggerEventId_6 = triggerEventId_5_shadowed;
        BigInteger amount_6 = amount_5_shadowed;// new BigInteger("50000000099999999999999999999");
        String transactionHash_6 = ethereumTransactionServiceFacade.createMintStcTransactionAndSend(
                TEST_MINT_ACCOUNT, amount_6, triggerEventId_6,
                gasPrice);
        System.out.println("Transaction recreated: " + transactionHash_6);

        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //------------------  Assert Result -----------------
        // These transactions exist in table...
        String[] transactionHashes = new String[]{
                transactionHash_0,
                transactionHash_1,
                transactionHash_2,
                transactionHash_3,
                transactionHash_6
        };
        assertEquals(
                ethereumTransactionRepository.findByTransactionHashIn(transactionHashes).size(),
                transactionHashes.length
        );
        // Dropped transaction(s) in individual table...
        String[] droppedTransactionHashes = new String[]{
                transactionHash_5_shadowed
        };
        assertEquals(
                droppedEthereumTransactionRepository.findByTransactionHashIn(droppedTransactionHashes).size(),
                droppedTransactionHashes.length
        );
        // Unexpected transaction(s) in individual table...
        String[] unexpectedTransactionHashes = new String[]{
                transactionHash_5
        };
        assertEquals(
                unexpectedEthereumTransactionRepository.findByTransactionHashIn(unexpectedTransactionHashes).size(),
                unexpectedTransactionHashes.length
        );
    }

    @Test
    @Order(1)
    void testAddStarcoinNodeHeartbeats() {
        StarcoinNodeHeartbeat b7 = new StarcoinNodeHeartbeat();
        b7.setNodeId("0x" + UUID.randomUUID().toString().replace("-", ""));
        b7.setStartedAt(BigInteger.valueOf(79));
        b7.setBeatenAt(BigInteger.valueOf(92));
        addStarcoinNodeHeartbeat(b7);

        StarcoinNodeHeartbeat b6 = new StarcoinNodeHeartbeat();
        b6.setNodeId("0x" + UUID.randomUUID().toString().replace("-", ""));
        b6.setStartedAt(BigInteger.valueOf(91));
        b6.setBeatenAt(BigInteger.valueOf(100));
        addStarcoinNodeHeartbeat(b6);

        StarcoinNodeHeartbeat b5 = new StarcoinNodeHeartbeat();
        b5.setNodeId("0x" + UUID.randomUUID().toString().replace("-", ""));
        b5.setStartedAt(BigInteger.valueOf(60));
        b5.setBeatenAt(BigInteger.valueOf(71));
        addStarcoinNodeHeartbeat(b5);

        StarcoinNodeHeartbeat b4 = new StarcoinNodeHeartbeat();
        b4.setNodeId("0x" + UUID.randomUUID().toString().replace("-", ""));
        b4.setStartedAt(BigInteger.valueOf(71));
        b4.setBeatenAt(BigInteger.valueOf(80));
        addStarcoinNodeHeartbeat(b4);

        StarcoinNodeHeartbeat b3 = new StarcoinNodeHeartbeat();
        b3.setNodeId("0x" + UUID.randomUUID().toString().replace("-", ""));
        b3.setStartedAt(BigInteger.valueOf(51));
        b3.setBeatenAt(BigInteger.valueOf(60));
        addStarcoinNodeHeartbeat(b3);

        StarcoinNodeHeartbeat b2 = new StarcoinNodeHeartbeat();
        b2.setNodeId("0x" + UUID.randomUUID().toString().replace("-", ""));
        b2.setStartedAt(BigInteger.valueOf(21));
        b2.setBeatenAt(BigInteger.valueOf(30));
        addStarcoinNodeHeartbeat(b2);

        StarcoinNodeHeartbeat b1 = new StarcoinNodeHeartbeat();
        b1.setNodeId("0x" + UUID.randomUUID().toString().replace("-", ""));
        b1.setStartedAt(BigInteger.valueOf(1));
        b1.setBeatenAt(BigInteger.valueOf(10));
        addStarcoinNodeHeartbeat(b1);

        List<AbstractNodeHeartbeat.Breakpoint> breakpoints = nodeHeartbeatRepository.findBreakpoints();
        breakpoints.forEach(b -> System.out.println(b.getBeatenAt() + "\t" + b.getIsEndPoint()));

        System.out.println(starcoinNodeHeartbeatService.findBreakIntervals());
    }

    private void addStarcoinNodeHeartbeat(StarcoinNodeHeartbeat b) {
        b.setCreatedAt(System.currentTimeMillis());
        b.setCreatedBy("admin");
        b.setUpdatedAt(b.getCreatedAt());
        b.setUpdatedBy(b.getCreatedBy());
        nodeHeartbeatRepository.save(b);
    }

}

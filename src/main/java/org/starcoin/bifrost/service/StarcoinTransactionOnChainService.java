package org.starcoin.bifrost.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.novi.serde.SerializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.starcoin.bean.TypeObj;
import org.starcoin.bifrost.DomainError;
import org.starcoin.bifrost.data.model.StarcoinDepositStc;
import org.starcoin.utils.JsonRpcClient;
import org.starcoin.utils.HexUtils;
import org.starcoin.bifrost.utils.StarcoinAccountAddressUtils;
import org.starcoin.utils.StarcoinOnChainUtils;
import org.starcoin.jsonrpc.client.JSONRPC2Session;
import org.starcoin.types.*;
import org.starcoin.utils.AccountAddressUtils;
import org.starcoin.utils.SignatureUtils;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.starcoin.bifrost.utils.Sha3HashUtils.hashStarcoinSignedUserTransaction;
import static org.starcoin.utils.StarcoinOnChainUtils.submitHexTransaction;
import static org.starcoin.bifrost.utils.StarcoinTransactionPayloadUtils.encode_withdraw_from_ethereum_chain_script_function;

@Service
public class StarcoinTransactionOnChainService {

    static final long TRANSACTION_EXPIRATION_SECONDS = 2 * 60 * 60L; // two hours?

    private static final Logger LOG = LoggerFactory.getLogger(EthereumTransactionService.class);

    private static final BigInteger DEPOSIT_STC_GAS_LIMIT = BigInteger.valueOf(10000000);

    private final String jsonRpcUrl;

    private final JSONRPC2Session jsonRpcSession;

    @Value("${starcoin.sender-address}")
    private String senderAddress;// = "0x07fa08a855753f0ff7292fdcbe871216";

    @Value("${starcoin.sender-public-key}")
    private String senderPublicKey;// = "0x3517cf661eb9ec48ad86639db66ea463b871b7d10c52bb37461570aef68f8c36";

    @Value("${starcoin.sender-private-key}")
    private String senderPrivateKey;

    @Value("${starcoin.chain-id}")
    private Integer chainId;// = (byte) 254;

    @Autowired
    private Executor taskExecutor;

    public StarcoinTransactionOnChainService(@Value("${starcoin.json-rpc-url}") String jsonRpcUrl) throws MalformedURLException {
        this.jsonRpcUrl = jsonRpcUrl;
        this.jsonRpcSession = new JSONRPC2Session(new URL(this.jsonRpcUrl));
    }

    public String getJsonRpcUrl() {
        return jsonRpcUrl;
    }

    private Map<String, Object> getJsonRpcDryRunParam(String code, List<String> type_args, List<String> args) {
        return StarcoinOnChainUtils.getJsonRpcDryRunParam(this.chainId,
                this.getOnChainGasPrice(), DEPOSIT_STC_GAS_LIMIT, this.senderAddress, this.senderPublicKey,
                this.getSenderSequenceNumber(), code, type_args, args);
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public String getSenderPublicKey() {
        return senderPublicKey;
    }

    public String getSenderPrivateKey() {
        return senderPrivateKey;
    }

    public Integer getChainId() {
        return chainId;
    }

    public BigInteger getOnChainGasPrice() {
        return StarcoinOnChainUtils.getGasPrice(this.jsonRpcSession);
    }

    public BigInteger getSenderSequenceNumber() {
        return StarcoinOnChainUtils.getAccountSequenceNumber(this.jsonRpcSession, this.senderAddress);
    }

    BigInteger getSenderSequenceNumber(String senderAddress) {
        return StarcoinOnChainUtils.getAccountSequenceNumber(this.jsonRpcSession, senderAddress);
    }

    public Long getNowSeconds() {
        return StarcoinOnChainUtils.getNowSeconds(this.jsonRpcSession);
    }

    public BigInteger estimateDepositStcGas(String from, String to, BigInteger amount, Integer fromChain) {
        Map<String, Object> m = new JsonRpcClient(jsonRpcSession).sendJsonRpc("contract.dry_run",
                Collections.singletonList(getWithdrawFromEthereumChainJsonRpcDryRunParam(from, to, amount, fromChain)), //Arrays.asList(getTransferScriptsPeerToPeerJsonRpcDryRunParam()),
                new TypeReference<Map<String, Object>>() {
                });
        if (!"Executed".equalsIgnoreCase(m.get("explained_status").toString())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Dry-run error." + m);
            }
            throw new RuntimeException("Dry-run error.");
        }
        return new BigInteger((String) m.get("gas_used"));
    }

    /**
     * This method is ONLY FOR TEST!!!
     * Just create a transaction and send without saving it.
     */
    public String noSavingJustSendDepositStcTransaction(String depositAccount, BigInteger depositAmount,
                                                        String fromAccount, BigInteger fromChain,
                                                        String triggerEventId,
                                                        BigInteger accountSeqNumber, BigInteger gasPrice,
                                                        Long expirationTimestampSecs) {
        StarcoinDepositStc depositStc;
        try {
            depositStc = doCreateDepositStcTransaction(depositAccount, depositAmount, fromAccount, fromChain, triggerEventId, accountSeqNumber, gasPrice, expirationTimestampSecs);
        } catch (SerializationError error) {
            throw new RuntimeException(error);
        }
        byte[] signedMessage = depositStc.getSignedMessage();
        sendDepositStcTransaction(depositStc, signedMessage);
        return depositStc.getTransactionHash();
    }

    StarcoinDepositStc doCreateDepositStcTransaction(String depositAccount, BigInteger depositAmount,
                                                     String fromAccount, BigInteger fromChain,
                                                     String triggerEventId,
                                                     BigInteger accountSeqNumber, BigInteger gasPrice,
                                                     Long expirationTimestampSecs) throws SerializationError {
        //BigInteger gasPrice = DEFAULT_GAS_PRICE;
        BigInteger gasLimit = DEPOSIT_STC_GAS_LIMIT;
        TransactionPayload payload = createTransactionPayload(depositAccount, depositAmount, fromAccount, fromChain.intValue());

        RawUserTransaction rawUserTransaction = createRawUserTransaction(AccountAddressUtils.create(senderAddress),
                accountSeqNumber, payload, gasPrice, gasLimit, expirationTimestampSecs);
        // ///////////////////////////////////////
        Ed25519PrivateKey ed25519PrivateKey = SignatureUtils.strToPrivateKey(senderPrivateKey);
        // check private key and sender address...
        assertPrivateKeyMatchesSenderAddress(ed25519PrivateKey);
        SignedUserTransaction signedUserTransaction = SignatureUtils.signTxn(ed25519PrivateKey,
                rawUserTransaction);
        byte[] signedMessage = signedUserTransaction.bcsSerialize();
        String txnHash = hashStarcoinSignedUserTransaction(signedMessage);
        LOG.debug("TransactionHash: " + txnHash);
        // ///////////////////////////////////////
        StarcoinDepositStc depositStc = new StarcoinDepositStc();
        depositStc.setTransactionHash(txnHash);
        depositStc.setAccountAddress(senderAddress);
//        depositStc.setRecipient(contractAddress);
        depositStc.setAccountSequenceNumber(accountSeqNumber);
        depositStc.setGasUnitPrice(gasPrice);
        depositStc.setMaxGasAmount(gasLimit);
        depositStc.setDepositAccount(depositAccount);
        depositStc.setDepositAmount(depositAmount);
        depositStc.setFromAccount(fromAccount);
        depositStc.setFromChain(fromChain);
        depositStc.setTriggerEventId(triggerEventId);
        depositStc.setPayload(HexUtils.toHexString(payload.bcsSerialize()));
        depositStc.setExpirationTimestampSecs(expirationTimestampSecs);
        depositStc.setCreatedBy("ADMIN");
        depositStc.setCreatedAt(System.currentTimeMillis());
        depositStc.setUpdatedBy("ADMIN");
        depositStc.setUpdatedAt(System.currentTimeMillis());
        // ----------- transient properties -------------
        depositStc.setRawTransaction(rawUserTransaction);
        depositStc.setCredentials(ed25519PrivateKey);
        depositStc.setSignedMessage(signedMessage);
        return depositStc;
    }

    private void assertPrivateKeyMatchesSenderAddress(Ed25519PrivateKey ed25519PrivateKey) {
        String senderAddressFromPK = StarcoinAccountAddressUtils.getAddressFromPrivateKey(ed25519PrivateKey);
        if (!senderAddressFromPK.equalsIgnoreCase(senderAddress)) {
            LOG.debug("Get sender address from private key: " + senderAddressFromPK
                    + ", NOT match with config address: " + this.senderAddress);
            throw new DomainError("Sender address config ERROR.");
        }
    }

    TransactionPayload createTransactionPayload(String depositAccount, BigInteger depositAmount, String from, int fromChain) {
        if (depositAmount.signum() == -1) {
            throw new IllegalArgumentException("depositAmount is negative: " + depositAmount);
        }
        TransactionPayload payload = encode_withdraw_from_ethereum_chain_script_function(
                TypeObj.STC().toTypeTag(), from, AccountAddressUtils.create(depositAccount), depositAmount, fromChain);
        return payload;
    }

//    /**
//     * This method is ONLY FOR TEST!
//     */
//    private Map<String, Object> getTransferScriptsPeerToPeerJsonRpcDryRunParam() {//(String recipientAddress, String amount) {
//        String code = "0x1::TransferScripts::peer_to_peer";
//        List<String> type_args = Collections.singletonList("0x1::STC::STC");
//        List<String> args = Arrays.asList("0x621500bf2b4aad17a690cb24f9a225c6", "x\"\"", "1000000000u128");
//        return getJsonRpcDryRunParam(code, type_args, args);
//    }

    RawUserTransaction createRawUserTransaction(AccountAddress sender,
                                                BigInteger seqNumber,
                                                TransactionPayload payload,
                                                BigInteger gasPrice, BigInteger gasLimit,
                                                Long expirationTimestampSecs) {
        return StarcoinOnChainUtils.createRawUserTransaction(this.chainId, sender, seqNumber,
                payload, gasPrice, gasLimit, expirationTimestampSecs);
    }

    @Async
    protected void sendDepositStcTransaction(StarcoinDepositStc depositStc) {
        TransactionPayload payload = createTransactionPayload(
                depositStc.getDepositAccount(), depositStc.getDepositAmount(), depositStc.getFromAccount(), depositStc.getFromChain().intValue());
        RawUserTransaction rawUserTransaction = createRawUserTransaction(
                AccountAddressUtils.create(getSenderAddress()),
                depositStc.getAccountSequenceNumber(),
                payload,
                depositStc.getGasUnitPrice(),
                depositStc.getMaxGasAmount(),
                depositStc.getExpirationTimestampSecs());
        // ///////////////////////////////////////
        Ed25519PrivateKey ed25519PrivateKey = SignatureUtils.strToPrivateKey(getSenderPrivateKey());
        // check private key with sender address...
        assertPrivateKeyMatchesSenderAddress(ed25519PrivateKey);
        SignedUserTransaction signedUserTransaction = SignatureUtils.signTxn(ed25519PrivateKey,
                rawUserTransaction);
        byte[] signedMessage = new byte[0];
        try {
            signedMessage = signedUserTransaction.bcsSerialize();
        } catch (SerializationError error) {
            LOG.error("Serialize signedUserTransaction error.", error);
            throw new RuntimeException(error);
        }
        String txnHash = hashStarcoinSignedUserTransaction(signedMessage);
        LOG.debug("TransactionHash recalculated: " + txnHash);
        if (!depositStc.getTransactionHash().equals(txnHash)) {
            throw new RuntimeException("Transaction hash error. Hash in database NOT equals recalculated hash. "
                    + depositStc.getTransactionHash() + " <> " + txnHash
                    + ". Maybe you have switched sender private key.");
        }
        sendDepositStcTransaction(depositStc, signedMessage);
    }

    @Async
    protected void sendDepositStcTransaction(StarcoinDepositStc depositStc, byte[] signedMessage) {
        String transactionHash = depositStc.getTransactionHash();
        String result = submitHexTransaction(this.jsonRpcSession, signedMessage);
        if (!transactionHash.equalsIgnoreCase(result)) {
            String msg = "Transaction hash error. Transaction hash in database NOT equals RPC returned hash. "
                    + transactionHash + " <> " + result;
            LOG.error(msg);
        }
    }

    private Map<String, Object> getWithdrawFromEthereumChainJsonRpcDryRunParam(String from, String to, BigInteger amount, Integer fromChain) {
        String code = "0x569AB535990a17Ac9Afd1bc57Faec683::BifrostScripts::withdraw_from_ethereum_chain";//todo config??
        List<String> type_args = Collections.singletonList("0x1::STC::STC");
        //(signer: signer, from: vector<u8>, to: address, amount: u128, from_chain: u8)
        List<String> args = Arrays.asList(
                "x\"" + HexUtils.removePrefix(from) + "\"", //"0x621500bf2b4aad17a690cb24f9a225c6",
                StarcoinAccountAddressUtils.trimAddress(to), // todo: his account must exist and accept token! Is this OK???
                amount + "u128",
                fromChain + "u8");
        return getJsonRpcDryRunParam(code, type_args, args);
    }

    public StarcoinOnChainUtils.OnChainTransaction getOnChainTransaction(String transactionHash) {
        return StarcoinOnChainUtils.getOnChainTransaction(this.jsonRpcSession, transactionHash);
    }

}


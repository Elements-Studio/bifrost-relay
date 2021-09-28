package org.starcoin.bifrost.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.DomainError;
import org.starcoin.bifrost.data.model.EthereumMintStc;
import org.starcoin.bifrost.ethereum.model.STC;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction;

@Service
public class EthereumTransactionOnChainService {
    protected static final BigInteger MINT_STC_GAS_LIMIT = BigInteger.valueOf(1000000L);

    private static final Logger LOG = LoggerFactory.getLogger(EthereumTransactionService.class);
    private static final Credentials NO_CREDENTIALS = Credentials.create("0x99");
    private static final BigDecimal ETH_TO_WEI = BigDecimal.TEN.pow(18);
    private static final BigDecimal STC_TO_NANOSTC = BigDecimal.TEN.pow(9);

    @Autowired
    protected Web3j web3j;

    @Value("${ethereum.mint-stc-contract-address}")
    protected String mintStcContractAddress;// = "0x3d8c97DF6A0948f11273b0367579B1f0413DE0C4";

    @Value("${ethereum.sender-address}")
    protected String senderAddress;// = "0x414d6e7B82E9Ce949d468d502057483195C69df9";

    @Value("${ethereum.sender-private-key}")
    protected String senderPrivateKey;// = "bc4f359cd7a64bca074ffdeafcd18e3c2a860f22c64611577fcb5108fec850da";

    @Value("${ethereum.chain-id}")
    private Long chainId;// = 1337L; // Ganache ChainId.

    public String getSenderAddress() {
        return senderAddress;
    }

    public Long getChainId() {
        return chainId;
    }

    public BigInteger getOnChainGasPrice() throws IOException {
        return web3j.ethGasPrice().send().getGasPrice();
    }

    public String getSenderPrivateKey() {
        return senderPrivateKey;
    }

    public String getMintStcContractAddress() {
        return mintStcContractAddress;
    }


    public BigInteger estimateMintStcGas(String mintAccount, BigInteger mintAmount, BigInteger accountNonce,
                                         BigInteger gasPrice) throws IOException {
        if (mintAccount == null || mintAccount.isEmpty()) {
            throw new IllegalArgumentException("Account address is null.");
        }
        if (mintAmount == null) {
            throw new IllegalArgumentException("Amount is null.");
        }
        //BigInteger gasPrice = DEFAULT_GAS_PRICE;
        BigInteger gasLimit = MINT_STC_GAS_LIMIT;

        String encodedFunction = encodeMintStcFunctionCall(mintAccount, mintAmount);

        RawTransaction rawTransaction = RawTransaction.createTransaction(accountNonce,
                gasPrice, gasLimit,
                mintStcContractAddress, encodedFunction);
        EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(createFunctionCallTransaction(
                senderAddress, accountNonce,
                gasPrice, gasLimit, mintStcContractAddress, encodedFunction)).send();
        return ethEstimateGas.getAmountUsed();
    }

    /**
     * This method is ONLY FOR TEST!!!
     * Just create a Mint STC transaction and send without saving it.
     *
     * @param mintAccount    mint account address
     * @param mintAmount     mint amount
     * @param triggerEventId trigger event Id.
     * @return Transaction hash.
     */
    public String noSavingJustSendMintStcTransaction(String mintAccount, BigInteger mintAmount,
                                                     String triggerEventId, BigInteger accountNonce, BigInteger gasPrice) {
        EthereumMintStc mintStc = doCreateMintStcTransaction(mintAccount, mintAmount, triggerEventId, accountNonce, gasPrice);
        byte[] signedMessage = mintStc.getSignedMessage();
        sendMintStcTransaction(mintStc, signedMessage);
        return mintStc.getTransactionHash();
    }

    void sendMintStcTransaction(EthereumMintStc mintStc, byte[] signedMessage) {
        String transactionHash = mintStc.getTransactionHash();
        String hexValue = Numeric.toHexString(signedMessage);
        LOG.debug("Signed mint STC transaction: " + hexValue);
        web3j.ethSendRawTransaction(hexValue).flowable().subscribe(s -> {
            if (s.hasError()) {
                LOG.error("Mint STC transaction '" + transactionHash + "' sent, returned error: "
                        + s.getError().getMessage());
            } else {
                LOG.debug("Mint STC transaction sent, returned hash: " + s.getTransactionHash());
                if (!transactionHash.equals(s.getTransactionHash())) {
                    LOG.error("Transaction hash error." + transactionHash + " <> " + s.getTransactionHash());
                }
            }
        });
    }

    void sendMintStcTransaction(EthereumMintStc mintStc) {
        RawTransaction rawTransaction = RawTransaction.createTransaction(mintStc.getAccountNonce(),
                mintStc.getGasPrice(), mintStc.getGasLimit(),
                mintStc.getRecipient(), mintStc.getInput());
        Credentials credentials = Credentials.create(getSenderPrivateKey());
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, getChainId(), credentials);
        String txnHash = Numeric.toHexString(Hash.sha3(signedMessage));
        LOG.debug("TransactionHash recalculated: " + txnHash);
        if (!mintStc.getTransactionHash().equals(txnHash)) {
            throw new RuntimeException("Transaction hash error. " + mintStc.getTransactionHash() + " <> " + txnHash);
        }
        sendMintStcTransaction(mintStc, signedMessage);
    }

    EthereumMintStc doCreateMintStcTransaction(String mintAccount, BigInteger mintAmount,
                                                         String triggerEventId,
                                                         BigInteger accountNonce, BigInteger gasPrice) {
        //BigInteger gasPrice = DEFAULT_GAS_PRICE;
        BigInteger gasLimit = MINT_STC_GAS_LIMIT;

        String encodedFunction = encodeMintStcFunctionCall(mintAccount, mintAmount);

        RawTransaction rawTransaction = RawTransaction.createTransaction(accountNonce,
                gasPrice, gasLimit,
                mintStcContractAddress, encodedFunction);
        // ///////////////////////////////////////
        Credentials credentials = Credentials.create(senderPrivateKey);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, getChainId(), credentials);
        String txnHash = Numeric.toHexString(Hash.sha3(signedMessage));
        LOG.debug("TransactionHash: " + txnHash);
        // ///////////////////////////////////////
        EthereumMintStc mintStc = new EthereumMintStc();
        mintStc.setTransactionHash(txnHash);
        assertPrivateKeyMatchesSenderAddress(credentials);
        mintStc.setAccountAddress(this.senderAddress);
        mintStc.setAccountNonce(accountNonce);
        mintStc.setRecipient(mintStcContractAddress);
        mintStc.setGasPrice(gasPrice);
        mintStc.setGasLimit(gasLimit);
        mintStc.setMintAccount(mintAccount);
        mintStc.setMintAmount(mintAmount);
        mintStc.setTriggerEventId(triggerEventId);
        mintStc.setInput(encodedFunction);
        mintStc.setCreatedBy("ADMIN");
        mintStc.setCreatedAt(System.currentTimeMillis());
        mintStc.setUpdatedBy("ADMIN");
        mintStc.setUpdatedAt(System.currentTimeMillis());
        // ----------- transient properties -------------
        mintStc.setRawTransaction(rawTransaction);
        mintStc.setCredentials(credentials);
        mintStc.setSignedMessage(signedMessage);
        return mintStc;
    }

    private void assertPrivateKeyMatchesSenderAddress(Credentials credentials) {
        String addressFromPK = credentials.getAddress();
        if (!addressFromPK.equalsIgnoreCase(this.senderAddress)) {
            LOG.debug("Get Mint STC sender address from private key: " + addressFromPK
                    + ", NOT match config address: " + this.senderAddress);
            throw new DomainError("Mint STC sender config ERROR.");
        }
    }

    public Transaction getOnChainTransaction(String unexpectedTransactionHash) throws IOException {
        EthTransaction ethTransaction = web3j.ethGetTransactionByHash(unexpectedTransactionHash).send();
        Transaction onChainTransaction = ethTransaction.getTransaction().orElse(null);
        if (onChainTransaction == null) {
            throw new IllegalArgumentException("Illegal unexpected transaction hash: " + unexpectedTransactionHash);
        }
        return onChainTransaction;
    }

    protected String encodeMintStcFunctionCall(String mintAccount, BigInteger mintAmount) {
        STC estc = STC.load(mintStcContractAddress, web3j, NO_CREDENTIALS, new DefaultGasProvider());
        return estc.depositFromStarcoinChain(mintAccount, mintAmount).encodeFunctionCall();
    }

}

package org.starcoin.bifrost;

import org.starcoin.bifrost.ethereum.model.STC;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.TransactionUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class EthereumCallTests {

    private static String ethereumHttpServiceUrl = "HTTP://127.0.0.1:7545";
    private static String contractAddress = "0x3d8c97DF6A0948f11273b0367579B1f0413DE0C4";
    private static String privateKey = "bc4f359cd7a64bca074ffdeafcd18e3c2a860f22c64611577fcb5108fec850da";


    public static void main(String[] args) {
//        Ed25519PrivateKey pk = SignatureUtils.strToPrivateKey(
//                "...");
//        String addr = StarcoinAccountAddressUtils.getAddressFromPrivateKey(pk);
//        System.out.println(addr);
//        //0x569AB535990a17Ac9Afd1bc57Faec683
//        if (true) return;
        Web3j web3 = Web3j.build(new HttpService(ethereumHttpServiceUrl));
        StaticGasProvider staticGasProvider = new StaticGasProvider(DefaultGasProvider.GAS_PRICE,
                BigInteger.valueOf(1000000));
        Credentials credentials = Credentials.create(privateKey);
        STC estc = STC.load(contractAddress, web3, credentials, staticGasProvider);
//        estc.mint("0x91BAa5D576519147f9208F7C3097838dA52E2B3F", BigInteger.valueOf(19999999999999L)).flowable().subscribe(s -> {
//            System.out.println(s);
//        });
        BigInteger accountNonce = BigInteger.valueOf(9);
		String encodedFunction = estc.depositFromStarcoinChain("0x91BAa5D576519147f9208F7C3097838dA52E2B3F", BigInteger.valueOf(19999999999999L)).encodeFunctionCall();
		RawTransaction rawTransaction = RawTransaction.createTransaction(accountNonce, DefaultGasProvider.GAS_PRICE, BigInteger.valueOf(1000000),
				contractAddress, encodedFunction);
		// /////////////////////
        System.out.println("------------ transation hash -----------------");
		System.out.println(TransactionUtils.generateTransactionHashHexEncoded(rawTransaction, credentials));
		// /////////////////////
		byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
		String hexValue = Numeric.toHexString(signedMessage);
        System.out.println("------------ signed message -----------------");
		System.out.println(hexValue);

		web3.ethSendRawTransaction(hexValue).flowable().subscribe(s -> {
			System.out.println(s);
		});
    }

}

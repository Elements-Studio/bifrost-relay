package org.starcoin.bifrost;

import com.novi.serde.Bytes;
import com.novi.serde.SerializationError;
import org.starcoin.bean.TypeObj;
import org.starcoin.types.*;
import org.starcoin.utils.*;

import java.math.BigInteger;
import java.util.Collections;

public class JsonRpcDeployMoveApp {

    private static final String GENESIS_HEADER = "000000009b915617000000000000000000000000000000000000000000000000" +
            "0000000000000000000000006de0a8f7ee3fb67d8e04ac9547f3615e59adc6e0a2309c90080a272dc1" +
            "fa1fd900000000000000000000000000000000000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000c8365b000000001dac2b7c00000000" +
            "fd1a057b226c6561646572223a343239343936373239352c227672665f76616c7565223a22484a675171" +
            "706769355248566745716354626e6443456c384d516837446172364e4e646f6f79553051666f67555634" +
            "764d50675851524171384d6f38373853426a2b38577262676c2b36714d7258686b667a72375751343d2" +
            "22c227672665f70726f6f66223a22785864422b5451454c4c6a59734965305378596474572f442f39542f7" +
            "46e5854624e436667354e62364650596370382f55706a524c572f536a5558643552576b75646632646f4c526" +
            "7727052474b76305566385a69413d3d222c226c6173745f636f6e6669675f626c6f636b5f6e756d223a343" +
            "239343936373239352c226e65775f636861696e5f636f6e666967223a7b2276657273696f6e223a312c2276" +
            "696577223a312c226e223a372c2263223a322c22626c6f636b5f6d73675f64656c6179223a313030303030" +
            "30303030302c22686173685f6d73675f64656c6179223a31303030303030303030302c22706565725f68616" +
            "e647368616b655f74696d656f7574223a31303030303030303030302c227065657273223a5b7b22696e6465" +
            "78223a312c226964223a2231323035303238313732393138353430623262353132656165313837326132613" +
            "265336132386439383963363064393564616238383239616461376437646437303664363538227d2c7b22696" +
            "e646578223a322c226964223a22313230353033386238616636323130656366646362636162323235353265663" +
            "86438636634316336663836663963663961623533643836353734316366646238333366303662227d2c7" +
            "b22696e646578223a332c226964223a2231323035303234383261636236353634623139623930363533663665" +
            "396338303632393265386161383366373865376139333832613234613665666534316330633036663339227d2" +
            "c7b22696e646578223a342c226964223a22313230353032363739393330613432616166336336393739386361" +
            "38613366313265313334633031393430353831386437383364313137343865303339646538353135393838227d" +
            "2c7b22696e646578223a352c226964223a2231323035303234363864643138393965643264316363326238323938" +
            "383261313635613065636236613734356166306337326562323938326436366234333131623465663733227d2c7b" +
            "22696e646578223a362c226964223a22313230353032656231626161623630326335383939323832353631636461616" +
            "13761616262636464306363666362633365373937393361633234616366393037373866333561227d2c7b22696e64" +
            "6578223a372c226964223a22313230353033316530373739663563356363623236313233353266653461323" +
            "03066393964336537373538653730626135336636303763353966663232613330663637386666227d5d2c22706f" +
            "735f7461626c65223a5b362c342c332c352c362c312c322c352c342c372c342c322c332c332c372c362c352c342" +
            "c362c352c312c342c332c312c322c352c322c322c362c312c342c352c342c372c322c332c342c312c352c372c342" +
            "c312c322c322c352c362c342c342c322c372c332c362c362c352c312c372c332c312c362c312c332c332c322c342c" +
            "342c312c352c362c352c312c322c362c372c352c362c332c342c372c372c332c322c372c312c352c362c352c322c" +
            "332c362c322c362c312c372c372c372c312c372c342c332c332c332c322c312c372c355d2c226d61785f626c6f" +
            "636b5f6368616e67655f76696577223a36303030307d7d9fe171f3fe643eb1c188400b828ba184816fc9ac0000";
    private static final String PUBLIC_KEYS = "1205041e0779f5c5ccb2612352fe4a200f99d3e7758e70ba53f607c59ff22a30f678ff7" +
            "57519efff911efc7ed326890a2752b9456cc0054f9b63215f1d616e574d6197120504468dd1899ed2d1" +
            "cc2b829882a165a0ecb6a745af0c72eb2982d66b4311b4ef73cff28a6492b076445337d8037c6c7be4" +
            "d3ec9c4dbe8d7dc65d458181de7b5250120504482acb6564b19b90653f6e9c806292e8aa83f78e7a93" +
            "82a24a6efe41c0c06f39ef0a95ee60ad9213eb0be343b703dd32b12db32f098350cf3f4fc3bad6db23c" +
            "e120504679930a42aaf3c69798ca8a3f12e134c019405818d783d11748e039de8515988754f348293c65055" +
            "f0f1a9a5e895e4e7269739e243a661fff801941352c387121205048172918540b2b512eae1872a2a2e3" +
            "a28d989c60d95dab8829ada7d7dd706d658df044eb93bbe698eff62156fc14d6d07b7aebfbc1a98ec4180b" +
            "4346e67cc3fb01205048b8af6210ecfdcbcab22552ef8d8cf41c6f86f9cf9ab53d865741cfdb833f06b72f" +
            "cc7e7d8b9e738b565edf42d8769fd161178432eadb2e446dd0a8785ba088f120504eb1baab602c5899282561" +
            "cdaaa7aabbcdd0ccfcbc3e79793ac24acf90778f35a059fca7f73aeb60666178db8f704b58452b7a0b8621" +
            "9402c0770fcb52ac9828c";
    private static final long DEFAULT_TRANSACTION_EXPIRATION_SECONDS = 2 * 60 * 60;
    private static final String GAS_TOKEN_CODE = "0x1::STC::STC";

    public static void main(String[] args) {

        //ChainInfo chainInfo = ChainInfo.DEFAULT_BARNARD;//使用 barnard 网络！
        ChainInfo chainInfo = new ChainInfo(
                "halley",
                "https://halley-seed.starcoin.org",
                253
        );
//        ChainInfo chainInfo = new ChainInfo(
//                "dev",
//                "http://localhost:9850",
//                254
//        );
//        ChainInfo chainInfo = new ChainInfo(
//                System.getenv("STARCOIN_NETWORK"),
//                System.getenv("STARCOIN_RPC_URL"),
//                Integer.parseInt(System.getenv("STARCOIN_CHAIN_ID"))
//        );
        StarcoinClient starcoinClient = new StarcoinClient(chainInfo);
//        if (args.length < 1) {
//            throw new IllegalArgumentException("Please enter account private key");
//        }
        String privateKey = System.getenv("STARCOIN_SENDER_PRIVATE_KEY");//args[0];
        if (privateKey == null || privateKey.isEmpty()) {
            throw new RuntimeException("Private key is null!");
        }

        // -------------------------------------------------------
        // starcoin% dev package -o ./build -n packaged ./storage/0x2d81a0427d64ff61b11ede9085efa5ad/
        String packageFilePath = "~/Documents/Elements-Studio/poly-stc-contracts/build/packaged.blob";
        String rspBody = starcoinClient.deployContractPackage(
                AccountAddressUtils.create("0x2d81a0427d64Ff61b11eDe9085EFA5ad"),
                SignatureUtils.strToPrivateKey(privateKey),
                packageFilePath, null);
        System.out.println("------------------ deploy blob package ------------------");
        System.out.println(rspBody);
        try {
            Thread.sleep(1000 * 20);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        // -------------------------------------------------------
        TransactionPayload initPayload = encode_init_genesis_script_function();
        RawUserTransaction rawUserTransaction = buildRawUserTransaction(chainInfo.getChainId(),
                AccountAddressUtils.create("0x2d81a0427d64Ff61b11eDe9085EFA5ad"),
                starcoinClient.getAccountSequenceNumber(AccountAddressUtils.create("0x2d81a0427d64Ff61b11eDe9085EFA5ad")),
                System.currentTimeMillis() / 1000 + DEFAULT_TRANSACTION_EXPIRATION_SECONDS,
                initPayload);
        String initTxHash = starcoinClient.submitHexTransaction(
                //AccountAddressUtils.create("0x2d81a0427d64Ff61b11eDe9085EFA5ad"),
                SignatureUtils.strToPrivateKey(privateKey),
                rawUserTransaction);
        System.out.println("------------------ init genesis ------------------");
        System.out.println(initTxHash);

        try {
            Thread.sleep(1000 * 20);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        // -------------------------------------------------------
        TransactionPayload lockPayload = encode_cross_chain_lock_script_function();
        String lockTxHash = starcoinClient.submitTransaction(
                AccountAddressUtils.create("0x2d81a0427d64Ff61b11eDe9085EFA5ad"),
                SignatureUtils.strToPrivateKey(privateKey),
                lockPayload);
        System.out.println("------------------ lock asset ------------------");
        System.out.println(lockTxHash);

    }

    public static TransactionPayload encode_cross_chain_lock_script_function() {
        ScriptFunction.Builder script_function_builder = new ScriptFunction.Builder();
        script_function_builder.ty_args = java.util.Arrays.asList(
                TypeObj.STC().toTypeTag(),
                TypeObj.builder().name("Starcoin").moduleName("CrossChainType").moduleAddress("0x2d81a0427d64ff61b11ede9085efa5ad").build().toTypeTag()
        );
        try {
            script_function_builder.args = java.util.Arrays.asList(
                    new Bytes(bcsSerializeHex("bd7e8be8fae9f60f2f5136433e36a091")),
                    new Bytes(bcsSerializeU128(BigInteger.valueOf(1000000)))
            );
        } catch (SerializationError error) {
            error.printStackTrace();
            throw new RuntimeException();
        }
        script_function_builder.function = new Identifier("lock");
        script_function_builder.module = new ModuleId(AccountAddress.valueOf(Hex.decode("0x2d81a0427d64ff61b11ede9085efa5ad")),
                new Identifier("CrossChainScript"));

        TransactionPayload.ScriptFunction.Builder builder = new TransactionPayload.ScriptFunction.Builder();
        builder.value = script_function_builder.build();
        return builder.build();
    }

    public static TransactionPayload encode_init_genesis_script_function() {
        ScriptFunction.Builder script_function_builder = new ScriptFunction.Builder();
        script_function_builder.ty_args = Collections.emptyList();
        //TypeObj.builder().name("").moduleName("").moduleAddress("").build().toTypeTag();
        try {
            script_function_builder.args = java.util.Arrays.asList(
                    new Bytes(bcsSerializeHex(GENESIS_HEADER)),
                    new Bytes(bcsSerializeHex(PUBLIC_KEYS))
            );
        } catch (SerializationError error) {
            error.printStackTrace();
            throw new RuntimeException();
        }
        script_function_builder.function = new Identifier("init_genesis");
        script_function_builder.module = new ModuleId(AccountAddress.valueOf(Hex.decode("0x2d81a0427d64ff61b11ede9085efa5ad")),
                new Identifier("CrossChainScript"));

        TransactionPayload.ScriptFunction.Builder builder = new TransactionPayload.ScriptFunction.Builder();
        builder.value = script_function_builder.build();
        return builder.build();
    }

    public static byte[] bcsSerializeHex(String hex) throws com.novi.serde.SerializationError {
        com.novi.serde.Serializer serializer = new com.novi.bcs.BcsSerializer();
        serializer.serialize_bytes(new Bytes(Hex.decode(hex)));
        return serializer.get_bytes();
    }

    public static byte[] bcsSerializeU128(BigInteger integer) throws com.novi.serde.SerializationError {
        com.novi.serde.Serializer serializer = new com.novi.bcs.BcsSerializer();
        serializer.serialize_u128(integer);
        return serializer.get_bytes();
    }

    private static RawUserTransaction buildRawUserTransaction(int chaindId, AccountAddress sender, long seqNumber,
                                                              long expirationSeconds,
                                                              TransactionPayload payload) {
        ChainId chainId = new ChainId((byte) chaindId);
        return new RawUserTransaction(sender, seqNumber, payload, 40000000L, 1L,
                GAS_TOKEN_CODE, expirationSeconds, chainId);
    }

}


package org.starcoin.bifrost.subscribe;

import org.starcoin.bifrost.data.model.StcToEthereum;
import org.starcoin.bifrost.data.utils.IdUtils;
import org.starcoin.utils.HexUtils;

import java.math.BigInteger;

/**
 * Starcoin on-chain Event.
 * Now it is only CrossChainDepositEvent.
 */
public class StarcoinEvent {

    //public DecodeEventData decode_event_data;
    public String block_hash;// "0xc75a3245bad20b0b129c8778810b7f8bdbc4fee2fe208f719299d92368637329",
    public String block_number;// "14",
    public String transaction_hash;// "0x25e2866a2947293491fb1e10bb98c104802734f97575e01b5a37f7e55100e419",
    public Long transaction_index;// 1,
    public String data;
    public String type_tag; // "0x569AB535990a17Ac9Afd1bc57Faec683::Bifrost::CrossChainDepositEvent",
    public String event_key;// "0x0300000000000000ccf1adedf0ba6f9bdb9a6905173a5d72",
    public String event_seq_number;// "7"

    /**
     * {
     * "decode_event_data": {
     * "token_code": {
     * "addr": "0x00000000000000000000000000000001",
     * "module_name": "0x535443",
     * "name": "0x535443"
     * },
     * "from": "0x569AB535990a17Ac9Afd1bc57Faec683",
     * "to": "0x71dfdd2bf49e8af5226e0078efa31ecf258bc44e",
     * "owner": "0x569AB535990a17Ac9Afd1bc57Faec683",
     * "value": 1000000000,
     * "to_chain": 1
     * },
     * "block_hash": "0xc75a3245bad20b0b129c8778810b7f8bdbc4fee2fe208f719299d92368637329",
     * "block_number": "14",
     * "transaction_hash": "0x25e2866a2947293491fb1e10bb98c104802734f97575e01b5a37f7e55100e419",
     * "transaction_index": 1,
     * "data": "0x000000000000000000000000000000010353544303535443ccf1adedf0ba6f9bdb9a6905173a5d721471dfdd2bf49e8af5226e0078efa31ecf258bc44eccf1adedf0ba6f9bdb9a6905173a5d7200ca9a3b00000000000000000000000001",
     * "type_tag": "0x569AB535990a17Ac9Afd1bc57Faec683::Bifrost::CrossChainDepositEvent",
     * "event_key": "0x0300000000000000ccf1adedf0ba6f9bdb9a6905173a5d72",
     * "event_seq_number": "7"
     * }
     */
    public static void copyProperties(StarcoinEvent src,
                                      org.starcoin.bifrost.types.CrossChainDepositEvent srcDecodedEventData, StcToEthereum e) {

        e.setBlockHash(src.block_hash);
        e.setBlockNumber(new BigInteger(src.block_number));
        e.setTransactionHash(src.transaction_hash);
        e.setTransactionIndex(BigInteger.valueOf(src.transaction_index));
        e.setData(src.data);
        e.setTypeTag(src.type_tag);
        e.setEventKey(src.event_key);
        e.setEventSequenceNumber(new BigInteger(src.event_seq_number));
        e.setMintAccount(HexUtils.byteArrayToHexWithPrefix(srcDecodedEventData.to.content()));
        //e.setMintAmount(...);// todo set mint(on ethereum) amount here???
        e.setDepositAmount(srcDecodedEventData.value);
//        src.decode_event_data.from;
//        src.decode_event_data.owner;
//        src.decode_event_data.to_chain;
//        src.decode_event_data.token_code;
        e.setCreatedAt(System.currentTimeMillis());
        e.setCreatedBy("ADMIN");
        e.setUpdatedAt(e.getCreatedAt());
        e.setUpdatedBy(e.getCreatedBy());
        e.setEventId(IdUtils.generateEventId(e));
        //e.setGasPriceInWei(ethereumTransactionOnChainService.getOnChainGasPrice());
        //e.setEstimatedGas(ethereumTransactionOnChainService.estimateMintStcGas(e.getMintAccount(), e.getMintAmount(), ethereumTransactionService.getAccountNonce(), e.getGasPriceInWei()));
        //e.setWeiToNanoStcExchangeRate(tokenPriceService.getWeiToNanoStcExchangeRate());
        //e.setDepositAmount(e.getWeiToNanoStcExchangeRate().multiply(new BigDecimal(e.getGasPriceInWei().multiply(e.getEstimatedGas()))).toBigInteger().add(e.getMintAmount()));
    }

//    public static class DecodeEventData {
//        public TokenCode token_code;
//        public String from; // "0x569AB535990a17Ac9Afd1bc57Faec683",
//        public String to;// "0x71dfdd2bf49e8af5226e0078efa31ecf258bc44e",
//        public String owner;// "0x569AB535990a17Ac9Afd1bc57Faec683",
//        public BigInteger value;// 1000000000,
//        public Long to_chain;// 1
//
//        public static class TokenCode {
//            public String addr; // "0x00000000000000000000000000000001",
//            public String module_name;// "0x535443",
//            public String name;// "0x535443"
//        }
//    }
}
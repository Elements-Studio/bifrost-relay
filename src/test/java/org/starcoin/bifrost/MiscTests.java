package org.starcoin.bifrost;

import com.novi.serde.DeserializationError;
import org.starcoin.bifrost.types.CrossChainDepositEvent;
import org.starcoin.jsonrpc.client.JSONRPC2Session;
import org.starcoin.utils.HexUtils;
import org.starcoin.utils.StarcoinOnChainUtils;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

public class MiscTests {

    public static void main(String[] args) {
        try {
            JSONRPC2Session jsonrpc2Session = new JSONRPC2Session(new URL("https://barnard-seed.starcoin.org"));
            BigInteger stcBalance = StarcoinOnChainUtils.getAccountStcBalance(jsonrpc2Session, "0x0000000000000000000000000a550c18");
            System.out.println(stcBalance);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (true) return;

        String eventData = "0x000000000000000000000000000000010353544303535443ccf1adedf0ba6f9bdb9a6905173a5d721471dfdd2bf49e8af5226e0078efa31ecf258bc44eccf1adedf0ba6f9bdb9a6905173a5d7200ca9a3b00000000000000000000000001";
        CrossChainDepositEvent decode_event_data;
        try {
            decode_event_data = CrossChainDepositEvent.bcsDeserialize(HexUtils.hexToByteArray(eventData));
        } catch (DeserializationError deserializationError) {
            deserializationError.printStackTrace();
            throw new RuntimeException(deserializationError);
        }
        System.out.println(decode_event_data);
        System.out.println(HexUtils.byteArrayToHexWithPrefix(decode_event_data.to.content()));
        System.out.println(decode_event_data.value);
        System.out.println(HexUtils.byteArrayToHexWithPrefix(decode_event_data.from.toBytes()));
        System.out.println(HexUtils.byteArrayToHexWithPrefix(decode_event_data.owner.toBytes()));
        System.out.println(decode_event_data.to_chain);
        System.out.println(decode_event_data.token_code.name);
    }
}

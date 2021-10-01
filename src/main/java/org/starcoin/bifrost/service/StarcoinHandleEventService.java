package org.starcoin.bifrost.service;

import com.novi.serde.DeserializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.StcToEthereum;
import org.starcoin.bifrost.subscribe.StarcoinEvent;
import org.starcoin.utils.HexUtils;

import java.math.BigInteger;

@Service
public class StarcoinHandleEventService {
    private static final Logger LOG = LoggerFactory.getLogger(StarcoinHandleEventService.class);

    @Value("${starcoin.event-filter.cross-chain-deposit-event-type-tag}")
    private String crossChainDepositEventTypeTag;

    @Autowired
    private StarcoinEventService starcoinEventService;

    @Autowired
    private StarcoinNodeHeartbeatService starcoinNodeHeartbeatService;

    public void handle(StarcoinEvent event) {
        AbstractNodeHeartbeatService.runAndBeat(() -> {
            StcToEthereum stcToEthereum = decodeEvent(event);
            starcoinEventService.save(stcToEthereum);
        }, this.starcoinNodeHeartbeatService, new BigInteger(event.block_number));
    }

    private StcToEthereum decodeEvent(StarcoinEvent event) {
        if (!this.crossChainDepositEventTypeTag.equalsIgnoreCase(event.type_tag)) {
            throw new RuntimeException("Decode wrong event, type tag: " + event.type_tag);
        }
        org.starcoin.bifrost.types.CrossChainDepositEvent decodedEventData;
        //todo filter 'to_chain'
        try {
            decodedEventData = org.starcoin.bifrost.types.CrossChainDepositEvent
                    .bcsDeserialize(HexUtils.hexToByteArray(event.data));
        } catch (DeserializationError deserializationError) {
            throw new RuntimeException("Deserialize event data error.", deserializationError);
        }
        StcToEthereum stcToEthereum = new StcToEthereum();
        StarcoinEvent.copyProperties(event, decodedEventData, stcToEthereum);
        return stcToEthereum;
    }
}

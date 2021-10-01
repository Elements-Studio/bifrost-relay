package org.starcoin.bifrost.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.EthereumWithdrawStc;
import org.starcoin.bifrost.data.utils.IdUtils;
import org.starcoin.bifrost.ethereum.model.STC;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Type;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

import static org.starcoin.bifrost.subscribe.EthereumWithdrawSubscriber.TOPIC_CROSS_CHAIN_WITHDRAW_EVENT;

@Service
public class EthereumHandleLogService {
    private static final Logger LOG = LoggerFactory.getLogger(EthereumHandleLogService.class);

    @Autowired
    private EthereumLogService ethereumLogService;

    @Autowired
    private EthereumNodeHeartbeatService ethereumNodeHeartbeatService;

    private static EthereumWithdrawStc createEthereumWithdrawStc(LogWrapper log,
                                                                 String toAddress, BigInteger amount,
                                                                 String fromAddress, String ownerAddress,
                                                                 BigInteger fromChain) {
        EthereumWithdrawStc withdrawStc = new EthereumWithdrawStc();
        withdrawStc.setWithdrawAmount(amount);
        withdrawStc.setFromAccount(fromAddress);
        withdrawStc.setToAccount(toAddress);
        withdrawStc.setOwnerAccount(ownerAddress);
        withdrawStc.setFromChain(fromChain);
        withdrawStc.setAddress(log.getAddress());
        withdrawStc.setBlockHash(log.getBlockHash());
        withdrawStc.setTransactionHash(log.getTransactionHash());
        withdrawStc.setTransactionIndex(log.getTransactionIndex());
        withdrawStc.setLogIndex(log.getLogIndex());
        withdrawStc.setData(log.getData());
        withdrawStc.setTopics(log.getTopics());
        withdrawStc.setBlockNumber(log.getBlockNumber());
        withdrawStc.setLogId(IdUtils.generateLogId(withdrawStc));
        withdrawStc.setCreatedAt(System.currentTimeMillis());
        withdrawStc.setCreatedBy("ADMIN");
        withdrawStc.setUpdatedAt(withdrawStc.getCreatedAt());
        withdrawStc.setUpdatedBy(withdrawStc.getCreatedBy());
        return withdrawStc;
    }

    private EthereumWithdrawStc decodeLog(LogWrapper log) {
        if (!TOPIC_CROSS_CHAIN_WITHDRAW_EVENT.equals(log.getTopics().get(0))) {
            throw new RuntimeException("Decode wrong log type, topic: " + log.getTopics().get(0));
        }
        //todo filter by fromChain
        //event CrossChainWithdrawEvent(address indexed from, bytes20 to, address indexed owner, uint256 value, uint8 from_chain);
        List<Type> data = FunctionReturnDecoder.decode(log.getData(), STC.CROSSCHAINWITHDRAWEVENT_EVENT.getNonIndexedParameters());
        BigInteger amount = (BigInteger) data.get(1).getValue();
        BigInteger fromChain = (BigInteger) data.get(2).getValue();
        byte[] toAddress = (byte[]) data.get(0).getValue();
        String fromAddress = FunctionReturnDecoder.decode(log.getTopics().get(1), STC.CROSSCHAINWITHDRAWEVENT_EVENT.getIndexedParameters().subList(0, 1)).get(0).toString();
        String ownerAddress = FunctionReturnDecoder.decode(log.getTopics().get(2), STC.CROSSCHAINWITHDRAWEVENT_EVENT.getIndexedParameters().subList(1, 2)).get(0).toString();
        EthereumWithdrawStc withdrawStc = createEthereumWithdrawStc(
                log,
                Numeric.toHexString(toAddress), amount, fromAddress, ownerAddress, fromChain);
        return withdrawStc;
    }

    public void handle(LogWrapper log) {
        AbstractNodeHeartbeatService.runAndBeat(() -> {
            EthereumWithdrawStc withdrawStc = decodeLog(log);
            ethereumLogService.save(withdrawStc);
        }, this.ethereumNodeHeartbeatService, log.getBlockNumber());
    }

    public interface LogWrapper {

        String getAddress();

        String getBlockHash();

        String getTransactionHash();

        BigInteger getTransactionIndex();

        BigInteger getLogIndex();

        String getData();

        List<String> getTopics();

        BigInteger getBlockNumber();
    }
}

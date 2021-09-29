package org.starcoin.bifrost.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.AbstractNodeHeartbeat;
import org.starcoin.bifrost.data.model.EthereumNodeHeartbeat;
import org.starcoin.bifrost.data.model.NodeHeartbeatId;
import org.starcoin.bifrost.data.repo.EthereumNodeHeartbeatRepository;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class EthereumNodeHeartbeatService extends AbstractNodeHeartbeatService<EthereumNodeHeartbeat> {

    private final EthereumNodeHeartbeatRepository ethereumNodeHeartbeatRepository;

    @Autowired
    public EthereumNodeHeartbeatService(EthereumNodeHeartbeatRepository ethereumNodeHeartbeatRepository) {
        this.ethereumNodeHeartbeatRepository = ethereumNodeHeartbeatRepository;
    }

    @Override
    protected Function<NodeHeartbeatId, EthereumNodeHeartbeat> findByIdOrElseNullFunction() {
        return id -> ethereumNodeHeartbeatRepository.findById(id).orElse(null);
    }

    @Override
    protected Supplier<EthereumNodeHeartbeat> newNodeHeartbeatSupplier() {
        return EthereumNodeHeartbeat::new;
    }

    @Override
    protected Consumer<EthereumNodeHeartbeat> saveNodeHeartbeatConsumer() {
        return ethereumNodeHeartbeatRepository::save;
    }

    @Override
    protected Supplier<List<AbstractNodeHeartbeat.Breakpoint>> findBreakpointsSupplier() {
        return ethereumNodeHeartbeatRepository::findBreakpoints;
    }
}

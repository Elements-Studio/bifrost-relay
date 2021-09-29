package org.starcoin.bifrost.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.AbstractNodeHeartbeat;
import org.starcoin.bifrost.data.model.NodeHeartbeatId;
import org.starcoin.bifrost.data.model.StarcoinNodeHeartbeat;
import org.starcoin.bifrost.data.repo.StarcoinNodeHeartbeatRepository;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class StarcoinNodeHeartbeatService extends AbstractNodeHeartbeatService<StarcoinNodeHeartbeat> {

    private final StarcoinNodeHeartbeatRepository starcoinNodeHeartbeatRepository;

    @Autowired
    public StarcoinNodeHeartbeatService(StarcoinNodeHeartbeatRepository starcoinNodeHeartbeatRepository) {
        this.starcoinNodeHeartbeatRepository = starcoinNodeHeartbeatRepository;
    }

    @Override
    protected Function<NodeHeartbeatId, StarcoinNodeHeartbeat> findByIdOrElseNullFunction() {
        return id -> starcoinNodeHeartbeatRepository.findById(id).orElse(null);
    }

    @Override
    protected Supplier<StarcoinNodeHeartbeat> newNodeHeartbeatSupplier() {
        return StarcoinNodeHeartbeat::new;
    }

    @Override
    protected Consumer<StarcoinNodeHeartbeat> saveNodeHeartbeatConsumer() {
        return starcoinNodeHeartbeatRepository::save;
    }

    @Override
    protected Supplier<List<AbstractNodeHeartbeat.Breakpoint>> findBreakpointsSupplier() {
        return starcoinNodeHeartbeatRepository::findBreakpoints;
    }
}

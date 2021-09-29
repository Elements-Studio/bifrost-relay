package org.starcoin.bifrost.service;

import org.starcoin.bifrost.data.model.AbstractNodeHeartbeat;
import org.starcoin.bifrost.data.model.NodeHeartbeatId;
import org.starcoin.bifrost.data.model.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractNodeHeartbeatService<T extends AbstractNodeHeartbeat> {

    private final String nodeId = "0x" + UUID.randomUUID().toString().replace("-", "");

    private final AtomicReference<BigInteger> startedAt = new AtomicReference<>();

    protected abstract Function<NodeHeartbeatId, T> findByIdOrElseNullFunction();

    protected abstract Supplier<T> newNodeHeartbeatSupplier();

    protected abstract Consumer<T> saveNodeHeartbeatConsumer();

    protected abstract Supplier<List<AbstractNodeHeartbeat.Breakpoint>> findBreakpointsSupplier();

    public void reset() {
        startedAt.set(null);
    }

    public void beat(BigInteger timePoint) {
        if (startedAt.get() == null) {
            startedAt.set(timePoint);
        }
        T nodeHeartbeat = findByIdOrElseNullFunction().apply(new NodeHeartbeatId(nodeId, startedAt.get()));
        if (nodeHeartbeat == null) {
            nodeHeartbeat = newNodeHeartbeatSupplier().get();
            nodeHeartbeat.setNodeId(nodeId);
            nodeHeartbeat.setStartedAt(timePoint);
            nodeHeartbeat.setBeatenAt(timePoint);
            nodeHeartbeat.setCreatedAt(System.currentTimeMillis());
            nodeHeartbeat.setCreatedBy("admin");
            nodeHeartbeat.setUpdatedAt(nodeHeartbeat.getCreatedAt());
            nodeHeartbeat.setUpdatedBy(nodeHeartbeat.getCreatedBy());
        } else {
            if (timePoint.compareTo(nodeHeartbeat.getBeatenAt()) > 0) {
                nodeHeartbeat.setBeatenAt(timePoint);
                nodeHeartbeat.setUpdatedAt(System.currentTimeMillis());
                nodeHeartbeat.setUpdatedBy("admin");
            }
        }
        saveNodeHeartbeatConsumer().accept(nodeHeartbeat);
    }

    public List<Pair<BigInteger, BigInteger>> findBreakIntervals() {
        List<AbstractNodeHeartbeat.Breakpoint> points = findBreakpointsSupplier().get();
        List<Pair<BigInteger, BigInteger>> intervals = new ArrayList<>();
        Pair<BigInteger, BigInteger> interval = null;
        for (int i = 0; i < points.size(); i++) {
            AbstractNodeHeartbeat.Breakpoint point = points.get(i);
            BigInteger beatenAt = point.getBeatenAt();
            int isEndPoint = point.getIsEndPoint().intValue();
            if (isEndPoint == 1) {
                interval = new Pair<>();
                interval.setItem1(beatenAt);
            } else if (interval != null && interval.getItem1().compareTo(beatenAt) < 0) {
                interval.setItem2(beatenAt);
                intervals.add(interval);
                interval = null;
            }
        }
        return intervals;
    }

}

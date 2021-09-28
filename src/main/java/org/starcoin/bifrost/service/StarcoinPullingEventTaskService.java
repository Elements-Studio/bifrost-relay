package org.starcoin.bifrost.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.StarcoinPullingEventTask;
import org.starcoin.bifrost.data.repo.StarcoinPullingEventTaskRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class StarcoinPullingEventTaskService extends AbstractPullingBlockTaskService<StarcoinPullingEventTask> {

    private final StarcoinPullingEventTaskRepository starcoinPullingEventTaskRepository;

    @Autowired
    public StarcoinPullingEventTaskService(StarcoinPullingEventTaskRepository starcoinPullingEventTaskRepository) {
        this.starcoinPullingEventTaskRepository = starcoinPullingEventTaskRepository;
    }

    @Override
    protected Function<BigInteger, StarcoinPullingEventTask> findByIdOrElseNullFunction() {
        return id -> starcoinPullingEventTaskRepository.findById(id).orElse(null);
    }

    @Override
    protected Supplier<StarcoinPullingEventTask> newPullingTaskSupplier() {
        return StarcoinPullingEventTask::new;
    }

    @Override
    protected Consumer<StarcoinPullingEventTask> savePullingTaskConsumer() {
        return starcoinPullingEventTaskRepository::save;
    }

    @Override
    protected Function<String, List<StarcoinPullingEventTask>> findByStatusEqualsFunction() {
        return starcoinPullingEventTaskRepository::findByStatusEquals;
    }
}

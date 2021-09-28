package org.starcoin.bifrost.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.starcoin.bifrost.data.model.EthereumPullingLogTask;
import org.starcoin.bifrost.data.repo.EthereumPullingLogTaskRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class EthereumPullingLogTaskService extends AbstractPullingBlockTaskService<EthereumPullingLogTask> {

    private final EthereumPullingLogTaskRepository ethereumPullingLogTaskRepository;

    @Autowired
    public EthereumPullingLogTaskService(EthereumPullingLogTaskRepository ethereumPullingLogTaskRepository) {
        this.ethereumPullingLogTaskRepository = ethereumPullingLogTaskRepository;
    }

    @Override
    protected Function<BigInteger, EthereumPullingLogTask> findByIdOrElseNullFunction() {
        return id -> ethereumPullingLogTaskRepository.findById(id).orElse(null);
    }

    @Override
    protected Supplier<EthereumPullingLogTask> newPullingTaskSupplier() {
        return EthereumPullingLogTask::new;
    }

    @Override
    protected Consumer<EthereumPullingLogTask> savePullingTaskConsumer() {
        return ethereumPullingLogTaskRepository::save;
    }

    @Override
    protected Function<String, List<EthereumPullingLogTask>> findByStatusEqualsFunction() {
        return ethereumPullingLogTaskRepository::findByStatusEquals;
    }
}

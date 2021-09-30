package org.starcoin.bifrost.service;

import org.starcoin.bifrost.data.model.AbstractPullingBlockTask;
import org.starcoin.bifrost.utils.BeanUtils2;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public abstract class AbstractPullingBlockTaskService<T extends AbstractPullingBlockTask> {

    protected abstract Function<BigInteger, T> findByIdOrElseNullFunction();

    protected abstract Supplier<T> newPullingTaskSupplier();

    protected abstract Consumer<T> savePullingTaskConsumer();

    protected abstract Function<String, List<T>> findByStatusEqualsFunction();


    @Transactional
    public void createOrUpdatePullingEventTask(T originPullingTask) {
        if (originPullingTask.getFromBlockNumber() == null) {
            throw new IllegalArgumentException("Argument 'fromBlockNumber' is null");
        }
        T targetPullingTask = findByIdOrElseNullFunction().apply(originPullingTask.getFromBlockNumber()); //pullingEventTaskRepository.findById(pullingEventTask.getFromBlockNumber()).orElse(null);
        if (targetPullingTask == null) {
            targetPullingTask = newPullingTaskSupplier().get();
            targetPullingTask.setCreatedAt(System.currentTimeMillis());
            targetPullingTask.setCreatedBy("ADMIN");
            targetPullingTask.setUpdatedAt(targetPullingTask.getCreatedAt());
            targetPullingTask.setUpdatedBy(targetPullingTask.getCreatedBy());
        } else if (AbstractPullingBlockTask.STATUS_DONE.equalsIgnoreCase(targetPullingTask.getStatus())) {
            targetPullingTask.setUpdatedAt(System.currentTimeMillis());
            targetPullingTask.setUpdatedBy("ADMIN");
            targetPullingTask.resetStatus();
        }
        Set<String> props = Arrays.stream(new String[]{"fromBlockNumber", "toBlockNumber"}).collect(Collectors.toSet());
        BeanUtils2.copySpecificProperties(originPullingTask, targetPullingTask, props);
        savePullingTaskConsumer().accept(targetPullingTask); //pullingEventTaskRepository.save(targetEventTask);
    }

    @Transactional
    public void createOrUpdatePullingTask(BigInteger fromBlockNumber, BigInteger toBlockNumber) {
        T targetPullingTask = findByIdOrElseNullFunction().apply(fromBlockNumber); //pullingEventTaskRepository.findById(fromBlockNumber).orElse(null);
        if (targetPullingTask == null) {
            targetPullingTask = newPullingTaskSupplier().get();
            targetPullingTask.setFromBlockNumber(fromBlockNumber);
            targetPullingTask.setToBlockNumber(toBlockNumber);
            targetPullingTask.setCreatedAt(System.currentTimeMillis());
            targetPullingTask.setCreatedBy("ADMIN");
            targetPullingTask.setUpdatedAt(targetPullingTask.getCreatedAt());
            targetPullingTask.setUpdatedBy(targetPullingTask.getCreatedBy());
        } else if (AbstractPullingBlockTask.STATUS_DONE.equalsIgnoreCase(targetPullingTask.getStatus())) {
            targetPullingTask.setToBlockNumber(toBlockNumber);
            targetPullingTask.setUpdatedAt(System.currentTimeMillis());
            targetPullingTask.setUpdatedBy("ADMIN");
            targetPullingTask.resetStatus();
        }
        savePullingTaskConsumer().accept(targetPullingTask);//pullingEventTaskRepository.save(targetEventTask);
    }

    @Transactional
    public void updateStatusDone(T t) {
        t.done();
        t.setUpdatedBy("ADMIN");
        t.setUpdatedAt(System.currentTimeMillis());
        savePullingTaskConsumer().accept(t); //pullingEventTaskRepository.save(t);
    }

    @Transactional
    public List<T> getPullingTaskToProcess() {
        List<T> tasks = findByStatusEqualsFunction().apply(T.STATUS_CREATED); //pullingEventTaskRepository.findByStatusEquals(T.STATUS_CREATED);
        for (T t : tasks) {
            t.processing();
            savePullingTaskConsumer().accept(t);
        }
        return tasks;
    }

}

package jh.playground.ignite.computetask;

import jh.playground.ignite.domain.Valuation;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.compute.*;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.TaskSessionResource;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MultiTradesValuationTaskImpl extends ComputeTaskSplitAdapter<MultiTradesRequest, Collection<Valuation>> {
    @IgniteInstanceResource
    private Ignite ignite;

    @LoggerResource
    private IgniteLogger log;

    @TaskSessionResource
    private ComputeTaskSession taskSes;

//    @SpringResource(resourceName = "slowBean")
//    private transient SlowBean slowBean;

    @Override
    protected Collection<? extends ComputeJob> split(int gridSize, MultiTradesRequest req) throws IgniteException {
        AtomicInteger tradesValued = new AtomicInteger();
        return req.trades.stream().map(t -> new ComputeJobAdapter() {
            @Override
            public Object execute() throws IgniteException {
                String msg = String.format("%s (%s) valuing trade %s", taskSes.getTaskName(), taskSes.getId(), t);
                log.info(msg);

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return new Valuation(tradesValued.incrementAndGet(), req.valuationDate, 1.5, 2.5, t.tradeId);
            }
        }).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public Collection<Valuation> reduce(List<ComputeJobResult> results) throws IgniteException {
        return results.stream().map(ComputeJobResult::<Valuation>getData).collect(Collectors.toList());
    }
}

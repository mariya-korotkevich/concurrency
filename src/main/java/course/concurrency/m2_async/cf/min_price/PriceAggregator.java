package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        try {
            ExecutorService executor = Executors.newCachedThreadPool();
            List<CompletableFuture<Double>> completableFutures = shopIds.stream()
                    .map(shopId -> CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
                            .exceptionally(throwable -> Double.NaN))
                    .collect(Collectors.toList());
            executor.shutdown();
            executor.awaitTermination(2900, TimeUnit.MILLISECONDS);
            return completableFutures.stream()
                    .filter(CompletableFuture::isDone)
                    .map(CompletableFuture::join)
                    .min(Double::compareTo).orElse(Double.NaN);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

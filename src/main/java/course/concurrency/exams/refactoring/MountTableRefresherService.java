package course.concurrency.exams.refactoring;

import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class MountTableRefresherService {

    private Others.RouterStore routerStore = new Others.RouterStore();
    private long cacheUpdateTimeout;

    /**
     * All router admin clients cached. So no need to create the client again and
     * again. Router admin address(host:port) is used as key to cache RouterClient
     * objects.
     */
    private Others.LoadingCache<String, Others.RouterClient> routerClientsCache;

    /**
     * Removes expired RouterClient from routerClientsCache.
     */
    private ScheduledExecutorService clientCacheCleanerScheduler;

    public void serviceInit() {
        long routerClientMaxLiveTime = 15L;
        this.cacheUpdateTimeout = 10L;
        routerClientsCache = new Others.LoadingCache<String, Others.RouterClient>();
        routerStore.getCachedRecords().stream().map(Others.RouterState::getAdminAddress)
                .forEach(addr -> routerClientsCache.add(addr, new Others.RouterClient()));

        initClientCacheCleaner(routerClientMaxLiveTime);
    }

    public void serviceStop() {
        clientCacheCleanerScheduler.shutdown();
        // remove and close all admin clients
        routerClientsCache.cleanUp();
    }

    private void initClientCacheCleaner(long routerClientMaxLiveTime) {
        ThreadFactory tf = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread();
                t.setName("MountTableRefresh_ClientsCacheCleaner");
                t.setDaemon(true);
                return t;
            }
        };

        clientCacheCleanerScheduler =
                Executors.newSingleThreadScheduledExecutor(tf);
        /*
         * When cleanUp() method is called, expired RouterClient will be removed and
         * closed.
         */
        clientCacheCleanerScheduler.scheduleWithFixedDelay(
                () -> routerClientsCache.cleanUp(), routerClientMaxLiveTime,
                routerClientMaxLiveTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Refresh mount table cache of this router as well as all other routers.
     */
    public void refresh() {

        List<Others.RouterState> cachedRecords = routerStore.getCachedRecords();

        Map<String, Others.MountTableManager> managers = cachedRecords.stream()
                .map(Others.RouterState::getAdminAddress)
                .filter(adminAddress -> !ObjectUtils.isEmpty(adminAddress))
                .collect(Collectors.toMap(address -> address,
                        address -> isLocalAdmin(address) ? createTableManager("local") : createTableManager(address)));

        if (!managers.isEmpty()) {
            invokeRefresh(managers);
        }
    }

    protected Others.MountTableManager createTableManager(String address) {
        return new Others.MountTableManager(address);
    }

    private void removeFromCache(String adminAddress) {
        routerClientsCache.invalidate(adminAddress);
    }

    private void invokeRefresh(Map<String, Others.MountTableManager> managers) {
        Map<String, CompletableFuture<Boolean>> futures = managers.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> CompletableFuture.supplyAsync(() -> entry.getValue().refresh())
                        .orTimeout(cacheUpdateTimeout, TimeUnit.MILLISECONDS)));
        logResult(futures);
    }

    private boolean isLocalAdmin(String adminAddress) {
        return adminAddress.contains("local");
    }

    private void logResult(Map<String, CompletableFuture<Boolean>> futures) {
        int successCount = 0;
        int failureCount = 0;
        boolean isInterrupted = false;
        boolean notAllUpdated = false;
        for (var entry : futures.entrySet()) {
            try {
                if (Boolean.TRUE.equals(entry.getValue().get())) {
                    successCount++;
                    continue;
                }
            } catch (InterruptedException e) {
                isInterrupted = true;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof TimeoutException) {
                    notAllUpdated = true;
                }
            }
            failureCount++;
            // remove RouterClient from cache so that new client is created
            removeFromCache(entry.getKey());
        }
        if (notAllUpdated) {
            log("Not all router admins updated their cache");
        }
        if (isInterrupted) {
            log("Mount table cache refresher was interrupted.");
        }
        log(String.format(
                "Mount table entries cache refresh successCount=%d,failureCount=%d",
                successCount, failureCount));
    }

    public void log(String message) {
        System.out.println(message);
    }

    public void setCacheUpdateTimeout(long cacheUpdateTimeout) {
        this.cacheUpdateTimeout = cacheUpdateTimeout;
    }

    public void setRouterClientsCache(Others.LoadingCache cache) {
        this.routerClientsCache = cache;
    }

    public void setRouterStore(Others.RouterStore routerStore) {
        this.routerStore = routerStore;
    }
}
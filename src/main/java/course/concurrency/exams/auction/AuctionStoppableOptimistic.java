package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(null, null, Long.MIN_VALUE));
    private volatile boolean stopped = false;

    public boolean propose(Bid bid) {
        Bid previousBid;
        do {
            previousBid = latestBid.get();
            if (stopped || bid.getPrice() <= previousBid.getPrice()) {
                return false;
            }
        } while (!stopped && !latestBid.compareAndSet(previousBid, bid));
        if (previousBid.getId() != null){
            notifier.sendOutdatedMessage(previousBid);
        }
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }

    public Bid stopAuction() {
        stopped = true;
        return latestBid.get();
    }
}

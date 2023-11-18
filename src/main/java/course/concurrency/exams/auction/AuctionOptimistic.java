package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private final Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }
    private final AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(null, null, Long.MIN_VALUE));

    public boolean propose(Bid bid) {
        Bid previousBid;
        do {
            previousBid = latestBid.get();
            if (bid.getPrice() <= previousBid.getPrice()) {
                return false;
            }
        } while (!latestBid.compareAndSet(previousBid, bid));
        if (previousBid.getId() != null){
            notifier.sendOutdatedMessage(previousBid);
        }
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}

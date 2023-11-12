package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }
    private AtomicReference<Bid> latestBid = new AtomicReference<>(null);

    public boolean propose(Bid bid) {
        while (true) {
            Bid last = latestBid.get();
            if (last == null || bid.getPrice() > last.getPrice()) {
                if (latestBid.compareAndSet(last, bid)) {
                    notifier.sendOutdatedMessage(last);
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}

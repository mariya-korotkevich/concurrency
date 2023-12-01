package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }
    private final AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference<>(
            new Bid(null, null, Long.MIN_VALUE), false);

    public boolean propose(Bid bid) {
        Bid previousBid;
        do {
            previousBid = latestBid.getReference();
            if (latestBid.isMarked() || bid.getPrice() <= previousBid.getPrice()) {
                return false;
            }
        } while (!latestBid.compareAndSet(previousBid, bid, false, false));
        if (previousBid.getId() != null){
            notifier.sendOutdatedMessage(previousBid);
        }
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    public Bid stopAuction() {
        Bid lastBid;
        do {
            lastBid = latestBid.getReference();
        } while (!latestBid.attemptMark(lastBid, true));
        return lastBid;
    }
}

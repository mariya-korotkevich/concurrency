package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private final Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }
    private final AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(null, null, Long.MIN_VALUE));

    public boolean propose(Bid bid) {
        Bid last;
        boolean successBid = false;
        do {
            last = latestBid.get();
            if (bid.getPrice() <= last.getPrice()) {
                break;
            }
            successBid = latestBid.compareAndSet(last, bid);
        } while (!successBid);
        if (successBid && last.getId() != null){
            notifier.sendOutdatedMessage(last);
        }
        return successBid;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}

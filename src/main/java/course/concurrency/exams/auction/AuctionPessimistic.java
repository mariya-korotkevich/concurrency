package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {

    private final Notifier notifier;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid = new Bid(null, null, Long.MIN_VALUE);
    public boolean propose(Bid bid) {
        Bid last = null;
        synchronized (this) {
            if (bid.getPrice() > latestBid.getPrice()) {
                last = latestBid;
                latestBid = bid;
            }
        }
        if (last != null && last.getId() != null) {
            notifier.sendOutdatedMessage(last);
        }
        return last != null;
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}

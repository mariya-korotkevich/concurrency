package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {

    private final Notifier notifier;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid = new Bid(null, null, Long.MIN_VALUE);
    public boolean propose(Bid bid) {
        if (bid.getPrice() <= latestBid.getPrice()) {
            return false;
        }

        Bid previousBid;
        synchronized (this) {
            if (bid.getPrice() <= latestBid.getPrice()) {
                return false;
            }
            previousBid = latestBid;
            latestBid = bid;
        }

        if (previousBid.getId() != null) {
            notifier.sendOutdatedMessage(previousBid);
        }
        return true;
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}

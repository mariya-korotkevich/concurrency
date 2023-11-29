package course.concurrency.exams.auction;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid = new Bid(null, null, Long.MIN_VALUE);
    private volatile boolean stopped = false;

    public boolean propose(Bid bid) {
        if (stopped || bid.getPrice() <= latestBid.getPrice()) {
            return false;
        }

        Bid previousBid;
        synchronized (this) {
            if (stopped || bid.getPrice() <= latestBid.getPrice()) {
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

    public Bid stopAuction() {
        synchronized (this) {
            stopped = true;
        }
        return latestBid;
    }
}

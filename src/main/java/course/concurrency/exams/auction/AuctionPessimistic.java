package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {

    private Notifier notifier;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid;

    public boolean propose(Bid bid) {
        synchronized (this) {
            if (latestBid == null || bid.getPrice() > latestBid.getPrice()) {
                if (latestBid != null) {
                    notifier.sendOutdatedMessage(latestBid);
                }
                latestBid = bid;
                return true;
            }
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}

package org.scalasino.roulette;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.concurrent.ExecutionContext;
import scala.runtime.BoxedUnit;

import java.io.Serializable;

public class WalletClient extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);
    private final ExecutionContext ec = context().dispatcher();

    private WalletService walletService;

    private PartialFunction<Object, BoxedUnit> withdrawalRetrying;
    private PartialFunction<Object, BoxedUnit> withdrawalRollingBack;
    private PartialFunction<Object, BoxedUnit> depositRetrying;

    public static class BetAndWin implements Serializable {
        private int bet;
        private int win;
        private String id;

        public BetAndWin(int bet, int win, String id) {
            this.bet = bet;
            this.win = win;
            this.id = id;
        }

        public int getBet() {
            return bet;
        }

        public int getWin() {
            return win;
        }

        public String getId() {
            return id;
        }
    }

    public static class BetSuccess implements Serializable {
        private String betId;
    }

    public static class BetReject implements Serializable {
        private String betId;
    }

    public static class BetError implements Serializable {
        private String betId;
    }

    public static class BetFailure implements Serializable {
        private String betId;
    }

    public static class WinSuccess implements Serializable {
        private String winId;
    }

    public WalletClient() {
        receive(ReceiveBuilder.
                match(BetAndWin.class, baw -> {
                    ActorRef sender = sender();
                    ActorRef self = self();
                    walletService.withdraw(baw.getId() + "_bet").onSuccess(new OnSuccess<String>() {
                        public void onSuccess(String result) {
                            sender.tell(new BetSuccess(), self);
                            context().become(depositRetrying);
                        }
                    }, ec);

                }).
                matchAny(o -> log.info("received unknown message")).build());
    }

/*    private PartialFunction<Object, BoxedUnit> withdrawalRetrying(BetAndWin baw) {
        return ReceiveBuilder.
                match(BetSuccess.class, s -> {
                    context().become(depositRetrying);
                }).
                matchAny(o -> log.info("received unknown message")).build();
    }*/
}

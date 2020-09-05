package SplitUtil;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author: ffzs
 * @Date: 2020/9/5 上午11:09
 */

public class SplitWriter extends AbstractBehavior<SplitWriter.Command> {


    interface Command {}

    public SplitWriter(ActorContext<Command> context, int counter) {
        super(context);
        this.counter = counter;
    }

    private int counter;

    public static Behavior<Command> create (int counter) {
        return Behaviors.setup(context -> new SplitWriter(context, counter));
    }


    public static final class Write2File implements SplitWriter.Command{
        public final String line;
        public final BufferedWriter bw;

        public Write2File(String line, BufferedWriter bw) {
            this.line = line;
            this.bw = bw;
        }
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Write2File.class, this::onWrite2File)
                .build();
    }

    private Behavior<Command> onWrite2File(Write2File info) throws IOException {
        info.bw.write(info.line);
        if (counter++ >= 50) {
            info.bw.flush();
            counter=0;
        }
        return this;
    }
}

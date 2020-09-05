package SplitUtil;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: ffzs
 * @Date: 2020/9/5 上午10:06
 */


public class SplitFile extends AbstractBehavior<SplitFile.Command> {

    interface Command {}

    public SplitFile(ActorContext<Command> context) {
        super(context);
        getContext().getLog().info("开始 SplitFile");
    }

    public static Behavior<SplitFile.Command> create () {
        return Behaviors.setup(SplitFile::new);
    }

    Map<String, ActorRef<SplitWriter.Command>> actorMap = new HashMap<>();
    Map<String, BufferedWriter> bufferMap = new HashMap<>();

    public static final class Fileline implements SplitFile.Command {
        public final String filename;
        public final String filepath;
        public final String line;

        public Fileline(String filename, String line, String filepath) {
            this.filename = filename;
            this.filepath = filepath;
            this.line = line;
        }
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Fileline.class, this::onPost2Actor)
                .onSignal(PostStop.class, sig -> onPostStop())
                .build();
    }

    private Behavior<Command> onPostStop() {
        bufferMap.forEach((k, v) -> {
            try {
                v.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        getContext().getLog().info("执行完毕");
        return Behaviors.stopped();
    }

    private Behavior<Command> onPost2Actor(Fileline info) throws IOException {
        if (actorMap.containsKey(info.filename)) {
            ActorRef<SplitWriter.Command> actor = actorMap.get(info.filename);
            actor.tell(new SplitWriter.Write2File(info.line, bufferMap.get(info.filename)));
        }
        else {
            getContext().getLog().info("为 {} 创建 actor", info.filename);
            BufferedWriter bf = Files.newBufferedWriter(Path.of(info.filepath));
            bufferMap.put(info.filename, bf);
            ActorRef<SplitWriter.Command> actor = getContext().spawn(SplitWriter.create(0), info.filename);
            actorMap.put(info.filename, actor);
            actor.tell(new SplitWriter.Write2File(info.line, bf));
        }
        return this;
    }
}

package SplitUtil;

import akka.actor.typed.ActorSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author: ffzs
 * @Date: 2020/9/5 上午11:31
 */


public class Main {

    private static String transName (String name) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char it = name.charAt(i);
            if (( it>='a' && it <='z')||(it>='A' && it <='Z')) ret.append(it);
            else ret.append('-');
        }
        return ret.toString();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        ActorSystem<SplitFile.Command> system = ActorSystem.create(SplitFile.create(), "root");
        Files.lines(Path.of("test_data.csv"))
                .forEach(line -> {
                    String[] arrStr = line.split(",");
                    String filename = transName(arrStr[3] + "_" +arrStr[11]);
                    String filePath = "./tmp/"+arrStr[3] + "_" +arrStr[11]+".csv";
                    system.tell(new SplitFile.Fileline(filename, line+"\n", filePath));
                });
        Thread.sleep(200);
        system.terminate();
    }
}

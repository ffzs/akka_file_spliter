import DeviceSystem.Device;
import DeviceSystem.DeviceManager;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import org.junit.Test;

/**
 * @author: ffzs
 * @Date: 2020/9/4 下午2:40
 */
public class AkkaTest {

    public static Behavior<Void> create () {
        return Behaviors.setup(
                context -> {
                    context.getLog().info("...");
                    Behavior<Device.Command> device1 = Device.create("G1","D1");
//                    Behavior<DeviceSystem.Device.Command> device2 = DeviceSystem.Device.create("G1","D2");
//                    ActorRef<DeviceSystem.Device.Command> d1 = context.spawn(device1, "D1");
                    ActorRef<DeviceManager.DeviceRegistered> register = context.spawn(Behaviors.empty(), "D1");
                    ActorRef<DeviceManager.Command> manager = context.spawn(DeviceManager.create(), "manager");
                    context.watch(manager);
                    manager.tell(new DeviceManager.RequestTrackDevice("G1", "D1", register));
//                    chatRoom.tell(new ChatRoom.GetSession("ol’ Gabbler", gabbler));

                    return Behaviors.receive(Void.class)
//                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    @Test
    public void justTest(){
        ActorSystem.create(AkkaTest.create(), "manager");
    }
}

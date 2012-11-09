import java.util.LinkedList;

import org.powerbot.core.script.ActiveScript;
import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.GroundItems;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.node.GroundItem;

@Manifest(name = "Ash Collector", description = "Collecting Ashes and bank them", version = 1.00, authors = {"Shachar"}, hidden = false)

public class AshCollector extends ActiveScript
{
    /* NODES */
    private static final LinkedList<Node> nodes = new LinkedList<Node>();
    public static int ashesID = 592;

    @Override
    public void onStart()
    {
        nodes.add(new Collect());
        nodes.add(new Banking());
    }

    public void onStop()
    {

    }

    @Override
    public int loop()
    {
        if(nodes == null || nodes.size() <= 0)
            return Random.nextInt(150, 250);

        for(final Node job : nodes.toArray(new Node[nodes.size()]))
        {
            if(job.activate())
            {
                getContainer().submit(job);
                job.join();
            }
        }

        return Random.nextInt(150, 250);
    }

    public class Collect extends Node
    {
        @Override
        public boolean activate() {
            if (!Inventory.isFull() && GroundItems.getNearest(ashesID) != null)
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        @Override
        public void execute() {
            GroundItem ashes = GroundItems.getNearest(ashesID);

            final int n = Inventory.getCount();

            if(ashes.isOnScreen())
            {
                if (ashes.interact("Take"))
                {
                    Timer timer = new Timer(Random.nextInt(3500, 4500));
                    while(timer.isRunning() && Inventory.getCount() == n)
                        Task.sleep(80, 120);
                }
            }
            else // not on screen
            {
                if(Walking.walk(ashes))
                {
                    Timer timer = new Timer(Random.nextInt(3500, 4500));
                    while(timer.isRunning() && Calculations.distanceTo(Walking.getDestination()) > 8)
                        Task.sleep(80, 120);
                }
            }
        }
    }

    public class Banking extends Node
    {

        @Override
        public boolean activate() {
            return Inventory.isFull();
        }

        @Override
        public void execute() {
            if (!Bank.isOpen())
            {
                if (Bank.open())
                {
                    final Timer timer = new Timer(Random.nextInt(3500, 4500));
                    while (timer.isRunning() && !Bank.isOpen() && Calculations.distanceTo(Walking.getDestination()) > 6)
                    {
                        Task.sleep(80, 120);
                    }
                }
            }
            else
            {
                if (Bank.depositInventory())
                {
                    Timer timer = new Timer(Random.nextInt(1000, 2000));
                    while (timer.isRunning() && Inventory.isFull())
                    {
                      Task.sleep(80, 120);
                    }
                }
            }
        }
    }
}
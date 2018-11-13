package TokenRing;

import java.util.Scanner;

public class AddQueue implements Runnable {
    MessageQueue queue;

    public AddQueue(MessageQueue q) {
        queue = q;
    }

    Scanner k = new Scanner(System.in);

    @Override
    public void run() {
        while (true) {

            System.out.println("Message: ");
            String b = k.nextLine();
            try {
                queue.AddMessage(b);
                System.out.println("Total messages in queue: " + queue.Size());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

package TokenRing;

import java.util.Scanner;

public class AddQueue implements Runnable {
	MessageQueue queue;

	public AddQueue(MessageQueue q){
		queue = q;
	}
	Scanner a = new Scanner(System.in);
	
	@Override
	public void run() {
		while(true){
			
			System.out.println("Message: ");
			String b = a.nextLine();			
			try {
				queue.AddMessage(b);
				System.out.println("Total messages in queue: " + queue.Size());
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
}

package TokenRing;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/* Esta classe deve implementar uma fila de mensagens. Observe que esta fila será
 * acessada por um consumidor (MessageSender) e um produtor (Classe principal, TokenRing).
 * Portanto, implemente controle de acesso (sincronização), para acesso a fila. 
 */

public class MessageQueue {
    /*Implemente uma estrutura de dados para manter uma lista de mensagens em formato string. 
     * Você pode, por exemplo, usar um ArrayList(). 
     * Não se esqueça que em uma fila, o primeiro elemente a entrar será o primeiro
     * a ser removido.
    */
	Semaphore tr = new Semaphore(1);
    ArrayList<String> queue = new ArrayList<>();

    public void AddMessage(String message) throws InterruptedException{
        /* Adicione a mensagem no final da fila. Não se esqueça de garantir que apenas uma thread faça isso 
        por vez. */
    	tr.acquire();
        queue.add(message);
       
       tr.release();
    }
    
    public String RemoveMessage() throws InterruptedException{ 
        
        /* Retire uma mensagem do inicio da fila. Não se esqueça de garantir que apenas uma thread faça isso 
        por vez.  */  
    	
    	return queue.remove(0);
  
        
    }
    
    public int Size(){
    	return queue.size();
    }
    

    
}

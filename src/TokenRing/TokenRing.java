package TokenRing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenRing {

    public static void main(String[] args) throws IOException, InterruptedException {
        String ip_port;
        int port;
        int t_token = 0;
        boolean token = false;
        String nickname;

        /* Le arquivo de configuração. */
        try (BufferedReader inputFile = new BufferedReader(new FileReader("ring.cfg"))) {

            /* Lê IP e Porta */
            ip_port = inputFile.readLine();
            String aux[] = ip_port.split(":");
            port = Integer.parseInt(aux[1]);

            /* Lê apelido */
            nickname = inputFile.readLine();

			/* Lê tempo de espera com o token. Usado para fins de depuração. Em caso de 
            execução normal use valor 0. */
            t_token = Integer.parseInt(inputFile.readLine());

            /* Lê se k estação possui o token inicial. */
            token = Boolean.parseBoolean(inputFile.readLine());

        } catch (FileNotFoundException ex) {
            Logger.getLogger(TokenRing.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        MessageQueue queue = new MessageQueue();
        String myNick = "Teste";
        MessageController controller = new MessageController(queue, ip_port, t_token, token, nickname, myNick);
        Thread thr_controller = new Thread(controller);
        Thread thr_receiver = new Thread(new MessageReceiver(queue, port, controller));
        Thread thr_addQueue = new Thread(new AddQueue(queue));
        thr_controller.start();
        thr_receiver.start();
        thr_addQueue.start();

        /* Cria uma fila de mensagens. */
        /*
        MessageQueue queue = new MessageQueue();
        String nickLocal = "Teste";
        MessageController controller = new MessageController(queue, ip_port, t_token, token, nickname, nickLocal);
        Thread thr_controller = new Thread(controller);
        Thread thr_receiver = new Thread(new MessageReceiver(queue, port, controller));
        Thread thr_addQueue = new Thread(new AddQueue(queue));
        thr_controller.start();
        thr_receiver.start();
        thr_addQueue.start();


        /*
         *
         *  Neste ponto, k thread principal deve ficar aguarando o usuário entrar com o destinatário
         * e k mensagem k ser enviada. Destinatário e mensagem devem ser adicionados na fila de mensagens pendentes.
         * MessageQueue()
         *
         */
    }
}



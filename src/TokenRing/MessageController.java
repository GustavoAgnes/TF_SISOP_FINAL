package TokenRing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class MessageController implements Runnable {
    private MessageQueue queue; /*Tabela de roteamento */
    private InetAddress IPAddress;
    private int port;
    private Semaphore WaitForMessage, semAux;
    private String nickname;
    private int time_token;
    private Boolean token;
    private String myNick;
    private int count;

    public MessageController(MessageQueue q,
                             String ip_port,
                             int t_token,
                             Boolean t,
                             String n,
                             String myN) throws UnknownHostException {

        queue = q;
        String aux[] = ip_port.split(":");
        IPAddress = InetAddress.getByName(aux[0]);
        port = Integer.parseInt(aux[1]);
        time_token = t_token;
        token = t;
        nickname = n;
        WaitForMessage = new Semaphore(0);
        semAux = new Semaphore(0);
        myNick = myN;
        count = 0;

    }

    /**
     * ReceiveMessage()
     * Nesta função, vc deve decidir o que fazer com k mensagem recebida do vizinho da esquerda:
     * Se for um token, é k sua chance de enviar uma mensagem de sua fila (queue);
     * Se for uma mensagem de dados e se for para esta estação, apenas k exiba no console, senão,
     * envie para seu vizinho da direita;
     * Se for um ACK e se for para você, sua mensagem foi enviada com sucesso, passe o token para o vizinho da direita, senão,
     * repasse o ACK para o seu vizinho da direita.
     *
     * @throws InterruptedException
     */
    public void ReceivedMessage(String msg) throws InterruptedException {
        DatagramSocket clientSocket = null;
        /* Cria socket para envio de mensagem */
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        if (count < 4) { //Após 3 tentativas de retransmissão, essa mensagem deve ser excluída e um token deve ser enviado para seu vizinho da direita.
            if (msg.equals("4060")) {
                token = true;
                while (token) {
                    sleep(100); //threads nao funcionam corretamente sem o sleep
                    if (queue.size() > 0) {
                        System.out.println(queue.size());
                        String msgs = null;
                        msgs = queue.removeMessage();
                        if (msgs != null) {
                            byte[] sendDatas = msgs.getBytes();

                            DatagramPacket sendPacket = new DatagramPacket(sendDatas, sendDatas.length, IPAddress, port);

                            try {
                                clientSocket.send(sendPacket);
                                System.out.println("Transfering message: " + msgs);
                            } catch (IOException ex) {
                                Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        token = false;
                    }
                }
            }
            /* Se for uma mensagem de dados e se for para esta estação, apenas k exiba no console, senão,
             * envie para seu vizinho da direita;*/
            else {
                String[] auxMsg = msg.split(";");
                String[] auxMsg2 = new String[0];
                //   System.out.println("auxMsg lenght: " + auxMsg.length);
                if (auxMsg.length > 1) {
                    //      System.out.println("auxMsg: " + auxMsg[1].toString());
                    auxMsg2 = auxMsg[1].split(":");
                }
                if (auxMsg[0].equals("4066") && auxMsg2.length > 0) {
                    if (myNick.equals(auxMsg2[1])) {
                        System.out.println("***\nThis machine received a message from " + auxMsg2[0] + ", the message is: " + auxMsg2[2] + "\n***");
                        msg = "ACK;" + auxMsg2[0];
                        byte[] sendData = msg.getBytes();

                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

                        try {
                            clientSocket.send(sendPacket);
                            System.out.println("Sending ACK.");
                        } catch (IOException ex) {
                            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (myNick.equals(auxMsg2[0])) {
                        System.out.println("Meu nick: " + myNick);
                        System.out.println("Aux msg2: " + auxMsg2[0]);
                        retransfer(msg, clientSocket);
                    } else {
                        System.out.println("CAIU NO ELSE");
                        transferMessage(msg, clientSocket);

                        /*
                        //
                        byte[] sendData = msg.getBytes();

                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        try {
                            clientSocket.send(sendPacket);
                            System.out.println("***\nMessage being transfered!\n***");
                            count++;
                        } catch (IOException ex) {
                            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        */
                    }
                }
                /*Se for um ACK e se for para você, sua mensagem foi enviada com sucesso, passe o token para o vizinho da direita, senão,
                 * repasse o ACK para o seu vizinho da direita.*/
                else if (auxMsg[0].equals("ACK") && auxMsg2.length > 0) {
                    //   System.out.println("Mensagem[0] é: " + auxMsg[0].toString());
                    //   System.out.println("aa tem tamanho: "+auxMsg2.length);
                    //   System.out.println("Mensagem aa[0] é: " + auxMsg2[0].toString());
                    //   System.out.println("Length auxMsg2" + auxMsg2.length);
                    if (auxMsg2[0].trim().equalsIgnoreCase((myNick.trim()))) {
                        msg = "4060";
                        byte[] sendData = msg.getBytes();

                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

                        try {
                            clientSocket.send(sendPacket);
                            System.out.println("***\nACK Received\n*** \nSending Token\n***");
                            token = false;
                        } catch (IOException ex) {
                            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        byte[] sendData = msg.getBytes();

                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

                        try {
                            clientSocket.send(sendPacket);
                            System.out.println("***\nACK being Resent\n***");
                        } catch (IOException ex) {
                            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        } else {
            count = 0;
            msg = "4060";

            byte[] sendData = msg.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

            try {
                clientSocket.send(sendPacket);
                System.out.println("Token being transfered");
                token = false;
            } catch (IOException ex) {
                Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        semAux.release();
    }

    public void retransfer(String msg, DatagramSocket clientSocket) { //4066;Teste:Teste2:Teste caiu nesse método
        byte[] sendData = msg.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

        try {
            clientSocket.send(sendPacket);
            System.out.println("***\nRetransfer attemp number: " + count + "\n***");
            count++;
        } catch (IOException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void transferMessage(String msg, DatagramSocket clientSocket) {
        //System.out.println("CAIU NO ELSE");
        byte[] sendData = msg.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        try {
            clientSocket.send(sendPacket);
            System.out.println("***\nMessage being transfered!\n***");
            count++;
        } catch (IOException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        DatagramSocket clientSocket = null;
        byte[] sendData;


        /* Cria socket para envio de mensagem */
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        while (true) {

			/* Neste exemplo, considera-se que k estação sempre recebe o token
               e o repassa para k próxima estação. */

            try {
				/* Espera time_token segundos para o envio do token. Isso é apenas para depuração,
                   durante execução real faça time_token = 0,*/
                sleep(time_token * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (token == true) {
                /* Converte string para array de bytes para envio pelo socket. */
                String msg = "4060"; /* Lembre-se do protocolo, "4060" é o token! */
                sendData = msg.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

                /* Realiza envio da mensagem. */
                try {
                    clientSocket.send(sendPacket);
                } catch (IOException ex) {
                    Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            /* A estação fica aguardando k ação gerada pela função ReceivedMessage(). */
            try {
                WaitForMessage.acquire();


            } catch (InterruptedException ex) {
                Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

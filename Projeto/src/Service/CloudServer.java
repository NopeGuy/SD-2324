package Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.ServerCloneException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CloudServer {

    private static HashMap<Integer, RemoteServer> serversConnected = new HashMap<>();

    private static HashMap<String, Connection> clientsConnected = new HashMap<>();

    private static Queue<Task> taskQueue = new ArrayDeque<>();

    private static HashMap<Integer, Task> tasksHistory = new HashMap<>();

    private static ReentrantLock connectionsLock = new ReentrantLock();

    private static ReentrantLock queueLock = new ReentrantLock();

    private static Condition queueChange = queueLock.newCondition();

    private static int taskId = 0;
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket server = new ServerSocket(10080);
        tasksExecutor();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));


        while (1 == 1) {
            System.out.println("CLOUD SERVER MANAGER OPENED ON PORT 10080");
            Socket socket = server.accept();
            Connection con = new Connection(socket);

            Runnable cloudServer = () -> {
                try (con){
                    while(true) {
                        Frame f  = con.receive();
                        System.out.println("Frame f = " + f.tag + " recebida");
                        if(f.tag == 90) {
                            connectionsLock.lock();
                            RemoteServer s = new RemoteServer(serversConnected.size(), Integer.parseInt(new String(f.data)), con);
                            serversConnected.put(serversConnected.size(), s);
                            con.sendString(91, s.serverId, "");
                            connectionsLock.unlock();
                            System.out.println("Servidor conectado");

                            // StandaloneServer a comunicar com o Cloudserver
                        } else if(f.tag == 1001) {
                            try {
                                connectionsLock.lock();
                                System.out.println("Servidor recebeu alguma coisa do StandaloneServer");
                                Task t = tasksHistory.get(f.taskid);
                                serversConnected.get(t.executorServer).addMemory(t.getMemory());
                                t.c.sendString(31, f.taskid, "A task com id = " + f.taskid + " terminou com sucesso..");
                            } finally {
                                connectionsLock.unlock();
                            }
                        } else if (f.tag == 100 ){
                            handleClient(con);
                        }



                    }
                } catch( IOException error ){
                    System.out.println("Error com o servidor " + socket.getLocalAddress() + " :" + error.getMessage());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            };

            new Thread(cloudServer).start();

        }
    }


    private static void handleClient(Connection clientConnection) throws IOException, ClassNotFoundException {

        File u = new File("users.data");
        final Users users;


        if(u.exists())
            users = Users.deserialize("users.data");
        else
            users = new Users();


        connectionsLock.lock();
        clientsConnected.put(clientConnection.getClientInfo(), clientConnection);
        connectionsLock.unlock();

        try {
            while (true) {
                System.out.println("A espera no handleClient");
                Frame f = clientConnection.receive();
                if( f.tag == 1 ) {
                    String[] user_pass = new String(f.data).split(";;");
                    String recieved_email = user_pass[0];
                    String recieved_pass = user_pass[1];

                    if( !users.validatePassword(recieved_email, recieved_pass)){
                        clientConnection.sendString( 2,0,"ERRO: Email e Password incorretos...");
                    } else {
                        clientConnection.sendString( 2, 0,"Email e Password corretos");
                    }
                    // Register Request
                }
                else if ( f.tag == 2 ){
                    String[] user_pass = new String(f.data).split(";;");
                    String recieved_user = user_pass[0];
                    String recieved_pass = user_pass[1];

                    //users.addAccount(recieved_user, recieved_pass);
                    //users.serialize("users.data");
                    clientConnection.sendData(3, 0, "User criado".getBytes());
                    //username = recieved_user;

                    //  Executar JOB
                }
                else if( f.tag == 30 ) {
                    try {
                        queueLock.lock();
                        Task t = new Task(taskId, f.data, clientConnection);
                        taskQueue.add(t);
                        tasksHistory.put(taskId++, t);
                        if(Client.DEBUG){ System.out.println("Adicionar task à Queue"); }
                        queueChange.signal();
                        if(Client.DEBUG){ System.out.println("Signaling"); }

                    }finally {
                        queueLock.unlock();
                    }
                    // Get status do Servidor
                }
                else if (f.tag == 40) {
                    connectionsLock.lock();
                    String status = "";
                    for(Map.Entry<Integer, RemoteServer> server : serversConnected.entrySet()){
                        RemoteServer s = server.getValue();
                        status += "Servidor " + s.serverId + ": " + s.availiableMemory + "/" + s.serverMemory + "\n";
                    }
                    if(status.equals( "")) status = "Sem servidores conectados.";
                    connectionsLock.unlock();
                    clientConnection.sendString(41, 0, status);
                    // Alguma tag não existente
                } else {
                    clientConnection.sendString(-1, 0, "Ainda não foi implementado...");
                }
                f = null;
            }
        } catch (IOException e) {
            try {
                clientConnection.close();
                connectionsLock.lock();
                clientsConnected.remove(clientConnection.getClientInfo());
                connectionsLock.unlock();
                System.out.println("Cliente desconetado");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    private static void tasksExecutor() {
        new Thread(() -> {
            while (true) {
                queueLock.lock();
                try {

                    while (taskQueue.isEmpty()) {
                        try {
                            queueChange.await();
                            System.out.println("A SAIR DO taskQueue isEmpty()");
                        } catch (InterruptedException e) {
                            System.err.println("Thread interrupted: " + e.getMessage());
                        }
                    }

                    Task task = taskQueue.poll();
                    queueLock.unlock();
                    connectionsLock.lock();
                    if (task != null) {
                        boolean executed = false;
                        while (!executed) {
                            for(Map.Entry<Integer, RemoteServer> server : serversConnected.entrySet()){
                                RemoteServer s = server.getValue();
                                if(s.availiableMemory > task.getMemory()) {
                                    task.executorServer = s.serverId;
                                    s.connection.sendData(1000, task.taskID, task.data);
                                    s.removeMemory(task.getMemory());
                                    connectionsLock.unlock();

                                    executed = true;
                                    break;
                                }
                            }
                            if (executed = false ) {
                                if(!taskQueue.isEmpty()){
                                    Task prox = taskQueue.peek();
                                    for(Map.Entry<Integer, RemoteServer> server : serversConnected.entrySet()){
                                        RemoteServer s = server.getValue();
                                        if(s.availiableMemory > prox.getMemory()) {
                                            taskQueue.poll();
                                            prox.executorServer = s.serverId;
                                            s.connection.sendData(1000, prox.taskID, prox.data);
                                            s.removeMemory(task.getMemory());
                                            connectionsLock.unlock();

                                            executed = true;
                                            break;
                                        }
                                    }
                                    taskQueue.add(task);
                                }
                                queueLock.unlock();
                            }

                        }
                    } else {
                        connectionsLock.unlock();
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao enviar para worker: " + e.getMessage());
                }
            }
        }, "TaskDistributionThread").start();


    }

}

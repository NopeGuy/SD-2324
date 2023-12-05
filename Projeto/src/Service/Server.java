package Service;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {

    // EM MB
    private double MAX_MEMORY = 1000 * 10e6;

    // IMPLEMENTAÇÃO DISTRIBUIDA
    public Server(int MAX_MEMORY){
        this.MAX_MEMORY = MAX_MEMORY;

    }

    // FUNCIONALIDADES AVANÇADAS
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(12345);
        final Users users;

        File u = new File("users.data");

        if(u.exists())
            users = Users.deserialize("users.data");
        else
            users = new Users();


        TasksExecutor tasksExecutor = new TasksExecutor();



        while(1 == 1){
            System.out.println("Á espera de conexão");
            Socket socket = server.accept();
            Connection con = new Connection(socket);



            Runnable clientThread = () -> {
                try (con){
                    while(true) {
                        String username = "";
                        Frame f  = con.receive();

                        System.out.println("Recebii frame com tag = " + f.tag );
                        // Login Request
                        if( f.tag == 1 ) {
                            String[] user_pass = new String(f.data).split(";;");
                            String recieved_email = user_pass[0];
                            String recieved_pass = user_pass[1];

                            if( !users.validatePassword(recieved_email, recieved_pass)){
                                con.sendString( 2,0,"ERRO: Email e Password incorretos...");
                            } else {
                                con.sendString( 2, 0,"Email e Password corretos");
                            }
                        // Register Request
                        }
                        else if ( f.tag == 2 ){
                            String[] user_pass = new String(f.data).split(";;");
                            String recieved_user = user_pass[0];
                            String recieved_pass = user_pass[1];

                            users.addAccount(recieved_user, recieved_pass);
                            users.serialize("users.data");
                            con.sendData(3, 0, "User criado".getBytes());
                            username = recieved_user;

                        //  Executar JOB
                        }
                        else if( f.tag == 30 ) {
                            tasksExecutor.addTask(f.data, con);
                        // Get status do Servidor
                        }
                        else if (f.tag == 40) {
                            con.sendData(41, 0, tasksExecutor.getStatus());
                        // Alguma tag não existente
                        } else {
                            con.sendString(-1, 0, "Ainda não foi implementado...");
                        }

                    }
                } catch( IOException error ){
                    System.out.println("Error com o cliente " + socket.getLocalAddress() + " :" + error.getMessage());
                }
            };

            new Thread(clientThread).start();

        }


    }

}
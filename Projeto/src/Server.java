import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(12345);
        final Users users;

        File u = new File("users.data");

        if(u.exists())
            users = Users.deserialize("users.data");
        else
            users = new Users();

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
                                con.sendString( -1, "Email e Password incorretos...");
                            } else {
                                con.sendString( 1, "Email e Password corretos");
                            }
                        // Register Request
                        } else if ( f.tag == 2 ){
                            String[] user_pass = new String(f.data).split(";;");
                            String recieved_user = user_pass[0];
                            String recieved_pass = user_pass[1];

                            users.addAccount(recieved_user, recieved_pass);
                            users.serialize("users.data");
                            con.sendData(1, "User criado".getBytes());
                            username = recieved_user;

                        // Alguma tag não existente
                        } else if( f.tag == 30 ) {
                            sd23.JobFunction.execute(f.data);
                        }else {
                            con.sendString(-1, "Ainda não foi implementado...");
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
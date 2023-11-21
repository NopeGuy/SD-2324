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
            Socket socket = server.accept();


        }


    }

}
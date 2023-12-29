package Service;

import sd23.JobFunctionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class StandaloneServer {

    // EM MB
    private double MAX_MEMORY = 1000 * 10e6;

    // IMPLEMENTAÇÃO DISTRIBUIDA
    public StandaloneServer(int MAX_MEMORY){
        this.MAX_MEMORY = MAX_MEMORY;

    }

    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 10080);

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        Connection c = new Connection(s);

        while(1 == 1){
            Connection con = new Connection(s);
            con.sendString(90, -1, "{memory: 5000}");


            try (con){

                while(true) {
                    Frame f  = con.receive();

                    System.out.println("Recebii frame com tag = " + f.tag );
                    if( f.tag == 1000 ) {

                        new Thread(() -> {
                            try {
                                byte[] resp = sd23.JobFunction.execute(f.data);
                                // Resposta
                                con.sendData(1001, f.taskid, resp);
                            } catch (JobFunctionException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    // Get status do Servidor
                    }
                    else if (f.tag == 91) {
                        System.out.println("Este servidor tem ID = " + f.taskid);
                    // Alguma tag não existente
                    } else {
                        con.sendString(-1, 0, "Ainda não foi implementado...");
                    }

                }
            } catch( IOException error ){
                System.out.println("Error com o cliente " + s.getLocalAddress() + " :" + error.getMessage());
            }



        }


    }

}
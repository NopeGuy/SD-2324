import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Dispacher {

    public void startServer() throws IOException {
        System.out.println("HelloWorld");
        ServerSocket ss = new ServerSocket(Main.Port);
        Scanner s = new Scanner(System.in);
        int opt = -1;

        while (opt != 0) {
            System.out.println("Opções do Servidor: \n1. Fechar servidor\n0. Sair\nOpção: ");
            Socket socket = ss.accept();
            Thread worker = new Thread(new Server(socket));
            worker.start();


            opt = s.nextInt();

            if(opt == 1){
                socket.close();
                System.out.println("Servidor terminado");
            }
        }
    }
}

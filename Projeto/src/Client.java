import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 12345);
        String username = null;
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        while(username == null){
            System.out.println("Bem vindo. \n1. Login\n2. Registar");
            String opt = stdin.readLine();
            if(opt.equals("1")) {
                System.out.print("***INICIAR SESSÃO***\n"
                        + "\n"
                        + "Introduza o seu endereço de email: ");
                String email = stdin.readLine();
                System.out.print("Introduza a sua palavra-passe: ");
                String password = stdin.readLine();
                Connection c = new Connection(s);
                c.sendString(1, email + ";;" + password);
                Thread.sleep(1000);
                Frame f = c.receive();
                if (f.tag != 1){
                    System.out.print(new String(f.data));
                } else {
                    System.out.println("Login com sucesso..");
                    username = email;
                }
            }
        }

        System.out.println("A terminar.");

    }
}

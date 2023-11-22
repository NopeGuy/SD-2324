import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 12345);
        String username = null;
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        Connection c = new Connection(s);

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
                c.sendString(1, email + ";;" + password);
                Frame f = c.receive();
                if (f.tag != 1){
                    System.out.print(new String(f.data));
                } else {
                    System.out.println("Login com sucesso..");
                    username = email;
                }
            }
            if(opt.equals("2")){
                System.out.println("Introduza o seu username: ");
                String user = stdin.readLine();
                System.out.print("Introduza a sua palavra-passe: ");
                String password = stdin.readLine();
                c.sendString(2, user + ";;" + password);
                Frame f = c.receive();
                if (f.tag != 1){
                    System.out.print(new String(f.data));
                } else {
                    System.out.println("Login com sucesso..");
                    username = user;
                }

            }
        }

        System.out.println("A terminar.");

    }
}

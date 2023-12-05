package Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

public class CloudServer {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket server = new ServerSocket(10080);
        final Servers servers;

        File u = new File("servers.data");

        if (u.exists())
            servers = Servers.deserialize("servers.data");
        else
            servers = new Servers();

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));


        while (1 == 1) {
            System.out.println("CLOUD SERVER MANAGEMENT\n" +
                    "1. Adicionar Servidor\n" +
                    "2. Remover Servidor\n" +
                    "3. Listar Servidores\n" +
                    "4. Iniciar Serviço\n" +
                    "9. Sair\n" +
                    "Opção: ");

            String opt = stdin.readLine();

            if (opt.equals("1")) {
                System.out.println("Hostname: ");
                String host = stdin.readLine();
                System.out.println("Porta: ");
                int porta = Integer.parseInt(stdin.readLine());
                System.out.println("Memória Máxima: ");
                int max_memory = Integer.parseInt(stdin.readLine());
                servers.addServer(host, porta, max_memory);
                servers.serialize("servers.data");
                System.out.println("Servidor adicionado com sucesso.");
            } else if (opt.equals("2")) {
                System.out.println("Servidores disponiveis: \n");
                System.out.println(servers.toString());
                System.out.println("Opção a remover: ");
                int serverId = Integer.parseInt(stdin.readLine());
                servers.removeServer(serverId);
                servers.serialize("servers.data");
            } else if (opt.equals("3")) {
                System.out.println("Servidores disponiveis: \n");
                System.out.println(servers.toString());
                System.out.println("Enter para recuar");
                stdin.readLine();
            } else if (opt.equals("4")) {

            } else if (opt.equals("9")) {

            } else {
                System.out.println("Comando incorreto.");
            }
        }
    }
}

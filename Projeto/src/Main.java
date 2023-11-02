import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static int Port = 1234;

    public static void main(String[] args) throws IOException {

        new Dispacher().startServer();

    }
}
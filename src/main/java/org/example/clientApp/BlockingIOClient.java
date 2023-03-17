package org.example.clientApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


//сделать чтобы клиент мог читать... видеть как в PuTTy - нужно подумать...
//скорее всего надо получить канал и вывести его когда selector реагирует на accept, а не вот это вот всё. Тут blocking IO.
    public class BlockingIOClient {
        public static void main(String[] args) throws InterruptedException {
            Scanner scanner = new Scanner(System.in);
            try (Socket clientSocket = new Socket("localhost", 8080);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                while (true) {
                    String msg = scanner.next();
                    out.println(msg);

                String serverResponse2 = in.readLine();
                System.out.println(serverResponse2);
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



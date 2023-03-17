package org.example;


import org.example.server.Server;

import java.io.IOException;

public class ServerApp {
    public static void main( String[] args ) throws IOException {
        new Server("localhost", 8080).start();
    }
}

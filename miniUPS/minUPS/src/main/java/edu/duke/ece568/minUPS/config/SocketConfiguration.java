package edu.duke.ece568.minUPS.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Configuration
public class SocketConfiguration {
    private final String HOST = "vcm-30634.vm.duke.edu";
    private final int WORLD_PORT = 12345;

    @Bean
    public Socket worldSocket() throws IOException {
        return new Socket(HOST, WORLD_PORT);
    }

    @Bean
    public ServerSocket serverSocket() {
        try {
            return new ServerSocket(22222);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create server socket bean", e);
        }
    }


}

package com.remotecontrol.star;

import com.remotecontrol.star.socket.HandleListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StarApplication {
    public static void main(String[] args) {
//        System.out.println(args);
        SpringApplication.run(StarApplication.class, args);
        Thread server = new Thread(new HandleListener());
        server.start();
    }
}

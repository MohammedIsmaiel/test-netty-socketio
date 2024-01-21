package com.tobeupdated.socketio;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class SocketioApplication {
	public static void main(String[] args) {
		SpringApplication.run(SocketioApplication.class, args);
	}

	@Bean
	public CommandLineRunner setup(SocketIOServer socket) {
		return args -> {
			socket.start();
			socket.addConnectListener(new ConnectListener() {
				@Override
				public void onConnect(SocketIOClient client) {
					System.out.println(client.getSessionId() + "--------- connected");
					socket.addEventListener(client.getSessionId().toString(), String.class,
							new DataListener<String>() {
								@Override
								public void onData(SocketIOClient client1, String data, AckRequest ackRequest) {
									System.out.println(data + "is a message from:- " + client1.getSessionId());
								}
							});
				}
			});
		};
	}

	@Bean
	public SocketIOServer socketIOServer() {
		Configuration config = new Configuration();
		config.setHostname("localhost");
		config.setPort(8081);
		config.setOrigin("http://localhost:8080");
		config.setAllowHeaders("Access-Control-Allow-Origin");
		return new SocketIOServer(config);
	}

	@Controller
	public class TestController {
		@GetMapping("/")
		public String showIndexString() {
			return "index";
		}
	}

	@Component
	public class SocketEventHandler {
		@OnConnect
		public void onConnect(SocketIOClient client) {
			System.out.println("Client connected: " + client.getSessionId().toString());
		}

		@OnDisconnect
		public void onDisconnect(SocketIOClient client) {
			System.out.println("Client disconnected: " + client.getSessionId().toString());
		}

		@OnEvent(value = "message")
		public void onMessage(SocketIOClient client, String message) {
			System.out.println("Received message from client " + client.getSessionId() + ": " + message);
		}
	}
}

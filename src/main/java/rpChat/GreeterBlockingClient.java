package rpChat;

import java.util.Optional;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import rpChat.GreeterGrpc.GreeterBlockingStub;

public class GreeterBlockingClient {
	public static void main(String[] args) throws Exception {
		ManagedChannel channel = ManagedChannelBuilder
				.forAddress("localhost", 1234)
				.usePlaintext(true)
				.build();

		GreeterBlockingStub remoteGreeter = GreeterGrpc.newBlockingStub(channel);
		GreeterBlockingClient client = new GreeterBlockingClient(remoteGreeter);

		Optional<String> userName;
		while ((userName = Utils.readLine()).isPresent()) {
			client.printWelcomeMessage(userName.get());
		}
	}

	private final GreeterBlockingStub greeter;
	
	public GreeterBlockingClient(GreeterBlockingStub greeter) {
		this.greeter = greeter;
	}

	public void printWelcomeMessage(String username) {
		GreeterReq req = GreeterReq.newBuilder()
				.setUserName(username)
				.build();

		GreeterResp response = greeter.welcomeMessage(req);
		System.out.println(response + ".");
	}
}

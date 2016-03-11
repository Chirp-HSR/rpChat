package rpChat;

import java.util.Optional;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import rpChat.GreeterGrpc.Greeter;

public class GreeterClient {
	public static void main(String[] args) throws Exception {
		ManagedChannel channel = ManagedChannelBuilder
				.forAddress("localhost", 1234)
				.usePlaintext(true)
				.build();

		Greeter remoteGreeter = GreeterGrpc.newStub(channel);
		GreeterClient client = new GreeterClient(remoteGreeter);

		Optional<String> userName;
		while ((userName = Utils.readLine()).isPresent()) {
			client.printWelcomeMessage(userName.get());
		}
	}

	private final Greeter greeter;
	
	public GreeterClient(Greeter greeter) {
		this.greeter = greeter;
	}

	public void printWelcomeMessage(String username) {
		GreeterReq req = GreeterReq.newBuilder()
				.setUserName(username)
				.build();

		greeter.welcomeMessage(req, new StreamObserver<GreeterResp>() {
			@Override
			public void onNext(GreeterResp resp) {
				System.out.print(resp);
			}

			@Override
			public void onError(Throwable t) {
				t.printStackTrace();
			}

			@Override
			public void onCompleted() {
				System.out.print(".");
			}
		});
	}
}

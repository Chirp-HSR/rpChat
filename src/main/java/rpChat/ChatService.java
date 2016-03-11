package rpChat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import rpChat.ChatGrpc.Chat;
import rpChat.GreeterGrpc.Greeter;

public class ChatService implements Chat {

	private final Map<Integer, String> userNamePerSessionId = new ConcurrentHashMap<>();
	private final Greeter greeter;
	private final AtomicInteger sessionCounter = new AtomicInteger();

	public ChatService(Greeter greeter) {
		this.greeter = greeter;
	}

	private int createSession(String userName) {
		int sessionId = sessionCounter.getAndIncrement();

		userNamePerSessionId.put(sessionId, userName);

		return sessionId;
	}

	@Override
	public void signUp(SignUpReq request, StreamObserver<SignUpResp> responseObserver) {
		System.out.println("> received: " + request);
		// TODO
	}

	@Override
	public StreamObserver<ChatReq> join(StreamObserver<ChatResp> responseObserver) {
		// TODO
		return new StreamObserver<ChatReq>() {
			@Override
			public void onCompleted() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onNext(ChatReq value) {
				// TODO Auto-generated method stub
			}};
	}

	public static void main(String[] args) throws Exception {
		ManagedChannel channel = ManagedChannelBuilder
				.forAddress("localhost", 1234)
				.usePlaintext(true)
				.build();

		Greeter remoteGreeter = GreeterGrpc.newStub(channel);

		Server server = ServerBuilder
				.forPort(1235)
				.addService(ChatGrpc.bindService(new ChatService(remoteGreeter)))
				.build()
				.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				server.shutdown();
			}
		});

		server.awaitTermination();
	}

}

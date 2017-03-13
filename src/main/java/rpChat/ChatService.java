package rpChat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
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
		String username = request.getUserName();

		GreeterReq greetReq = GreeterReq.newBuilder()
				.setUserName(username)
				.build();

		greeter.welcomeMessage(greetReq, new StreamObserver<GreeterResp>() {
			@Override
			public void onNext(GreeterResp greetResp) {
				int sessionId = createSession(username);

				SignUpResp signUpResp = SignUpResp.newBuilder()
						.setWelcomeMsg(greetResp.getGreetings())
						.setSessionId(sessionId)
						.build();

				responseObserver.onNext(signUpResp);
				responseObserver.onCompleted();
			}

			@Override
			public void onError(Throwable t) {
				Status status = ((StatusRuntimeException) t).getStatus();
				if (hasSameStatusCode(status, Status.UNKNOWN)) {
					responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT));
				} else if (hasSameStatusCode(status, Status.UNAVAILABLE)) {
					System.out.println("> Greeting Service not available!");
					responseObserver.onError(new StatusRuntimeException(Status.INTERNAL));
				} else {
					responseObserver.onError(t);
				}
			}

			@Override
			public void onCompleted() {}
		});
	}
	
	private static boolean hasSameStatusCode(Status status1, Status status2) {
		if (status1 == null || status2 == null) {
			return false;
		}
		Code statusCode1 = status1.getCode();
		Code statusCode2 = status2.getCode();
		return statusCode1 == statusCode2;
	}

	private final ChatRouter router = new ChatRouter();

	class ChatRouter {

		List<StreamObserver<ChatResp>> respObservers = new CopyOnWriteArrayList<>();

		public void registerResponseObserver(StreamObserver<ChatResp> responseObserver) {
			respObservers.add(responseObserver);
		}

		public void deregister(StreamObserver<ChatResp> responseObserver) {
			respObservers.remove(responseObserver);
		}

		public void publish(ChatReq req) {
			respObservers.forEach(respObs -> {
				String sender = userNamePerSessionId.get(req.getSessionId());
				respObs.onNext(ChatResp.newBuilder()
						.setSender(sender)
						.setContent(req.getContent())
						.build());
			});
		}

	}

	@Override
	public StreamObserver<ChatReq> join(StreamObserver<ChatResp> responseObserver) {
		router.registerResponseObserver(responseObserver);
		return new StreamObserver<ChatReq>() {
			@Override
			public void onNext(ChatReq req) {
				router.publish(req);
			}

			@Override
			public void onCompleted() {
				router.deregister(responseObserver);
			}

			@Override
			public void onError(Throwable t) {
				router.deregister(responseObserver);
			}
		};
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

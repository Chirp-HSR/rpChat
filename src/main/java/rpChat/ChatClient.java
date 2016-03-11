package rpChat;

import java.util.Optional;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import rpChat.ChatGrpc.Chat;

public class ChatClient {

	static boolean keepRunning = true;

	public static void main(String[] args) throws Exception {
		ManagedChannel channel = ManagedChannelBuilder
				.forAddress("localhost", 1235)
				.usePlaintext(true)
				.build();

		final Chat chat = ChatGrpc.newStub(channel);

		String username = Utils.readLine().get().trim();

		chat.signUp(SignUpReq.newBuilder().setUserName(username).build(), new StreamObserver<SignUpResp>() {
			@Override
			public void onNext(SignUpResp resp) {
				System.out.println(resp.getWelcomeMsg());

				joinChatSession(chat, resp.getSessionId());

				keepRunning = false;
			}

			@Override
			public void onError(Throwable t) {
				System.out.println(t);
			}

			@Override
			public void onCompleted() {}
		});

		while (keepRunning) {
			Thread.sleep(1000);
		}
	}

	static void joinChatSession(Chat chat, int sessionId) {
		StreamObserver<ChatReq> chatReqObserver = chat.join(new StreamObserver<ChatResp>() {
			@Override
			public void onNext(ChatResp value) {
				System.out.println(value);
			}

			@Override
			public void onError(Throwable t) {}

			@Override
			public void onCompleted() {}
		});

		Optional<String> msg;
		while ((msg = Utils.readLine()).isPresent()) {
			ChatReq chatReq = ChatReq.newBuilder()
					.setSessionId(sessionId)
					.setContent(msg.get())
					.build();
			chatReqObserver.onNext(chatReq);
		}
		chatReqObserver.onCompleted();
	}
}

package rpChat;

import java.io.IOError;
import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import rpChat.GreeterGrpc.Greeter;
import rpChat.GreeterReq.Language;

public class GreeterService implements Greeter {

	@Override
	public void welcomeMessage(GreeterReq request, StreamObserver<GreeterResp> responseObserver) {
		System.out.println("> received: " + request);
		
		String name = request.getUserName();
		if (name.toUpperCase().contains("RMI")) {
			responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT));
		} else if(name.length() > 10){
			throw new IOError(new IOException("Too much data!"));
		} else {
			String salute = request.getLanguage() == Language.ENGLISH ? "Hi " : "Hallo "; 
			
			GreeterResp resp = GreeterResp.newBuilder()
					.setGreetings(salute + name + "!")
					.build();
			responseObserver.onNext(resp);
			responseObserver.onCompleted();
		}
	}

	public static void main(String[] args) throws Exception {
		GreeterService serviceImpl = new GreeterService();
		
		Server server = ServerBuilder
				.forPort(1234)
				.addService(GreeterGrpc.bindService(serviceImpl))
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

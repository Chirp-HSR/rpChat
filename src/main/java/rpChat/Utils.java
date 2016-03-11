package rpChat;

import java.io.IOException;
import java.util.Optional;

public class Utils {
	public static Optional<String> readLine() {
		try {
			byte[] inBuffer = new byte[1024];
			int length = System.in.read(inBuffer);
			if (length > -1) {
				return Optional.of(new String(inBuffer, 0, length));
			} else {
				return Optional.empty();
			}
		} catch (IOException e) {
			return Optional.empty();
		}
	}
}

import java.util.ArrayList;
import java.util.List;

import com.Message;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import db.beans.User;

public class GsonTest {
	public static void main(String[] args) {
		Gson gson = new Gson();
		List<User> payload = new ArrayList<>();
		payload.add(new User());
		payload.add(new User());

		String s1 = gson.toJson(new Message.messageBuilder<>().Code(Message.M_LOGIN).Sender("sender")
				.Receiver("receiver").Payload(new User()).build());
		System.out.println(s1);
		Message message = null;
		try {
			message = gson.fromJson(s1, new TypeToken<Message<List<User>>>() {
			}.getType());
		} catch (Exception e) {
			message = gson.fromJson(s1, new TypeToken<Message<User>>() {
			}.getType());
		}
		System.out.println(message);
		System.out.println(message.getPayload());
		List<User> temp = (List<User>) message.getPayload();
		for (User user : temp) {
			System.out.println(user.getClass());
		}
	}
}

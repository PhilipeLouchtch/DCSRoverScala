package chatapp


import io.circe.{Encoder, Json, _}

class ChatUser(val username: String) {
}

object ChatUser {
	val Steffan = new ChatUser("steffan")
	val Giannis = new ChatUser("giannis")

	implicit val encodeUser: Encoder[ChatUser] = new Encoder[ChatUser] {
		final def apply(u: ChatUser): Json = Json.obj(
			("username", Json.fromString(u.username))
		)
	}

	implicit val decodeUser: Decoder[ChatUser] = new Decoder[ChatUser] {
		final def apply(c: HCursor): Decoder.Result[ChatUser] =
			for {
				username <- c.downField("username").as[String]
			} yield {
				new ChatUser(username)
			}
	}

}
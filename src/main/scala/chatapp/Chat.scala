package chatapp

import rover.rdo.AtomicObjectState
import rover.rdo.client.RdObject
import cats.implicits._
import com.monovore.decline._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async, await}
import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._

class User(val username: String) {

}

object User {
	val Steffan = new User("steffan")
	val Giannis = new User("giannis")
}

class ChatMessage(val body: String,
                  val author: User,
                  val timestamp: Long = java.time.Instant.now.getEpochSecond()) {

	override def toString: String = {
		s"${author.username}: $body"
	}
}

// FIXME: ensure messages can be read, but not modified or reassigned...
// FIXME: after state & rd object impl change
class Chat(_onStateModified: Chat#Updater) extends RdObject[List[ChatMessage]](AtomicObjectState.initial(List[ChatMessage]())) {
	type Updater = AtomicObjectState[List[ChatMessage]] => Promise[Unit]

	def send(message: ChatMessage): Promise[Unit]= {
		val op: AtomicObjectState[List[ChatMessage]]#Op = s => s :+ message

		Promise() completeWith async {
			modifyState(op)
		}
	}

	override def onStateModified(oldState: AtomicObjectState[List[ChatMessage]]): Promise[Unit] = {
		_onStateModified(state)
	}

	def currentVersion(): Long = {
		immutableState.size
	}

}
object Chat {
	val SIZE = 10

	def client(serverAddress: String, user: User): Unit = {
		val printer = (string: String) => {
			val cls = s"${string.split("\n").map(c => s"${REPL.UP}${REPL.ERASE_LINE_BEFORE}${REPL.ERASE_LINE_AFTER}").mkString("")}"
			// Prepend two spaces to match input indentation of "> "
			val text = string.split("\n").map(line => s"  $line").mkString("\n")

			s"${REPL.SAVE_CURSOR}$cls\r$text${REPL.RESTORE_CURSOR}"
		}

		// TODO: This is hacky, figure out a better way to do this
		val updater: Chat#Updater = state => Promise() completeWith async {
			val text = state.immutableState.takeRight(SIZE).map(m => m.toString()).mkString("\n")
			print(s"${printer(text)}")
		}

		val chat = new Chat(updater)
		println("  Welcome to Rover Chat!")
		print((1 to SIZE).map(i => "\n").mkString(""))

		// Simulate conversation
		async {
			Thread.sleep(3000)
			await(chat.send(new ChatMessage("Hey man!", User.Giannis)).future)
			updater(chat.state)

			Thread.sleep(3000)
			await(chat.send(new ChatMessage("How's it going?", User.Giannis)).future)
			updater(chat.state)

			Thread.sleep(10000)
			await(chat.send(new ChatMessage("Yea man I'm good", User.Giannis)).future)
			updater(chat.state)
		}

		val reader = () => {
			print("> ")
			val s = scala.io.StdIn.readLine()
			s
		}
		val executor = (input: String) => {
			async {
				val p = chat.send(new ChatMessage(input, user))
				await(p.future)
					// This clears the input line
					print(s"${REPL.UP}${REPL.ERASE_LINE_AFTER}")
				 	chat.immutableState.takeRight(SIZE).map(m => s"${m.toString()}").mkString("\n")
				}
		}
		val repl: REPL[String] = new REPL(reader, executor, printer)
		Await.result(repl.loop(), Duration.Inf)
	}

	def main(args: Array[String]): Unit = {
		client("bla", User.Steffan)
	}
}

// TODO: Add client and server subcommands
object ChatCLI extends CommandApp(
  name = "rover-chat",
  header = "Says hello!",
  main = {
    val userOpt =
      Opts.option[String]("target", help = "Person to greet.") .withDefault("world")

    val quietOpt = Opts.flag("quiet", help = "Whether to be quiet.").orFalse

    (userOpt, quietOpt).mapN { (user, quiet) =>
      if (quiet) println("...")
      else println(s"Hello $user!")
    }
  }
)

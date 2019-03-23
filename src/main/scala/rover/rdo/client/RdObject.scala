package rover.rdo.client

import rover.rdo.state.{AtomicObjectState, RecordedStateModification}

import scala.async.Async.async
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//FIXME: use hashes instead of Longs/Strings?
class RdObject[A](var state: AtomicObjectState[A]) {

	// TODO: "is up to date" or "version" methods

	protected final def modifyState(op: AtomicObjectState[A]#Op): Unit = {
		state = state.applyOp(op)
//		onStateModified(state)
	}

	protected def onStateModified(oldState: AtomicObjectState[A]): Future[Unit] = {
		async {

		}
	}

	protected final def immutableState: A = {
		return state.immutableState
	}

	override def toString: String = {
		state.toString
	}
}

class DiffWithAncestor[A](private val child: AtomicObjectState[A], private val ancestor: AtomicObjectState[A]) {

	def asList: List[RecordedStateModification[A]] = {
		for(i <- child.log.asList) {
			if(i.parent.contains(ancestor)) {
				// TODO: inefficient
				val indexOfI = child.log.asList.indexOf(i)
				val logRecordsUpToI = child.log.asList.slice(indexOfI, child.log.asList.size)
				return logRecordsUpToI

			}
		}
		
		throw new RuntimeException("Failed to determine difference with this ancestor")
	}

	override def toString: String = {
		"DiffWithAncestor{ \n	" + asList.mkString("\n	") + "\n}"
	}
}
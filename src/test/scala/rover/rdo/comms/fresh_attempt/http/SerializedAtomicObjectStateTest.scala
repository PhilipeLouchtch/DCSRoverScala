package rover.rdo.comms.fresh_attempt.http

import org.scalatest.FunSuite
import rover.rdo.state.AtomicObjectState

class SerializedAtomicObjectStateTest extends FunSuite {

	test("Serialize, deserialize works") {
		type Henk = List[String]
		var state: AtomicObjectState[Henk] = AtomicObjectState.initial(List())

		val serialized = new SerializedAtomicObjectState[Henk](state)
		val deserialized = new AtomicObjectStateAsByteArray[Henk](serialized.asBytes)

		val resState = deserialized.asAtomicObjectState

		val areSame = state.equals(resState)
		assert(areSame)

		val modifiedOrig = state.applyOp(a => a :+ "henk")
		val noLongerSame = modifiedOrig.equals(resState)
		assert(noLongerSame)
	}
}

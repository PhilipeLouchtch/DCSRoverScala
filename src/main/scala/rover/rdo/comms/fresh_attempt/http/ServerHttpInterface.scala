package rover.rdo.comms.fresh_attempt.http

import rover.rdo.ObjectId
import rover.rdo.comms.fresh_attempt.Server
import spark.Spark._

/**
  * Provides a default HTTP "restful" endpoints for `Server[A]` implementations
  *
  * @note Not statistically checked during compile-time if implementation is complete.
  *       Could use Spring's RestControllers or other method-based frameworks instead
  *       of Spark to be able to achieve this.
  * @param applicationName The name of the object/state. Is used as part of the http path
  * @param serverImpl The underlying server implementation
  * @tparam A
  */
class ServerHttpInterface[A <: Serializable](
	private val applicationName: String,
	private val serverImpl: Server[A]
) {
	val endpointPaths = ServerHttpEndpointPaths.forApp(applicationName)

	get(endpointPaths.createEndpoint, (request, result) => {
		val newlyCreated = serverImpl.create()

		val serialized = new SerializedAtomicObjectState[A](newlyCreated)

		result.body(serialized.asString)
		result.`type`("application/octet-stream")

		result
	})

	// Create an endpoint for the "get" server method
	get(endpointPaths.getEndpoint, (request, result) => {
		val objectIdStringInRequestParam = request.params(":objectId")
		val objectId = ObjectId.from(objectIdStringInRequestParam)

		val latestOnServer = serverImpl.get(objectId)
			.getOrElse(throw new Exception("Server did not have requested object/state..."))

		val serializedState = new SerializedAtomicObjectState[A](latestOnServer)

		result.body(serializedState.asString)
		result.`type`("application/octet-stream")

		result
	})

	post(endpointPaths.acceptEndpoint, (request, result) => {
		val bytes = request.bodyAsBytes()
		val deserializedAtomicObjectState = new AtomicObjectStateAsByteArray[A](bytes)

		val incomingState = deserializedAtomicObjectState.asAtomicObjectState
		serverImpl.accept(incomingState)

		result.status(200)
		result
	})

	get(endpointPaths.statusEndpoint, (request, result) => {
		// TODO
		result.status(200)

		result
	})
}

final case class ServerHttpEndpointPaths private (private val applicationName: String) {
	def createEndpoint = s"$applicationName/create"
	def getEndpoint = s"$applicationName/get/:objectId"
	def acceptEndpoint = s"$applicationName/accept"
	def statusEndpoint = s"$applicationName/status"
}

object ServerHttpEndpointPaths {
	def forApp(appName: String): ServerHttpEndpointPaths = {
		return new ServerHttpEndpointPaths(appName)
	}
}

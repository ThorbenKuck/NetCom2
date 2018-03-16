package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Session;

/**
 * This interface describes, that the inherited Class is capable of creating new Connections, based upon the Session.
 * <p>
 * To successfully use this method, the sessions have to be kept and controlled by the inherited Class. The {@link com.github.thorbenkuck.netcom2.network.server.ServerStart}
 * is such a Class.
 * <p>
 * It is designed to be called of a registered Communication registration, for example like this:
 * <p>
 * <code>
 * class NewConnectionRequest {
 * private Class key;
 * public NewConnectionRequest(Class key) {
 * this.key = key;
 * }
 * public Class getKey() {
 * return this.key;
 * }
 * }
 * ServerStart serverStart = ...
 * serverStart.getCommunicationRegistration()
 * .register(NewConnectionRequest.class)
 * .addFirst((session, newConnectionRequest) - serverStart.createNewConnection(session, newConnectionRequest.getKey()));
 * </code>
 * <p>
 * So, if the ServerStart receives a NewConnectionRequest, it establishes the new Connection over the ServerStart.
 * <p>
 * If you use the ServerStart for establishing multiple Connections, you should abstract and decouple your code. Do not
 * use the ServerStart directly, but pass the MultipleConnections interface instead.
 */
public interface MultipleConnections {

	/**
	 * Instantiates the creation of the new Connection.
	 * <p>
	 * This call should be Asynchronous, so that the caller may do different Things after calling this method.
	 * <p>
	 * For that, an instance of the {@link Awaiting} should be instantiated and returned.
	 * After the Connection is established <b>AND</b> usable, this Awaiting should be continued.
	 *
	 * @param session the Session, for which the new Connection should be used
	 * @param key     the key, which identifies the Connection
	 * @return an instance of the {@link Awaiting} interface for synchronization
	 */
	Awaiting createNewConnection(final Session session, final Class key);

}

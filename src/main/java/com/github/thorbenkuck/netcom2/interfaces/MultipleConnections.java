package com.github.thorbenkuck.netcom2.interfaces;

import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Session;

/**
 * This interface describes, that the inherited Class is capable of creating new Connections, based upon the Session.
 * <p>
 * To successfully use this method, the sessions have to be kept and controlled by the inherited Class. The {@link com.github.thorbenkuck.netcom2.network.server.ServerStart}
 * is such a Class.
 * <p>
 * You may access a new Connection by stating the following:
 * <p>
 * <pre>{@code
 * ServerStart serverStart = ...
 * Class connectionKey = ...
 * Session session = ...
 * serverStart.createNewConnection(session, connectionKey);
 * }</pre>
 * <p>
 * This however is a bad approach design wise. In most situations, it is recommended to access the new Connection through
 * the use of the {@link com.github.thorbenkuck.netcom2.network.shared.clients.Client} class.
 * <p>
 * A better way to approach this, would be to encapsulate the Session within an custom <code>User</code> object and to
 * call this at certain times within your code.
 * <p>
 * <p>
 * So, if the ServerStart receives a NewConnectionRequest, it establishes the new Connection over the ServerStart.
 * <p>
 * If you use the ServerStart for establishing multiple Connections, you should abstract and decouple your code. Do not
 * use the ServerStart directly, but pass the MultipleConnections interface instead.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.client.ClientStart#createNewConnection(Class)
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.Client#createNewConnection(Class)
 * @since 1.0
 */
public interface MultipleConnections {

	/**
	 * Instantiates the creation of the new Connection.
	 * <p>
	 * This call should be Asynchronous, so that the caller may do different things after calling this method.
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

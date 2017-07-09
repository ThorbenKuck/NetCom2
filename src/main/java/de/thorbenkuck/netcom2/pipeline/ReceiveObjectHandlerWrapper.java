package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.exceptions.HandlerInvocationException;
import de.thorbenkuck.netcom2.exceptions.NoCorrectHandlerFoundException;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

class ReceiveObjectHandlerWrapper {

	private final Logging logging = Logging.unified();
	private ReflectionBasedObjectAnalyzer reflectionBasedObjectAnalyzer;

	ReceiveObjectHandlerWrapper() {
		reflectionBasedObjectAnalyzer = new ReflectionBasedObjectAnalyzer();
	}

	public <T> OnReceiveTriple<T> wrap(Object o, Class<T> clazz) {
		Optional<Method> methodOptional = getResponsibleForClass(o, clazz);
		if (! methodOptional.isPresent()) {
			throw new NoCorrectHandlerFoundException("Could not resolve an Object to Handle " + clazz + " in " + o + " or:\n" +
					"Found more than one Object to handle!");
		}

		return wrap(clazz, methodOptional.get(), o);
	}

	private <T> Optional<Method> getResponsibleForClass(Object o, Class<T> clazz) {
		return reflectionBasedObjectAnalyzer.getResponsibleMethod(o, clazz);
	}

	private <T> OnReceiveTriple<T> wrap(Class<T> clazz, Method method, Object o) {
		return new InvokeWrapper<>(method, clazz, o);
	}

	class InvokeWrapper<T> implements OnReceiveTriple<T> {

		private final Method toInvoke;
		private final Class<T> toExpect;
		private final boolean accessible;
		private final Object caller;

		InvokeWrapper(Method toInvoke, Class<T> toExpect, Object caller) {
			this.toInvoke = toInvoke;
			this.toExpect = toExpect;
			accessible = toInvoke.isAccessible();
			this.caller = caller;
		}

		@Override
		public void accept(Connection connection, Session session, T t) {
			logging.debug("Trying to access " + t);
			if (! t.getClass().equals(toExpect) || ! t.getClass().isAssignableFrom(toExpect)) {
				throw new HandlerInvocationException("Could not invoke method: " + toInvoke + " awaiting class " + toExpect);
			}
			logging.trace("applying ..");
			invoke(getParametersInCorrectOder(connection, session, t));

		}

		private void invoke(Object[] args) {
			logging.trace("calling ..");
			synchronized (toInvoke) {
				logging.trace("Updating accessibility ..");
				if (! accessible) {
					logging.trace("Setting method accessible ..");
					toInvoke.setAccessible(true);
				}
				try {
					logging.trace("invoking " + toInvoke + " on " + caller + " with " + Arrays.toString(args) + " ..");
					toInvoke.invoke(caller, args);
					logging.trace("successfully invoked!");
				} catch (IllegalArgumentException e) {
					throw new HandlerInvocationException("Method rejected arguments " + Arrays.toString(args) + "\n" +
							"Method: " + toInvoke + "\n" +
							"Object: " + caller, e);
				} catch (IllegalAccessException e) {
					throw new HandlerInvocationException("Method became inaccessible: " + toInvoke, e);
				} catch (InvocationTargetException e) {
					throw new HandlerInvocationException(e);
				} finally {
					logging.trace("cleanup ..");
					toInvoke.setAccessible(accessible);
				}
			}
		}

		private Object[] getParametersInCorrectOder(Object... objects) {
			final List<Object> arguments = new ArrayList<>();
			logging.debug("Assembling object to call " + toInvoke);
			for (Class<?> clazz : toInvoke.getParameterTypes()) {
				tryMatch(clazz, arguments, objects);
			}
			return arguments.toArray();
		}

		private void tryMatch(Class<?> clazz, List<Object> arguments, Object[] objects) {
			logging.trace("Searching for");
			for (Object o : objects) {
				logging.trace("Matching " + clazz + " with " + o.getClass());
				if (clazz.isAssignableFrom(o.getClass())) {
					logging.trace("Match found! ");
					arguments.add(o);
				}
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (! (o instanceof ReceiveObjectHandlerWrapper)) return false;

		ReceiveObjectHandlerWrapper that = (ReceiveObjectHandlerWrapper) o;

		if (! logging.equals(that.logging)) return false;
		return reflectionBasedObjectAnalyzer.equals(that.reflectionBasedObjectAnalyzer);
	}

	@Override
	public int hashCode() {
		int result = logging.hashCode();
		result = 31 * result + reflectionBasedObjectAnalyzer.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "ReceiveObjectHandlerWrapper{" +
				"reflectionBasedObjectAnalyzer=" + reflectionBasedObjectAnalyzer +
				'}';
	}
}

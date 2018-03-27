/**
 * This package contains some interfaces, that are unique to NetCom2, but not to any sup-package
 * <p>
 * So, for example, some of those interface define a bridge between 2 decoupled parts, some expanded interface of the
 * {@link java.util.function} package, such as {@link com.github.thorbenkuck.netcom2.interfaces.TriConsumer} or {@link com.github.thorbenkuck.netcom2.interfaces.TriPredicate},
 * which all may be used freely outside of NetCom2.
 * <p>
 * This package has the least likeliness to be altered. New additions are certainly possible, but not necessarily certain.
 * So, you might rely on those interface.
 * <p>
 * Any interface annotated with {@literal @}FunctionalInterface is meant to be a functional interface and will receive
 * at best some new default-methods. If the interface you are using does not have this annotation, this interface is a
 * functional interface by pure chance, not by design. Do not rely on those interface being a functional interface!
 * <p>
 * Note however, that using this interfaces outside of your network-layer might be a sign of bad coupling and dependency
 * towards NetCom2.
 */
package com.github.thorbenkuck.netcom2.interfaces;
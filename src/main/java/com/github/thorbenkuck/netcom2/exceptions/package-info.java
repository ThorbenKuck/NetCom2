/**
 * This package contains all Exceptions that may be thrown at different times within NetCom2.
 * <p>
 * You should not attempt to create your own Exception outside of NetCom2. Those Exceptions are meant to signal the using
 * developer, that NetCom2 internally encountered an Exception.
 * <p>
 * Every Exception in this package either inherits from the {@link com.github.thorbenkuck.netcom2.exceptions.NetComException}
 * or the {@link com.github.thorbenkuck.netcom2.exceptions.NetComRuntimeException}. This means, that if you do not care
 * about what exception may be thrown (like if you do not need to react differently), you may catch a NetComException or
 * a NetComRuntimeException.
 * <p>
 * Note however, that while this might be future prove, Exceptions such as the  {@link com.github.thorbenkuck.netcom2.exceptions.StartFailedException}
 * and {@link com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException} are by design completely different.
 * The first means, the Server could not be launched and is not recoverable. The second however means that something did
 * go wrong while a certain Client connected and it is likely to be recoverable.
 *
 * @since 1.0
 */
package com.github.thorbenkuck.netcom2.exceptions;
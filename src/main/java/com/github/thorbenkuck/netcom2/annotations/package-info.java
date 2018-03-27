/**
 * This package contains all non-rmi annotations.
 * <p>
 * Most of those annotations are meant for better readability. For example: The {@link com.github.thorbenkuck.netcom2.annotations.Experimental}
 * annotation does nothing at Runtime, but signals that the annotated part is not tested correctly.
 * <p>
 * Those Exception might (in the future) receive annotation-processors.
 * <p>
 * The rest of annotations within this package, are non-specific api annotations (such as {@link com.github.thorbenkuck.netcom2.annotations.ReceiveHandler}).
 * Those annotations are considered mandatory for NetCom2.
 *
 * @since 1.0
 */
package com.github.thorbenkuck.netcom2.annotations;
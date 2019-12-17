package net.epicorp.util;

import java.util.Optional;

/**
 * this is for dealing with data that may override each other
 */
public interface OverrideHandler<T> {
	/**
	 * force the previous data to be overriden
	 */
	void force();

	/**
	 * if there is no data already, then override it, otherwise do not do anything
	 * @return returns true if there was no data before the call
	 */
	default boolean ifVacant() {
		Optional<T> old = this.old();
		if (old.isPresent())
			return false;
		else {
			this.force();
			return true;
		}
	}

	/**
	 * returns an optional of the data
	 *
	 * @return if Optional#isPresent, there is an object that can be optionally overriden
	 */
	Optional<T> old(); 
}

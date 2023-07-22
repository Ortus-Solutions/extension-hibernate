/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package ortus.extension.orm.runtime.type;

import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;

public class KeyImpl implements Collection.Key {

	private static final long serialVersionUID = -8864844181140115609L; // do not change

	private String key;
	private transient String lcKey;
	private transient String ucKey;

	public KeyImpl() {
		// DO NOT USE, JUST FOR UNSERIALIZE
	}

	public KeyImpl(String key) {
		this.key = key;
		this.ucKey = key.toUpperCase();
	}

	@Override
	public int length() {
		return key.length();
	}

	/**
	 * for dynamic loading of key objects
	 * 
	 * @param string
	 * @return
	 */
	public static Collection.Key init(String key) {
		return new KeyImpl(key);
	}

	public static Collection.Key _const(String key) {
		return new KeyImpl(key);
	}

	public static Collection.Key getInstance(String key) {
		return new KeyImpl(key);
	}

	public static Collection.Key intern(String key) {
		return new KeyImpl(key);
	}

	@Override
	public char charAt(int index) {
		return key.charAt(index);
	}

	@Override
	public char lowerCharAt(int index) {
		return getLowerString().charAt(index);
	}

	@Override
	public char upperCharAt(int index) {
		return ucKey.charAt(index);
	}

	@Override
	public String getLowerString() {
		if (lcKey == null) lcKey = key.toLowerCase();
		return lcKey;
	}

	@Override
	public String getUpperString() {
		return ucKey;
	}

	@Override
	public String toString() {
		return key;
	}

	@Override
	public String getString() {
		return key;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other instanceof KeyImpl) {
			return hash() == ((KeyImpl) other).hash();
		}
		if (other instanceof String) {
			return key.equalsIgnoreCase((String) other);
		}
		if (other instanceof Key) {
			// Both strings are guaranteed to be upper case
			return ucKey.equals(((Key) other).getUpperString());
		}
		return false;
	}

	@Override
	public boolean equalsIgnoreCase(Key other) {
		if (this == other) return true;
		return ucKey.equalsIgnoreCase(other.getString());
	}

	@Override
	public int hashCode() {
		return ucKey.hashCode();
	}

	@Override
	public long hash() {
		return key.hashCode();
	}
}
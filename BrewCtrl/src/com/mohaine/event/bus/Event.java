/*
    Copyright 2009-2011 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

package com.mohaine.event.bus;

public abstract class Event<S extends EventHandler> {

	public static class Type<S> {
		static int nextIndex = 0;
		int index;
		{
			index = nextIndex++;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Type<?>) {
				Type<?> other = (Type<?>) obj;
				return other.index == index;
			}
			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return index;
		}

		@Override
		public String toString() {
			return "Event.Type(" + index + ")";
		}

	}

	public abstract Type<S> getAssociatedType();

	public abstract void dispatch(S event);

}

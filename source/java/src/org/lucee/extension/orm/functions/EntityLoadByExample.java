/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package org.lucee.extension.orm.functions;

import org.lucee.extension.orm.hibernate.util.ORMUtil;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.ORMSession;

public class EntityLoadByExample {
	public static Object call(PageContext pc, Object sampleEntity) throws PageException {
		return call(pc, sampleEntity, false);
	}

	public static Object call(PageContext pc, Object sampleEntity, boolean unique) throws PageException {
		ORMSession session = ORMUtil.getSession(pc);
		if (unique) return session.loadByExample(pc, sampleEntity);
		return session.loadByExampleAsArray(pc, sampleEntity);
	}
}
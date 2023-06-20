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
package ortus.extension.orm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.util.Cast;
import lucee.runtime.util.ResourceUtil;
import lucee.runtime.type.Struct;
import ortus.extension.orm.util.CommonUtil;

/**
 * Perhaps someday Lucee will update the loader interfaces and we can program against interfaces again.
 * Until that day happens, we cannot implement any (Lucee-defined) interface.
 */
// import lucee.runtime.orm.ORMConfigurationParser;

public class ConfigurationParser {

	/**
	 * Parse a struct of ORM settings (from an Application.cfc component) into an ORMConfiguration object.
	 * 
	 * @param config Lucee Config object
	 * @param ac The ApplicationContext object
	 * @param settings Struct of ORM configuration settings.
	 * @param defaultCFCLocation Default location for ORM entity CFCs
	 * @param defaultConfig A default configuration to apply
	 * @return An instantiated ORMConfigurationImpl object
	 */
	public ORMConfigurationImpl parse(Config config, ApplicationContext ac, Struct settings, Resource defaultCFCLocation, ORMConfigurationImpl defaultConfig) {

		if (defaultConfig == null) defaultConfig = new ORMConfigurationImpl();
		ORMConfigurationImpl newConfig = defaultConfig.duplicate();
		newConfig.setConfig( config );
		newConfig.setCfcLocations( defaultCFCLocation == null ? new Resource[0] : new Resource[] { defaultCFCLocation } );

		// autogenmap
		newConfig.setAutogenmap( CommonUtil.toBooleanValue(settings.get(ORMConfigurationImpl.AUTO_GEN_MAP, defaultConfig.autogenmap()), defaultConfig.autogenmap()) );

		// cfclocation
		Object obj = settings.get(ORMConfigurationImpl.CFC_LOCATION, null);

		if (obj != null) {
			java.util.List<Resource> list = new EntityFinder(obj,false).findCFCDirectories(config, ac, obj, true);

			if (list != null && !list.isEmpty()) {
				newConfig.setCfcLocations( list.toArray(new Resource[list.size()]) );
				newConfig.setIsDefaultCfcLocation( false );
			}
		}
		if (newConfig.getCfcLocations() == null) newConfig.setCfcLocations( defaultCFCLocation == null ? new Resource[0] : new Resource[] { defaultCFCLocation } );

		// catalog
		obj = settings.get(ORMConfigurationImpl.CATALOG, null);
		if (!obj.toString().isEmpty()) {
			Coll coll = _load(obj);
			newConfig.setCatalogDefault( coll.def == null ? "" : coll.def );
			newConfig.setCatalogMap( coll.map );
		}
		else {
			newConfig.setCatalogDefault( defaultConfig.getCatalogDefault() == null ? "" : defaultConfig.getCatalogDefault() );
			newConfig.setCatalogMap( defaultConfig.getCatalogMap() );
		}

		// dbcreate
		obj = settings.get(ORMConfigurationImpl.DB_CREATE, null);
		if (!obj.toString().isEmpty()) {
			Coll coll = _load(obj);
			newConfig.setDbCreateDefault( coll.def == null ? "" :coll.def  );
			newConfig.setDbCreateMap( coll.map );
		}
		else {
			newConfig.setDbCreateDefault( defaultConfig.getDbCreateDefault() == null ? "" : defaultConfig.getDbCreateDefault()  );
			newConfig.setDbCreateMap( defaultConfig.getDbCreateMap() );
		}

		// dialect
		obj = settings.get(ORMConfigurationImpl.DIALECT, null);
		if (!obj.toString().isEmpty()) {
			Coll coll = _load(obj);
			newConfig.setDialectDefault( coll.def == null ? "" : coll.def );
			newConfig.setDialectMap( coll.map );
		}
		else {
			newConfig.setDialectDefault( defaultConfig.getDialectDefault() == null ? "" : defaultConfig.getDialectDefault() );
			newConfig.setDialectMap( defaultConfig.getDialectMap() );
		}

		// sqlscript
		obj = settings.get(ORMConfigurationImpl.SQL_SCRIPT, null);
		if (!obj.toString().isEmpty()) {
			Coll coll = _load(obj);
			newConfig.setSqlScriptDefault( coll.def == null ? "" : coll.def );
			newConfig.setSqlScriptMap( coll.map );
		}
		else {
			newConfig.setSqlScriptDefault( defaultConfig.getSqlScriptDefault() == null ? "" : defaultConfig.getSqlScriptDefault() );
			newConfig.setSqlScriptMap( defaultConfig.getSqlScriptMap() );
		}

		// namingstrategy
		newConfig.setNamingStrategy( CommonUtil.toString(settings.get(ORMConfigurationImpl.NAMING_STRATEGY, defaultConfig.namingStrategy()), defaultConfig.namingStrategy()) );

		// eventHandler
		newConfig.setEventHandler( CommonUtil.toString(settings.get(ORMConfigurationImpl.EVENT_HANDLER, defaultConfig.eventHandler()), defaultConfig.eventHandler()) );

		// eventHandling
		Boolean b = CommonUtil.toBoolean(settings.get(ORMConfigurationImpl.EVENT_HANDLING, null), null);
		if (b == null) {
			if (defaultConfig.eventHandling()) b = Boolean.TRUE;
			else b = !newConfig.eventHandler().trim().isEmpty();
		}
		newConfig.setEventHandling( b );

		// flushatrequestend
		newConfig.setFlushAtRequestEnd( CommonUtil.toBooleanValue(settings.get(ORMConfigurationImpl.FLUSH_AT_REQUEST_END, defaultConfig.flushAtRequestEnd()), defaultConfig.flushAtRequestEnd()) );

		// logSQL
		newConfig.setLogSQL( CommonUtil.toBooleanValue(settings.get(ORMConfigurationImpl.LOG_SQL, defaultConfig.logSQL()), defaultConfig.logSQL()) );

		// autoManageSession
		newConfig.setAutoManageSession( CommonUtil.toBooleanValue(settings.get(ORMConfigurationImpl.AUTO_MANAGE_SESSION, defaultConfig.autoManageSession()), defaultConfig.autoManageSession()) );

		// skipCFCWithError
		newConfig.setSkipCFCWithError( CommonUtil.toBooleanValue(settings.get(ORMConfigurationImpl.SKIP_CFC_WITH_ERROR, defaultConfig.skipCFCWithError()), defaultConfig.skipCFCWithError()) );

		// savemapping
		newConfig.setSaveMapping( CommonUtil.toBooleanValue(settings.get(ORMConfigurationImpl.SAVE_MAPPING, defaultConfig.saveMapping()), defaultConfig.saveMapping()) );

		// schema
		obj = settings.get(ORMConfigurationImpl.SCHEMA, null);
		if (obj != null) {
			Coll coll = _load(obj);
			newConfig.setSchemaDefault( coll.def == null ? "" : coll.def );
			newConfig.setSchemaMap( coll.map );
		}
		else {
			newConfig.setSchemaDefault( defaultConfig.getSchemaDefault() == null ? "" : defaultConfig.getSchemaDefault() );
			newConfig.setSchemaMap( defaultConfig.getSchemaMap() );
		}

		// secondarycacheenabled
		newConfig.setSecondaryCacheEnabled( CommonUtil.toBooleanValue(settings.get(ORMConfigurationImpl.SECONDARY_CACHE_ENABLED, defaultConfig.secondaryCacheEnabled()), defaultConfig.secondaryCacheEnabled()) );

		// useDBForMapping
		newConfig.setUseDBForMapping( CommonUtil.toBooleanValue(settings.get(ORMConfigurationImpl.USE_DB_FOR_MAPPING, defaultConfig.useDBForMapping()), defaultConfig.useDBForMapping()) );

		// cacheconfig
		obj = settings.get(ORMConfigurationImpl.CACHE_CONFIG, null);
		if (!obj.toString().isEmpty()) {
			newConfig.setCacheConfig( toRes(config, obj) );
		}

		// cacheprovider
		newConfig.setCacheProvider( CommonUtil.toString(
			settings.get(ORMConfigurationImpl.CACHE_PROVIDER, defaultConfig.getCacheProvider()),
			defaultConfig.getCacheProvider()
			).trim()
		);

		// ormconfig
		obj = settings.get(ORMConfigurationImpl.ORM_CONFIG, null);
		if (!obj.toString().isEmpty()) {
			newConfig.setOrmConfig( toRes(config, obj) );
		}
		newConfig.setApplicationContext( ac );

		return newConfig;
	}

	private static Coll _load(Object obj) {
		final Coll coll = new Coll();
		if (obj != null) {
			// multi
			if (CommonUtil.isStruct(obj)) {
				Struct sct = CommonUtil.toStruct(obj, null);
				if (sct != null) {
					Iterator<Entry<Key, Object>> it = sct.entryIterator();
					coll.map = new HashMap<String, String>();
					while (it.hasNext()) {
						Entry<Key, Object> e = it.next();
						String k = e.getKey().getLowerString().trim();
						String v = CommonUtil.toString(e.getValue(), "").trim();

						if ("__default__".equals(k) || "".equals(k)) coll.def = v;
						else coll.map.put(k, v);
					}
				}
			}
			else {
				coll.def = CommonUtil.toString(obj, "").trim();
			}
		}
		return coll;
	}

	private Resource toRes(Config config, Object obj) throws PageException {
        ResourceUtil resourceUtil = CFMLEngineFactory.getInstance().getResourceUtil();
		return resourceUtil.toResourceExisting(CommonUtil.pc(), obj.toString());
	}

}

class Coll {
	Map<String, String> map;
	String def;
}

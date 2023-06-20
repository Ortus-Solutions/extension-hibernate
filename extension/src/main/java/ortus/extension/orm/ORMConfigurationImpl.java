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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package ortus.extension.orm;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.util.ResourceUtil;
import lucee.runtime.type.Struct;
import ortus.extension.orm.util.CommonUtil;

public class ORMConfigurationImpl implements ORMConfiguration {

	public static final int DBCREATE_NONE = 0;
	public static final int DBCREATE_UPDATE = 1;
	public static final int DBCREATE_DROP_CREATE = 2;

	public static final Key AUTO_GEN_MAP = CommonUtil.createKey("autogenmap");
	public static final Key CATALOG = CommonUtil.createKey("catalog");
	public static final Key IS_DEFAULT_CFC_LOCATION = CommonUtil.createKey("isDefaultCfclocation");
	public static final Key DB_CREATE = CommonUtil.createKey("dbCreate");
	public static final Key DIALECT = CommonUtil.createKey("dialect");
	public static final Key FLUSH_AT_REQUEST_END = CommonUtil.createKey("flushAtRequestEnd");
	public static final Key LOG_SQL = CommonUtil.createKey("logSql");
	public static final Key SAVE_MAPPING = CommonUtil.createKey("savemapping");
	public static final Key SCHEMA = CommonUtil.createKey("schema");
	public static final Key SECONDARY_CACHE_ENABLED = CommonUtil.createKey("secondarycacheenabled");
	public static final Key SQL_SCRIPT = CommonUtil.createKey("sqlscript");
	public static final Key USE_DB_FOR_MAPPING = CommonUtil.createKey("useDBForMapping");
	public static final Key CACHE_CONFIG = CommonUtil.createKey("cacheconfig");
	public static final Key CACHE_PROVIDER = CommonUtil.createKey("cacheProvider");
	public static final Key ORM_CONFIG = CommonUtil.createKey("ormConfig");
	public static final Key EVENT_HANDLING = CommonUtil.createKey("eventHandling");
	public static final Key EVENT_HANDLER = CommonUtil.createKey("eventHandler");
	public static final Key AUTO_MANAGE_SESSION = CommonUtil.createKey("autoManageSession");
	public static final Key NAMING_STRATEGY = CommonUtil.createKey("namingstrategy");
	public static final Key CFC_LOCATION = CommonUtil.createKey("cfcLocation");
    public static final Key SKIP_CFC_WITH_ERROR = CommonUtil.createKey("skipCFCWithError");

	private boolean autogenmap = true;
	private Resource[] cfcLocations;
	private Boolean eventHandling = null;
	private boolean flushAtRequestEnd = true;
	private boolean logSQL;
	private boolean saveMapping;
	private boolean secondaryCacheEnabled;
	private boolean useDBForMapping = true;
	private Resource cacheConfig;
	private String cacheProvider;
	private Resource ormConfig;
	private String eventHandler;
	private String namingStrategy;
	private boolean isDefaultCfcLocation = true;
	private boolean skipCFCWithError = true;
	private boolean autoManageSession = true;
	private ApplicationContext applicationContext;

	private Map<String, String> dbCreateMap;
	private String dbCreateDefault = "";

	private Map<String, String> dialectMap;
	private String dialectDefault = "";

	private Map<String, String> schemaMap;
	private String schemaDefault = "";

	private Map<String, String> catalogMap;
	private String catalogDefault = "";

	private Map<String, String> sqlScriptMap;
	private String sqlScriptDefault = "";
	private Config config;

	public ORMConfigurationImpl() {
		autogenmap = true;
		flushAtRequestEnd = true;
		useDBForMapping = true;
	}

	public ORMConfigurationImpl duplicate() {
		ORMConfigurationImpl other = new ORMConfigurationImpl();
		other.autogenmap = autogenmap;
		other.cfcLocations = cfcLocations;
		other.isDefaultCfcLocation = isDefaultCfcLocation;
		other.dbCreateMap = dbCreateMap;
		other.eventHandler = eventHandler;
		other.namingStrategy = namingStrategy;
		other.eventHandling = eventHandling;
		other.flushAtRequestEnd = flushAtRequestEnd;
		other.logSQL = logSQL;
		other.saveMapping = saveMapping;
		other.secondaryCacheEnabled = secondaryCacheEnabled;
		other.useDBForMapping = useDBForMapping;
		other.cacheConfig = cacheConfig;
		other.cacheProvider = cacheProvider;
		other.ormConfig = ormConfig;
		other.autoManageSession = autoManageSession;
		other.skipCFCWithError = skipCFCWithError;

		other.dbCreateDefault = dbCreateDefault;
		other.dbCreateMap = dbCreateMap;
		other.dialectDefault = dialectDefault;
		other.dialectMap = dialectMap;
		other.schemaDefault = schemaDefault;
		other.schemaMap = schemaMap;
		other.catalogDefault = catalogDefault;
		other.catalogMap = catalogMap;

		other.sqlScriptDefault = sqlScriptDefault;
		other.sqlScriptMap = sqlScriptMap;
		return other;
	}

	@Override
	public String hash() { // no longer used in Hibernate 3.5.5.72 and above
		ApplicationContext _ac = applicationContext;
		if (_ac == null) _ac = CommonUtil.pc().getApplicationContext();
		Object ds = _ac.getORMDataSource();
		ORMConfiguration ormConf = _ac.getORMConfiguration();

		StringBuilder data = new StringBuilder().append(ormConf.autogenmap()).append(':').append(ormConf.getCatalog()).append(':').append(ormConf.isDefaultCfcLocation())
				.append(':').append(ormConf.eventHandling()).append(':').append(ormConf.namingStrategy()).append(':').append(ormConf.eventHandler()).append(':')
				.append(ormConf.flushAtRequestEnd()).append(':').append(ormConf.logSQL()).append(':').append(ormConf.autoManageSession()).append(':')
				.append(ormConf.skipCFCWithError()).append(':').append(ormConf.saveMapping()).append(':').append(ormConf.getSchema()).append(':')
				.append(ormConf.secondaryCacheEnabled()).append(':').append(ormConf.useDBForMapping()).append(':').append(ormConf.getCacheProvider()).append(':').append(ds)
				.append(':');

		append(data, ormConf.getCfcLocations());
		append(data, ormConf.getSqlScript());
		append(data, ormConf.getCacheConfig());
		append(data, ormConf.getOrmConfig());

		append(data, dbCreateDefault, dbCreateMap);
		append(data, catalogDefault, catalogMap);
		append(data, dialectDefault, dialectMap);
		append(data, schemaDefault, schemaMap);
		append(data, sqlScriptDefault, sqlScriptMap);

		return CFMLEngineFactory.getInstance().getSystemUtil().hash64b(data.toString());
	}

	private static Resource toRes(Config config, Object obj) {
		try {
			ResourceUtil resourceUtil = CFMLEngineFactory.getInstance().getResourceUtil();
			return resourceUtil.toResourceExisting(CommonUtil.pc(), obj.toString());
		}
		catch (PageException pe) {
			return null;
		}
	}

	private static void append(StringBuilder data, String def, Map<String, String> map) {
		data.append(':').append(def);
		if (map != null) {
			for(Entry<String, String> e : map.entrySet()) {
				data.append(':').append(e.getKey()).append(':').append(e.getValue());
			}
		}
	}

	private void append(StringBuilder data, Resource[] reses) {
		if (reses == null) return;
		for (int i = 0; i < reses.length; i++) {
			append(data, reses[i]);
		}
	}

	private void append(StringBuilder data, Resource res) {
		if (res == null) return;
		if (res.isFile()) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			try {
				data.append(eng.getSystemUtil().hash64b(eng.getIOUtil().toString(res, null)));
				return;
			}
			catch (IOException e) {
			}
		}
		data.append(res.getAbsolutePath()).append(':');
	}

	/**
	 * @return the autogenmap
	 */
	@Override
	public boolean autogenmap() {
		return autogenmap;
	}

	/**
	 * @return the cfcLocation
	 */
	@Override
	public Resource[] getCfcLocations() {
		return cfcLocations;
	}

	@Override
	public boolean isDefaultCfcLocation() {
		return isDefaultCfcLocation;
	}

	@Override
	public int getDbCreate() {
		return dbCreateAsInt(dbCreateDefault);
	}

	public int getDbCreate(String datasourceName) { // FUTURE add to interface
		return dbCreateAsInt(_get(datasourceName, dbCreateDefault, dbCreateMap));
	}

	@Override
	public String getDialect() {
		return dialectDefault;
	}

	public String getDialect(String datasourceName) { // FUTURE add to interface
		return _get(datasourceName, dialectDefault, dialectMap);
	}

	@Override
	public String getSchema() {
		return schemaDefault;
	}

	public String getSchema(String datasourceName) { // FUTURE add to interface
		return _get(datasourceName, schemaDefault, schemaMap);
	}

	@Override
	public String getCatalog() {
		return catalogDefault;
	}

	public String getCatalog(String datasourceName) { // FUTURE add to interface
		return _get(datasourceName, catalogDefault, catalogMap);
	}

	@Override
	public Resource getSqlScript() {
		if (sqlScriptDefault.isEmpty()) return null;
		return toRes(config, sqlScriptDefault);
	}

	public Resource getSqlScript(String datasourceName) { // FUTURE add to interface
		String res = _get(datasourceName, sqlScriptDefault, sqlScriptMap);
		if (res.isEmpty()) return null;
		return toRes(config, res);
	}

	private static String _get(String datasourceName, String def, Map<String, String> map) {
		if (map != null && !datasourceName.isEmpty()) {
			datasourceName = datasourceName.toLowerCase().trim();
			String res = map.get(datasourceName);
			if (!res.isEmpty()) return res;
		}
		return def;
	}


	/**
	 * GETTERS
	 */

	@Override
	public boolean eventHandling() {
		return eventHandling == null ? false : eventHandling.booleanValue();
	}

	@Override
	public String eventHandler() {
		return eventHandler;
	}

	@Override
	public String namingStrategy() {
		return namingStrategy;
	}

	@Override
	public boolean flushAtRequestEnd() {
		return flushAtRequestEnd;
	}

	@Override
	public boolean logSQL() {
		return logSQL;
	}

	@Override
	public boolean saveMapping() {
		return saveMapping;
	}

	@Override
	public boolean secondaryCacheEnabled() {
		return secondaryCacheEnabled;
	}

	@Override
	public boolean useDBForMapping() {
		return useDBForMapping;
	}

	@Override
	public Resource getCacheConfig() {
		return cacheConfig;
	}

	@Override
	public String getCacheProvider() {
		return cacheProvider;
	}

	@Override
	public Resource getOrmConfig() {
		return ormConfig;
	}

	@Override
	public boolean skipCFCWithError() {
		return skipCFCWithError;
	}

	@Override
	public boolean autoManageSession() {
		return autoManageSession;
	}

	public Map<String, String> getDbCreateMap(){
		return dbCreateMap;
	}
	public String getDbCreateDefault(){
		return dbCreateDefault;
	}
	public Map<String, String> getDialectMap(){
		return dialectMap;
	}
	public String getDialectDefault(){
		return dialectDefault;
	}
	public Map<String, String> getSchemaMap(){
		return schemaMap;
	}
	public String getSchemaDefault(){
		return schemaDefault;
	}
	public Map<String, String> getCatalogMap(){
		return catalogMap;
	}
	public String getCatalogDefault(){
		return catalogDefault;
	}
	public Map<String, String> getSqlScriptMap(){
		return sqlScriptMap;
	}
	public String getSqlScriptDefault(){
		return sqlScriptDefault;
	}

	/**
	 * SETTERS
	 */



	public ORMConfiguration setAutogenmap(boolean value){
		this.autogenmap = value;
		return this;
	}
	public ORMConfiguration setCfcLocations(Resource[] value){
		this.cfcLocations = value;
		return this;
	}
	public ORMConfiguration setEventHandling(Boolean value){
		this.eventHandling = value;
		return this;
	}
	public ORMConfiguration setFlushAtRequestEnd(boolean value){
		this.flushAtRequestEnd = value;
		return this;
	}
	public ORMConfiguration setLogSQL(boolean value){
		this.logSQL = value;
		return this;
	}
	public ORMConfiguration setSaveMapping(boolean value){
		this.saveMapping = value;
		return this;
	}
	public ORMConfiguration setSecondaryCacheEnabled(boolean value){
		this.secondaryCacheEnabled = value;
		return this;
	}
	public ORMConfiguration setUseDBForMapping(boolean value){
		this.useDBForMapping = value;
		return this;
	}
	public ORMConfiguration setCacheConfig(Resource value){
		this.cacheConfig = value;
		return this;
	}
	public ORMConfiguration setCacheProvider(String value){
		this.cacheProvider = value;
		return this;
	}
	public ORMConfiguration setOrmConfig(Resource value){
		this.ormConfig = value;
		return this;
	}
	public ORMConfiguration setEventHandler(String value){
		this.eventHandler = value;
		return this;
	}
	public ORMConfiguration setNamingStrategy(String value){
		this.namingStrategy = value;
		return this;
	}
	public ORMConfiguration setIsDefaultCfcLocation(boolean value){
		this.isDefaultCfcLocation = value;
		return this;
	}
	public ORMConfiguration setSkipCFCWithError(boolean value){
		this.skipCFCWithError = value;
		return this;
	}
	public ORMConfiguration setAutoManageSession(boolean value){
		this.autoManageSession = value;
		return this;
	}
	public ORMConfiguration setApplicationContext(ApplicationContext value){
		this.applicationContext = value;
		return this;
	}
	public ORMConfiguration setDbCreateMap(Map<String, String> value){
		this.dbCreateMap = value;
		return this;
	}
	public ORMConfiguration setDbCreateDefault(String value){
		this.dbCreateDefault = value;
		return this;
	}
	public ORMConfiguration setDialectMap(Map<String, String> value){
		this.dialectMap = value;
		return this;
	}
	public ORMConfiguration setDialectDefault(String value){
		this.dialectDefault = value;
		return this;
	}
	public ORMConfiguration setSchemaMap(Map<String, String> value){
		this.schemaMap = value;
		return this;
	}
	public ORMConfiguration setSchemaDefault(String value){
		this.schemaDefault = value;
		return this;
	}
	public ORMConfiguration setCatalogMap(Map<String, String> value){
		this.catalogMap = value;
		return this;
	}
	public ORMConfiguration setCatalogDefault(String value){
		this.catalogDefault = value;
		return this;
	}
	public ORMConfiguration setSqlScriptMap(Map<String, String> value){
		this.sqlScriptMap = value;
		return this;
	}
	public ORMConfiguration setSqlScriptDefault(String value){
		this.sqlScriptDefault = value;
		return this;
	}
	public ORMConfiguration setConfig(Config value){
		this.config = value;
		return this;
	}

	@Override
	public Object toStruct() {

		Resource[] locs = getCfcLocations();
		Array arrLocs = CommonUtil.createArray();
		if (locs != null) for (int i = 0; i < locs.length; i++) {
			arrLocs.appendEL(getAbsolutePath(locs[i]));
		}
		Struct sct = CommonUtil.createStruct();
		sct.setEL(AUTO_GEN_MAP, this.autogenmap());
		sct.setEL(CFC_LOCATION, arrLocs);
		sct.setEL(IS_DEFAULT_CFC_LOCATION, isDefaultCfcLocation());
		sct.setEL(EVENT_HANDLING, eventHandling());
		sct.setEL(EVENT_HANDLER, eventHandler());
		sct.setEL(NAMING_STRATEGY, namingStrategy());
		sct.setEL(FLUSH_AT_REQUEST_END, flushAtRequestEnd());
		sct.setEL(LOG_SQL, logSQL());
		sct.setEL(SAVE_MAPPING, saveMapping());
		sct.setEL(SECONDARY_CACHE_ENABLED, secondaryCacheEnabled());
		sct.setEL(USE_DB_FOR_MAPPING, useDBForMapping());
		sct.setEL(CACHE_CONFIG, getAbsolutePath(getCacheConfig()));
		sct.setEL(CACHE_PROVIDER, getCacheProvider() == null ? "" : getCacheProvider());
		sct.setEL(ORM_CONFIG, getAbsolutePath(getOrmConfig()));

		sct.setEL(CATALOG, externalize(catalogMap, catalogDefault));
		sct.setEL(SCHEMA, externalize(schemaMap, schemaDefault));
		sct.setEL(DB_CREATE, externalize(dbCreateMap, dbCreateDefault));
		sct.setEL(DIALECT, externalize(dialectMap, dialectDefault));
		sct.setEL(SQL_SCRIPT, externalize(sqlScriptMap, sqlScriptDefault));
		return sct;
	}

	private static String getAbsolutePath(Resource res) {
		if (res == null) return "";
		return res.getAbsolutePath();
	}

	public static int dbCreateAsInt(String dbCreate) {
		if (dbCreate == null) dbCreate = "";
		else dbCreate = dbCreate.trim().toLowerCase();

		if ("update".equals(dbCreate)) return DBCREATE_UPDATE;
		if ("dropcreate".equals(dbCreate)) return DBCREATE_DROP_CREATE;
		if ("drop-create".equals(dbCreate)) return DBCREATE_DROP_CREATE;
		return DBCREATE_NONE;
	}

	public static String dbCreateAsString(int dbCreate) {

		switch (dbCreate) {
		case DBCREATE_DROP_CREATE:
			return "dropcreate";
		case DBCREATE_UPDATE:
			return "update";
		}

		return "none";
	}

	private static Object externalize(Map<String, String> map, String def) {
		if (map == null || map.isEmpty()) return def == null ? "" : def;
		Struct sct = CommonUtil.createStruct();
		for(Entry<String, String> e : map.entrySet()){
			if (!e.getValue().isEmpty()) sct.setEL(e.getKey(), e.getValue());
		}
		return sct;
	}
}
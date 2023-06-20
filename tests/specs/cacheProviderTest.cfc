component extends="testbox.system.BaseSpec" {

	public void function run(){
		describe( "2nd-level Cache Provider Support", () => {
			beforeEach( () => {
				var result = _internalRequest( "/tests/testApp/index.cfm?reinitApp=true" );
			} );
			describe( "noCache", () => {
				it( "should not be caching entities", () => {
					var ormSettings = {};
					_internalRequest(
						template: "/tests/testApp/index.cfm",
						url     : { "ormReload" : true },
						forms   : { ormSettings : serializeJSON( ormSettings ) }
					);
					var theTest = () => {
						transaction {
							var testEntity = entityNew( "User", { id : createUUID(), name : "Arthur Dent" } );
							entitySave( testEntity );
						}
						var isInCache = ormGetSessionFactory()
							.getCache()
							.containsEntity( "User", testEntity.getId() );
						if ( isInCache ) {
							throw( "FAILED!" );
						}
					};
					var result = _internalRequest(
						template: "/tests/testApp/index.cfm",
						forms   : {
							ormSettings : serializeJSON( ormSettings ),
							closure     : serialize( theTest )
						}
					);
				} );
			} );
			describe( "EHCache", () => {
				it( "should NOT cache a non-cacheable entity", () => {
					var ormSettings = {
						"secondarycacheenabled" : true,
						"cacheProvider"         : "ehcache",
						"cacheConfig"           : expandPath( "/tests/testApp/ehcache.xml" )
					};
					_internalRequest(
						template: "/tests/testApp/index.cfm",
						url     : { "ormReload" : true },
						forms   : { ormSettings : serializeJSON( ormSettings ) }
					);

					var theTest = () => {
						transaction {
							var fordDealer = entityNew( "Dealership", { id : createUUID(), name : "Fenton Ford" } );
							entitySave( fordDealer );
						}
						var isInCache = ormGetSessionFactory()
							.getCache()
							.containsEntity( "Dealership", fordDealer.getId() );
						if ( isInCache ) {
							throw( "FAILED!" );
						}
					};
					var result = _internalRequest(
						template: "/tests/testApp/index.cfm",
						forms   : {
							ormSettings : serializeJSON( ormSettings ),
							closure     : serialize( theTest )
						}
					);
					writeOutput( result.filecontent );
				} );
				xit( "SHOULD cache a cacheable entity", () => {
					var ormSettings = {
						"secondarycacheenabled" : true,
						"cacheProvider"         : "ehcache",
						"cacheConfig"           : expandPath( "/tests/testApp/ehcache.xml" ),
						"saveMapping"           : true
					};
					_internalRequest(
						template: "/tests/testApp/index.cfm",
						url     : { "ormReload" : true },
						forms   : { ormSettings : serializeJSON( ormSettings ) }
					);

					var theTest = () => {
						transaction {
							var testEntity = entityNew( "User", { id : createUUID(), name : "Arthur Dent" } );
							entitySave( testEntity );
						}
						// ormClearSession();
						// entityLoad( "User", testEntity.getId() );
						// // writeDump( ormGetSessionFactory().getCache() );
						// writeDump( ormGetSessionFactory().getStatistics().getSecondLevelCacheHitCount());
						// writeDump( ormGetSessionFactory().getStatistics().getSecondLevelCachePutCount());
						// abort;
						var isInCache = ormGetSessionFactory()
							.getCache()
							.containsEntity( "User", testEntity.getId() );
						if ( !isInCache ) {
							throw( "FAILED!" );
						}
					};
					var result = _internalRequest(
						template: "/tests/testApp/index.cfm?ormReload=true",
						forms   : {
							ormSettings : serializeJSON( ormSettings ),
							closure     : serialize( theTest )
						}
					);
					writeOutput( result.filecontent );
				} );
			} );
			describe( "JBossCache", () => {
				it( "should throw a bad config error", () => {
					expect( () => {
						var ormSettings = {
							"secondarycacheenabled" : true,
							"cacheProvider"         : "JBossCache"
						};
						_internalRequest(
							template: "/tests/testApp/index.cfm",
							url     : { "ormReload" : true },
							forms   : { ormSettings : serializeJSON( ormSettings ) }
						);
					} ).toThrow();
				} );
			} );
		} )
	}

}

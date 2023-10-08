component {

	// Application properties, modify as you see fit
	this.name               = "ORM Tests " & hash( getCurrentTemplatePath() );
	this.applicationTimeout = createTimespan( 0, 1, 0, 0 );

	this.datasources[ "h2_HRdb" ] = {
		class            : "org.h2.Driver",
		bundleName       : "org.lucee.h2",
		bundleVersion    : "2.1.214.0001L",
		connectionString : "jdbc:h2:./tests/db/HR;MODE=MySQL",
		username         : "",
		password         : "",
		// optional settings
		connectionLimit  : -1, // default:-1
		liveTimeout      : 15, // default: -1; unit: minutes
		validate         : false // default: false
	};
	this.datasource  = "h2";
	this.ormEnabled  = true;
	this.ormSettings = {
		cfclocation      : [ "models" ],
		dbcreate         : "dropcreate",
		saveMapping      : true,
		skipCFCWithError : false,
		datasource       : "h2",
		eventHandling    : true,
		eventHandler     : "models.EventHandler"
	};

	// https://luceeserver.atlassian.net/browse/LDEV-1676
	this.xmlFeatures = {
		externalGeneralEntities                                : true,
		secure                                                 : false,
		// The disallowDoctypeDecl alias is broken in Lucee, so we need to use the full feature string name
		// https://luceeserver.atlassian.net/browse/LDEV-4651
		// disallowDoctypeDecl     : false,
		"http://apache.org/xml/features/disallow-doctype-decl" : false
	};

	// Create testing mapping
	this.mappings[ "/tests" ] = getDirectoryFromPath( getCurrentTemplatePath() );
	// Map back to its root
	rootPath                  = reReplaceNoCase( this.mappings[ "/tests" ], "tests(\\|/)", "" );
	this.mappings[ "/root" ]  = rootPath;

	// custom helpers and mappings for tests ported from the Lucee codebase
	this.mappings[ "testsRoot" ]     = "/tests";
	this.mappings[ "luceeTestRoot" ] = this.mappings[ "testsRoot" ] & "/specs/luceeTests";
	server.helpers                   = new tests.specs.luceeTests.TestHelper();
	request.webAdminPassword         = "commandbox";
	application.ormEventLog          = [];

	public boolean function onApplicationStart(){
		application.ormEventLog = [];
		return true;
	}

	public void function onApplicationEnd( struct applicationScope = {} ){
		return;
	}

	public boolean function onRequestStart( required string targetPage ){
		application.ormEventLog = [];
		if ( url.keyExists( "ormReload" ) ) {
			ormReload();
		}
		if ( url.keyExists( "reinitApp" ) ) {
			applicationStop();
		}
		return true;
	}

	public void function onRequest( required string targetPage ){
		include arguments.targetPage;
		return;
	}

	public void function onRequestEnd(){
		return;
	}

}

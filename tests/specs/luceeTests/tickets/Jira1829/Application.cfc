/**
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
 * License along with this library.  If not, see <http: // www.gnu.org / licenses/>.
 *
 **/
component {

	// THIS LOADS THE DSN CREATOR WHEN INSTALLING CONTENTBOX FOR THE FIRST TIME
	// THIS CAN BE REMOVED AFTER INSTALLATION
	// location("modules/contentbox-dsncreator");
	// Application properties, modify as you see fit
	this.name       = "hibernate-" & hash( getCurrentTemplatePath() );
	request.baseURL = "http://#cgi.HTTP_HOST##getDirectoryFromPath( cgi.SCRIPT_NAME )#";


	this.sessionManagement = true;
	this.sessionTimeout    = createTimespan( 0, 0, 45, 0 );
	this.setClientCookies  = true;
	this.scriptProtect     = false;


	this.mappings[ "testsRoot" ]     = "/tests";
	this.mappings[ "luceeTestRoot" ] = this.mappings[ "testsRoot" ] & "/specs/luceeTests";
	server.helpers                   = new tests.specs.luceeTests.TestHelper();
	this.datasource                  = server.helpers.getDatasource( "h2", expandPath( "db" ) );

	// CONTENTBOX ORM SETTINGS
	this.ormEnabled  = true;
	this.ormSettings = {
		savemapping           : true,
		// ENTITY LOCATIONS, ADD MORE LOCATIONS AS YOU SEE FIT
		cfclocation           : [ "model", "modules" ],
		// THE DIALECT OF YOUR DATABASE OR LET HIBERNATE FIGURE IT OUT, UP TO YOU TO CONFIGURE
		// dialect 			= "MySQLwithInnoDB",
		// DO NOT REMOVE THE FOLLOWING LINE OR AUTO-UPDATES MIGHT FAIL.
		dbcreate              : "update",
		// FILL OUT: IF YOU WANT CHANGE SECONDARY CACHE, PLEASE UPDATE HERE
		secondarycacheenabled : false,
		cacheprovider         : "ehCache",
		// ORM SESSION MANAGEMENT SETTINGS, DO NOT CHANGE
		logSQL                : true,
		flushAtRequestEnd     : false,
		autoManageSession     : false,
		// ORM EVENTS MUST BE TURNED ON FOR CONTENTBOX TO WORK
		skipCFCWithError      : false
	};

	public function onRequestStart(){
		setting requesttimeout=10;
	}

}

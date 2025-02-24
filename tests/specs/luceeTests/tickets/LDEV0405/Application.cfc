/**
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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

	this.name = hash( getCurrentTemplatePath() );

	this.mappings[ "testsRoot" ]     = "/tests";
	this.mappings[ "luceeTestRoot" ] = this.mappings[ "testsRoot" ] & "/specs/luceeTests";
	server.helpers                   = new tests.specs.luceeTests.TestHelper();
	this.datasources                 = {
		"ds1" : server.helpers.getDatasource( "h2", "./tests/db/LDEV-0405-1" ),
		"ds2" : server.helpers.getDatasource( "h2", "./tests/db/LDEV-0405-2" ),
		"ds3" : server.helpers.getDatasource( "h2", "./tests/db/LDEV-0405-3" ),
		"ds4" : server.helpers.getDatasource( "h2", "./tests/db/LDEV-0405-4" )
	};

	this.ormEnabled  = true;
	this.ormSettings = {
		savemapping : true,
		dbcreate    : "update",
		cfcLocation : "orm",
		logSQL      : false,
		datasource  : "ds1"
	};

	public function onRequestStart(){
		setting requesttimeout=10;
	}

}

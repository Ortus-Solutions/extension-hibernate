<!---
	*
	* Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
	* Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	// public function setUp(){}

	public void function test(){
		local.uri    = server.helpers.getTestPath( "tickets/LDEV0881/index.cfm" );
		local.result = _InternalRequest( uri );
		assertEquals( 200, result.status );

		var ref = evaluate( trim( result.fileContent ) );
		if ( isArray( ref ) ) ref = ref[ 1 ];


		assertTrue( isValid( "component", ref ) );
		assertEquals( 1, ref.getId() );

		var code = ref.getCode();
		assertTrue( isValid( "component", code ) );
		assertEquals( 1, code.getId() );
		assertEquals( "a", code.getCode() );
	}

}
</cfscript>

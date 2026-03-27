<cfscript>
	test = new test();
	test.setName( "Testing" );
	entitySave( test );
	result = entityLoadByPK( "test", 1 );
	echo( result.getName() );
</cfscript>

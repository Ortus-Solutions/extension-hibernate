/**
 * Tests for Railo Jira-3049
 */
component extends="testbox.system.BaseSpec" {

	function afterAll(){
		// Cleanup so we can re-run tests
		transaction {
			queryExecute( "DELETE FROM mixed_component" );
			ormFlush();
		}
	}

	function run( testResults, testBox ){
		describe( "Railo Jira-3049, 'mixed component' test'", function(){
			it( "can save a 'MixedComponent'", function(){
				entity = entityNew( "MixedComponent" );
				entity.setUnitId( "hello" );
				entity.setEntityId( "goodbye" );
				entity.setEntityTypeId( 7 );
				entitySave( entity );

				entity = entityNew( "MixedComponent" );
				entity.setUnitId( 1 );
				entity.setEntityId( 1 );
				entity.setEntityTypeId( "7" );
				entitySave( entity );

				entity = entityNew( "MixedComponent" );
				entity.setUnitId( true );
				entity.setEntityId( 1 );
				entity.setEntityTypeId( false );
				entitySave( entity );

				ormFlush();
			} );
		} );
	}

}

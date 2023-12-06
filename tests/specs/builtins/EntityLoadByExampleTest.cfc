component extends="testbox.system.BaseSpec" {

	function beforeAll(){
		queryExecute( "DELETE FROM Auto" );
		variables.testDealer = entityNew( "Dealership", { name : "Mike's Ford", id : createUUID() } );
		entitySave( variables.testDealer );
		ormFlush();

		queryExecute(
			"
			INSERT INTO `Auto`( id, make, model, dealerID, inserted )
			VALUES
				( '#createUUID()#', 'Ford', 'Fusion', '#variables.testDealer.getId()#', true ),
				( '#createUUID()#', 'Ford', 'Focus', '#variables.testDealer.getId()#', true ),
				( '#createUUID()#', 'Ford', 'Mustang', null, true ),
				( '#createUUID()#', 'Ford', 'F-150', null, true )
		"
		);
	}

	public void function run(){
		/**
		 * @TODO: Get this test working. Does entityLoadByExample() currently work?
		 */
		xdescribe( "entityLoadByExample()", () => {
			it( "Can load entities matching the example entity", () => {
				var exampleEntity = entityNew( "Auto", { make : "Ford" } );
				var result        = entityLoadByExample( exampleEntity );
				expect( result ).toBeArray().toHaveLength( 4 );
				result.each( ( entity ) => {
					expect( entity.getMake() ).toBe( exampleEntity.getMake() );
				} )
			} );
			it( "Can load entities matching the example entity, including related entity", () => {
				var exampleEntity = entityNew( "Auto", { make : "Ford", dealer : variables.testDealer } );
				var result        = entityLoadByExample( exampleEntity );
				expect( result ).toBeArray().toHaveLength( 2 );
				result.each( ( entity ) => {
					expect( entity.getMake() ).toBe( exampleEntity.getMake() );
				} )
			} );
		} );
	}

}

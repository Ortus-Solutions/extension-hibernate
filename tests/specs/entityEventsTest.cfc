component extends="testbox.system.BaseSpec" {

	public void function run(){
		describe( "entity event listeners()", () => {
			describe( "preInsert", () => {
				it( "runs preInsert on ORMFlush to change and persist entity state", () => {
					var newCar = entityNew( "Auto", { id : createUUID(), make : "BMW" } );
					expect( newCar.getInserted() ).toBeFalse();
					entitySave( newCar );
					expect( newCar.getInserted() ).toBeFalse();
					ormFlush();
					expect( newCar.getInserted() ).toBeTrue( "preInsert should update 'inserted' boolean" );
					var theID = newCar.getId();

					ormClearSession();
					var persistedCar = entityLoadByPK( "Auto", theID );
					expect( persistedCar.getInserted() ).toBeTrue( "persisted value should be true" );
				} );

				/**
				 * See https://ortussolutions.atlassian.net/browse/OOE-9
				 */
				it( "OOE-9 - runs preInsert on ormFlush and persists date value state changes", () => {
					var theUser = entityNew( "User", { id : createUUID(), name : "Julian" } );
					expect( theUser.getDateCreated() ).toBeNull();
					entitySave( theUser );
					expect( theUser.getDateCreated() ).toBeNull();
					ormFlush();
					expect( theUser.getDateCreated() ).notToBeNull( "preInsert should update created date" );
					var theID = theUser.getId();

					entityReload( theUser );
					expect( theUser.getDateCreated() ).notToBeNull( "persisted value should be todays date" );
				} );
			} );
			describe( "preUpdate", () => {
				it( "runs preUpdate on ORMFlush to change and persist entity state", () => {
					var newCar = entityNew( "Auto", { id : createUUID(), make : "Audi" } );
					expect( newCar.getUpdated() ).toBeFalse();
					entitySave( newCar );
					ormFlush();
					expect( newCar.getUpdated() ).toBeFalse();
					newCar.setModel( "A5" );

					entitySave( newCar );
					ormFlush();

					expect( newCar.getUpdated() ).toBeTrue( "preUpdate should update 'updated' boolean" );
					var theID = newCar.getId();

					ormClearSession();
					var persistedCar = entityLoadByPK( "Auto", theID );
					expect( persistedCar.getUpdated() ).toBeTrue( "persisted value should be true" );
				} );

				/**
				 * See https://ortussolutions.atlassian.net/browse/OOE-9
				 */
				it( "OOE-9 - runs preUpdate on ormFlush and persists date value state changes", () => {
					var theUser = entityNew( "User", { id : createUUID(), name : "Julian" } );
					entitySave( theUser );
					ormFlush();
					entityReload( theUser );
					expect( theUser.getDateUpdated() ).toBeNull();
					theUser.setName( "Julian Halliwell" );

					entitySave( theUser );
					ormFlush();
					entityReload( theUser );

					expect( theUser.getDateUpdated() ).notToBeNull( "preUpdate should update created date" );
					var theID = theUser.getId();

					entityReload( theUser );
					expect( theUser.getDateUpdated() ).notToBeNull( "persisted value should be todays date" );
				} );
			} );
		} );
	}

}

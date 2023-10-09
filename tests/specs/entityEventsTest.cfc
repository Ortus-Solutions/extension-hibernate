component extends="testbox.system.BaseSpec" {

	public void function run(){
		describe( "entity event listeners()", () => {
			describe( "preInsert", () => {
				it( "runs preInsert on ORMFlush to change and persist entity state", () => {
					var newCar = entityNew( "Auto", {
						id : createUUID(),
						make : "BMW"
					} );
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
					var theUser = entityNew( "User", {
						id : createUUID(),
						name : "Julian",
						username: "jwell",
						password: "CF4Life"
					} );
					expect( theUser.getDateCreated() ).toBeNull();
					entitySave( theUser );
					expect( theUser.getDateCreated() ).toBeNull();
					ormFlush();
					expect( theUser.getDateCreated() ).notToBeNull( "preInsert should update created date" );
					var theID = theUser.getId();
	
					entityReload( theUser );
					expect( theUser.getDateCreated() ).notToBeNull( "persisted value should be todays date" );
				});

				it( "OOE-12 - can auto-update notnull password from preInsert", () => {
					expect( () => {
						var theUser = entityNew( "User", {
							id : createUUID(),
							name : "Julian",
							username: "jwell"
						} );
						entitySave( theUser );
						/**
						 * here's where the preInsert() should execute,
						 * detect a null password,
						 * update the password,
						 * and NOT throw a null constraint violation.
						 */
						ormFlush();
					}).notToThrow();
					ormEvictEntity( "User" );
					ormClearSession();
				});

				it( "OOE-12 - still throws on null username", () => {
					var theUser = entityNew( "User", {
						id : createUUID(),
						name : "Julian"
					} );
					expect( () => {
						entitySave( theUser );
						/**
						 * here's where the preInsert() should execute,
						 * complete normally without touching the username,
						 * and throw a null constraint violation because a notnull field is null.
						 */
						ormFlush();
					}).toThrow(); // Sadly, we can't catch "org.hibernate.exception.ConstraintViolationException"
					ormEvictEntity( "User" );
					ormClearSession();
				});

				/**
				 * This tests that the EventsListenerIntegrator
				 * is properly copying entity mutations to the event.getState() object.
				 */
				it( "OOE-14 - can preInsert mutate property on parent entity", () => {
					expect( () => {
						var theAdmin = entityNew( "Admin", {
							id : createUUID(),
							name : "Julian",
							username: "jwell"
						} );
						entitySave( theAdmin );
						/**
						 * here's where the preInsert() should execute on the parent entity,
						 * detect a null password,
						 * update the password,
						 * and NOT throw a null constraint violation.
						 */
						ormFlush();
					}).notToThrow();
					ormEvictEntity( "Admin" );
					ormClearSession();
				});
			});

			describe( "preUpdate", () => {
				it( "runs preUpdate on ORMFlush to change and persist entity state", () => {
					var newCar = entityNew( "Auto", {
						id : createUUID(),
						make : "Audi"
					} );
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
					var theUser = entityNew( "User", {
						id : createUUID(),
						name    : "Julian",
						username: "jwell",
						password: "CF4Life"
					} );
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
				});

				it( "OOE-12 - can auto-update notnull password from preUpdate", () => {
					var theUser = entityNew( "User", {
						id : createUUID(),
						name    : "Julian",
						username: "jwell",
						password: "CF4Life"
					} );
					entitySave( theUser );
					ormFlush();
					entityReload( theUser );
					expect( theUser.getDateUpdated() ).toBeNull();
					theUser.setPassword( nullValue() );
					
					expect( () => {
						entitySave( theUser );
						/**
						 * here's where the preUpdate() should execute,
						 * detect a null password,
						 * update the password,
						 * and NOT throw a null constraint violation.
						 */
						ormFlush();
					}).notToThrow(); // Sadly, we can't catch "org.hibernate.exception.ConstraintViolationException"
					ormEvictEntity( "User" );
					ormClearSession();
				});
				it( "OOE-12 - still throws on null username", () => {
					var theUser = entityNew( "User", {
						id : createUUID(),
						name    : "Julian",
						username: "jwell",
						password: "CF4Life"
					} );
					entitySave( theUser );
					ormFlush();
					entityReload( theUser );
					expect( theUser.getDateUpdated() ).toBeNull();
					theUser.setUsername( nullValue() );
					
					expect( () => {
						/**
						 * here's where the preUpdate() should execute,
						 * complete normally without touching the username,
						 * and throw a null constraint violation because a notnull field is null.
						 */
						entitySave( theUser );
						ormFlush();
					}).toThrow(); // Sadly, we can't catch "org.hibernate.exception.ConstraintViolationException"
					ormEvictEntity( "User" );
					ormClearSession();
				});

				/**
				 * This tests that the EventsListenerIntegrator
				 * is properly copying entity mutations to the event.getState() object.
				 */
				it( "OOE-14 - can preUpdate mutate property on parent entity", () => {
					var theAdmin = entityNew( "Admin", {
						id : createUUID(),
						name    : "Julian",
						username: "jwell",
						password: "CF4Life"
					} );
					entitySave( theAdmin );
					ormFlush();
					entityReload( theAdmin );
					expect( theAdmin.getDateUpdated() ).toBeNull();
					theAdmin.setPassword( nullValue() );
					expect( () => {
						entitySave( theAdmin );
						/**
						 * here's where the preUpdate() should execute,
						 * detect a null password,
						 * update the password,
						 * and NOT throw a null constraint violation.
						 */
						ormFlush();
					}).notToThrow(); // Sadly, we can't catch "org.hibernate.exception.ConstraintViolationException"
					ormEvictEntity( "Admin" );
					ormClearSession();
				});
			});
		} );
	}

}

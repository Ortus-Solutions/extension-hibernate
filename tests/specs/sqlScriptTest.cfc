component extends="testbox.system.BaseSpec" {

	public void function run(){
		describe( "ormConfig.sqlScript .sql file execution upon startup", () => {
			beforeEach( () => {
				var result            = _internalRequest( "/tests/testApp/index.cfm?reinitApp=true" );
				variables.ormSettings = {
					dbcreate  : "dropcreate",
					sqlScript : "/tests/models/testSeeder.sql"
				};
			} );
			it( "should execute on startup and seed the database", () => {
				var sql = "
                    DROP TABLE IF EXISTS ""permissions"";
                    CREATE TABLE ""permissions""(
                        id varchar(36),
                        name varchar(155)
                    );
                    INSERT INTO ""permissions"" (id, name)
                    VALUES (RANDOM_UUID(), 'read_users')
                    ;
                    INSERT INTO ""permissions"" (id, name)
                    VALUES (RANDOM_UUID(), 'edit_users')
                ";
				fileWrite( "/tests/models/testSeeder.sql", sql );

				_internalRequest(
					template: "/tests/testApp/index.cfm",
					url     : { "ormReload" : true },
					forms   : { ormSettings : serializeJSON( variables.ormSettings ) }
				);
				var theTest = () => {
					var firstStatementResult = queryExecute(
						"SELECT * FROM ""permissions"" WHERE name='read_users'"
					);
					if ( firstStatementResult.recordCount != 1 ) {
						throw( "FAILED!" );
					}
					var secondStatementResult = queryExecute(
						"SELECT * FROM ""permissions"" WHERE name='edit_users'"
					);
					if ( secondStatementResult.recordCount != 1 ) {
						throw( "FAILED!" );
					}
				};
			} );
			it( "should throw if sql is not valid", () => {
				fileWrite( "/tests/models/testSeeder.sql", "CREATABLE fudge(id varchar(36))" );

				expect( () => {
					_internalRequest(
						template: "/tests/testApp/index.cfm",
						url     : { "ormReload" : true },
						forms   : { ormSettings : serializeJSON( variables.ormSettings ) }
					);
				} ).toThrow();
			} );
		} )
	}

}

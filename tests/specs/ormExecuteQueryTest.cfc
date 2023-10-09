component extends="testbox.system.BaseSpec" {

	function beforeAll(){
		queryExecute( "DELETE from Auto WHERE make='Ford'" );
		entitySave( entityNew( "Auto", { make : "Ford", id : createUUID() } ) );
		ormFlush();
	}

	public void function run(){
		describe( "ormExecuteQuery()", () => {
			it( "can use inline parameters", () => {
				// inline parameter
				result = ormExecuteQuery( hql: "SELECT id FROM Auto WHERE make = 'Ford'", unique: true );
				expect( isValid( "uuid", result ) ).toBe( true );
			} );
			it( "can pass struct of query parameters", () => {
				// struct parameter
				result = ormExecuteQuery(
					"SELECT id FROM Auto WHERE make = :make",
					{ make : "Ford" },
					true
				);
				expect( isValid( "uuid", result ) ).toBe( true );
			} );
			it( "can pass array of query parameters", () => {
				// array parameter
				result = ormExecuteQuery(
					"SELECT id FROM Auto WHERE  make = ?1",
					[ "Ford" ],
					true
				);
				expect( isValid( "uuid", result ) ).toBe( true );
			} );
			it( "can use legacy ? parameter syntax", () => {
				// legacy parameter
				result = ormExecuteQuery(
					"SELECT id FROM Auto WHERE  make = ?",
					[ "Ford" ],
					true
				);
				expect( isValid( "uuid", result ) ).toBe( true );
			} );
			it( "can use named parameter syntax with options struct", () => {
				expect(
					ormExecuteQuery(
						"
                        select count(id)
                        from Auto
                        WHERE make = :make
                    ",
						{ make : "Ford" },
						false,
						{ cacheable : false }
					)
				).toBeArray().toHaveLength( 1 );
			} );
			it( "can use count() with unique=true", () => {
				expect(
					ormExecuteQuery(
						"
                        select count(id)
                        from Auto
                        WHERE make = :make
                    ",
						{ make : "Ford" },
						true
					)
				).toBeNumeric( 1 ).toBe( 1 );
			} );
			it( "can use count() with unique=true and options struct", () => {
				expect(
					ormExecuteQuery(
						"
                        select count(id)
                        from Auto
                        WHERE make = :make
                    ",
						{ make : "Ford" },
						true,
						{ cacheable : false }
					)
				).toBeNumeric( 1 ).toBe( 1 );
			} );
		} );
		describe( "ormQueryExecute()", () => {
			it( "can use inline parameters", () => {
				// inline parameter
				result = ormQueryExecute( hql: "SELECT id FROM Auto WHERE make = 'Ford'", unique: true );
				expect( isValid( "uuid", result ) ).toBe( true );
			} );
			it( "can pass struct of query parameters", () => {
				// struct parameter
				result = ormQueryExecute(
					"SELECT id FROM Auto WHERE make = :make",
					{ make : "Ford" },
					true
				);
				expect( isValid( "uuid", result ) ).toBe( true );
			} );
			it( "can pass array of query parameters", () => {
				// array parameter
				result = ormQueryExecute(
					"SELECT id FROM Auto WHERE  make = ?1",
					[ "Ford" ],
					true
				);
				expect( isValid( "uuid", result ) ).toBe( true );
			} );
			it( "can use legacy ? parameter syntax", () => {
				// legacy parameter
				result = ormQueryExecute(
					"SELECT id FROM Auto WHERE  make = ?",
					[ "Ford" ],
					true
				);
				expect( isValid( "uuid", result ) ).toBe( true );
			} );
			it( "can use named parameter syntax with options struct", () => {
				expect(
					ormQueryExecute(
						"
                        select count(id)
                        from Auto
                        WHERE make = :make
                    ",
						{ make : "Ford" },
						false,
						{ cacheable : false }
					)
				).toBeArray().toHaveLength( 1 );
			} );
			it( "can use count() with unique=true", () => {
				expect(
					ormQueryExecute(
						"
                        select count(id)
                        from Auto
                        WHERE make = :make
                    ",
						{ make : "Ford" },
						true
					)
				).toBeNumeric( 1 ).toBe( 1 );
			} );
			it( "can use count() with unique=true and options struct", () => {
				expect(
					ormQueryExecute(
						"
                        select count(id)
                        from Auto
                        WHERE make = :make
                    ",
						{ make : "Ford" },
						true,
						{ cacheable : false }
					)
				).toBeNumeric( 1 ).toBe( 1 );
			} );
		} );
	}

}

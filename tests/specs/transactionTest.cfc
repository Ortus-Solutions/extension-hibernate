component extends="testbox.system.BaseSpec" {

	public void function run(){
		describe( "all transaction functionality", () => {
			// TODO: Fix this!
			xdescribe( "transactionCommit()", () => {
				it( "can transactionCommit entire transaction", () => {
					transaction {
						theDealer = entityNew(
							"Dealership",
							{
								"name"    : "Uptown Auto",
								"address" : "123 Auto Way",
								"phone"   : "123-456-7890",
								"id"      : createUUID()
							}
						);

						entitySave( theDealer );
						expect(
							queryExecute( "SELECT * FROM Dealership WHERE id=:id", { id : theDealer.getId() } ).recordCount
						).toBe( 0, "dealer should not exist before commit" );
						transactionCommit();
						expect(
							queryExecute( "SELECT * FROM Dealership WHERE id=:id", { id : theDealer.getId() } ).recordCount
						).toBe( 1, "dealer should exist after commit" );
					}
				} );

				// Skipped until savepoints are supported.
				it( "Can commit a single savepoint", () => {
					transaction {
						theDealer = entityNew(
							"Dealership",
							{
								"name"    : "Uptown Auto",
								"address" : "123 Auto Way",
								"phone"   : "123-456-7890",
								"id"      : createUUID()
							}
						);

						entitySave( theDealer );
						transactionSetSavepoint( "dealer-saved" );
						transactionCommit();

						theAuto = entityNew(
							"Auto",
							{
								"make"  : "Hyundai",
								"model" : "Elantra",
								"id"    : createUUID()
							}
						);

						entitySave( theAuto );
						transactionCommit();
					}
					expect(
						queryExecute( "SELECT * FROM Dealer WHERE id=:id", { id : theDealer.getId() } ).recordCount
					).toBe( 1, "should have saved dealer" );
					expect(
						queryExecute( "SELECT * FROM Auto WHERE id=:id", { id : theAuto.getId() } ).recordCount
					).toBe( 0, "should not have saved auto" );
				} );
			} );

			describe( "transactionRollback()", () => {
				it( "can roll back entire transaction", () => {
					transaction {
						theDealer = entityNew(
							"Dealership",
							{
								"name"    : "Uptown Auto",
								"address" : "123 Auto Way",
								"phone"   : "123-456-7890",
								"id"      : createUUID()
							}
						);

						entitySave( theDealer );
						transactionRollback();
					}
					expect(
						queryExecute( "SELECT * FROM Dealership WHERE id=:id", { id : theDealer.getId() } ).recordCount
					).toBe( 0, "dealer should not exist in DB" );
				} );

				// Skipped until savepoints are supported.
				xit( "Can roll back to savepoints", () => {
					transaction {
						theDealer = entityNew(
							"Dealership",
							{
								"name"    : "Uptown Auto",
								"address" : "123 Auto Way",
								"phone"   : "123-456-7890",
								"id"      : createUUID()
							}
						);

						entitySave( theDealer );
						transactionSetSavepoint( "dealer-saved" );

						theAuto = entityNew(
							"Auto",
							{
								"make"  : "Hyundai",
								"model" : "Elantra",
								"id"    : createUUID()
							}
						);

						entitySave( theAuto );
						transactionRollback( "dealer-saved" );
					}
					expect(
						queryExecute( "SELECT * FROM Dealer WHERE id=:id", { id : theDealer.getId() } ).recordCount
					).toBe( 1, "should have saved dealer" );
					expect(
						queryExecute( "SELECT * FROM Auto WHERE id=:id", { id : theAuto.getId() } ).recordCount
					).toBe( 0, "should not have saved auto" );
				} );
			} );

			// Skipped until savepoints are supported.
			xdescribe( "transactionSetSavepoint()", () => {
				it( "Doesn't error on an ORM transaction", () => {
					transaction {
						myEntity = entityNew(
							"Auto",
							{
								"make"  : "Hyundai",
								"model" : "Accent",
								"id"    : createUUID()
							}
						);

						entitySave( myEntity );
						transactionSetSavepoint();
					}
					expect(
						queryExecute( "SELECT * FROM Auto WHERE id=:id", { id : myEntity.getId() } ).recordCount
					).toBe( 1 );
				} );
				it( "Accepts a savepoint name", () => {
					transaction {
						myEntity = entityNew(
							"Auto",
							{
								"make"  : "Hyundai",
								"model" : "Elantra",
								"id"    : createUUID()
							}
						);

						entitySave( myEntity );
						transactionSetSavepoint( "" );
					}
					expect(
						queryExecute( "SELECT * FROM Auto WHERE id=:id", { id : myEntity.getId() } ).recordCount
					).toBe( 1 );
				} );
			} );

			xdescribe( "transaction isolation", () => {
				/**
				 * From Lucee source: `core/src/main/java/lucee/runtime/tag/Transaction.java`
				 * [read_uncommitted,read_committed,repeatable_read,serializable]
				 */
				xit( "serializable", () => {
					transaction isolation="serializable" {
						myEntity = entityNew(
							"Auto",
							{
								"make"  : "Hyundai",
								"model" : "Elantra",
								"id"    : createUUID()
							}
						);

						entitySave( myEntity );
					}
					expect(
						queryExecute( "SELECT * FROM Auto WHERE id=:id", { id : myEntity.getId() } ).recordCount
					).toBe( 1 );
				} );

				// @TODO: Implement!
				xit( "read_uncommitted", () => {
					expect( false ).toBeTrue();
				} );

				// @TODO: Implement!
				xit( "repeatable_read", () => {
					expect( false ).toBeTrue();
				} );

				// @TODO: Implement!
				xit( "snapshot", () => {
					expect( false ).toBeTrue();
				} );
			} );
		} )
	}

}

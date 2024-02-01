package ortus.extension.orm;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import lucee.runtime.orm.ORMTransaction;

/**
 * Hibernate Transaction wrapper object. Useful for opening, closing, and general management of a single transaction.
 */
public class HibernateORMTransaction implements ORMTransaction {

	private Transaction	trans;
	private Session		session;
	private boolean		doRollback;
	private boolean		autoManage;

	/**
	 * Constructor. Does NOT open a Hibernate transaction at this time.
	 * <p>
	 * To open a Hibernate transaction, call begin() after this method:
	 *
	 * <pre>
	 * HibernateORMTransaction tx = new HibernateORMTransaction( session, true );
	 * tx.begin();
	 * </pre>
	 *
	 * @param session
	 *                   Hibernate session to open a transaction on
	 * @param autoManage
	 *                   Should the Transaction be auto-managed
	 */
	public HibernateORMTransaction( Session session, boolean autoManage ) {
		this.session	= session;
		this.autoManage	= autoManage;
	}

	/**
	 * Open or acquire a Transaction for the current session.
	 * <p>
	 * Will flush the current session if autoManage is enabled.
	 *
	 * @see org.hibernate.SharedSessionContract#getTransaction()
	 */
	@Override
	public void begin() {
		if ( autoManage ) {
			session.flush();
		}
		trans = session.getTransaction();

	}

	/**
	 * Commit the transaction.
	 *
	 * Just kidding... right now this method does nothing.
	 */
	@Override
	public void commit() {
		// do nothing
	}

	/**
	 * Mark the transaction for rollback.
	 *
	 * Will only execute a rollback on {@link #end()}
	 */
	@Override
	public void rollback() {
		doRollback = true;
	}

	/**
	 * Wrap up the transaction.
	 * <ul>
	 * <li>Will roll back if rollback() called.
	 * <li>Will commit if transaction already committed. 🤯
	 * <li>May flush the session or clear the session depending on transaction state and autoManage settings.
	 * <li>(currently) closes the session on execution. (See LDEV-4017)
	 * </ul>
	 */
	@Override
	public void end() {
		if ( doRollback ) {
			trans.rollback();
			if ( autoManage ) {
				session.clear();
			}
		} else {
			if ( trans.getStatus() == TransactionStatus.COMMITTED ) {
				trans.commit();
			}
			session.flush();
		}
	}

	/**
	 * Retrieve the internal Hibernate transaction
	 *
	 * @return a Hibernate {@link org.hibernate.Transaction} object.
	 */
	public Transaction getTransaction() {
		return trans;
	}
}

/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package ortus.extension.orm.tags;

import java.sql.Connection;

import javax.servlet.jsp.JspException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lucee.loader.util.Util;
import lucee.runtime.db.DataSourceManager;
import lucee.runtime.exp.PageException;
import ortus.extension.orm.util.ExceptionUtil;

/**
 * Transaction class
 */
public final class Transaction extends BodyTagTryCatchFinallyImpl {

	private static final int ACTION_NONE = 0;

	private static final int ACTION_BEGIN = 1;

	private static final int ACTION_COMMIT = 2;

	private static final int ACTION_ROLLBACK = 4;

	private static final int ACTION_SET_SAVEPOINT = 8;

    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);

	private int isolation = Connection.TRANSACTION_NONE;
	private int action = ACTION_NONE;
	private boolean innerTag = false;

	private boolean ignore = false;

	private String savepoint;

	@Override
	public void release() {
		isolation	= Connection.TRANSACTION_NONE;
		action		= ACTION_NONE;
		innerTag	= false;
		ignore		= false;
		savepoint	= null;
		super.release();
	}

	/**
	 * @param action The action to set.
	 * 
	 * @throws PageException
	 */
	public void setAction( String strAction ) throws PageException {
		strAction = strAction.trim().toLowerCase();
		if ( strAction.equals( "begin" ) )
			action = ACTION_BEGIN;
		else if ( strAction.equals( "commit" ) )
			action = ACTION_COMMIT;
		else if ( strAction.equals( "rollback" ) )
			action = ACTION_ROLLBACK;
		else if ( strAction.equals( "setsavepoint" ) )
			action = ACTION_SET_SAVEPOINT;
		else {
			throw ExceptionUtil.createException(
					"Attribute [action] has an invalid value, valid values are [begin,commit,setsavepoint and rollback]" );
		}

	}

	/**
	 * @param isolation The isolation to set.
	 * 
	 * @throws PageException
	 */
	public void setIsolation( String isolation ) throws PageException {
		isolation = isolation.trim().toLowerCase();
		if ( isolation.equals( "read_uncommitted" ) )
			this.isolation = Connection.TRANSACTION_READ_UNCOMMITTED;
		else if ( isolation.equals( "read_committed" ) )
			this.isolation = Connection.TRANSACTION_READ_COMMITTED;
		else if ( isolation.equals( "repeatable_read" ) )
			this.isolation = Connection.TRANSACTION_REPEATABLE_READ;
		else if ( isolation.equals( "serializable" ) )
			this.isolation = Connection.TRANSACTION_SERIALIZABLE;
		else if ( isolation.equals( "none" ) )
			this.isolation = Connection.TRANSACTION_NONE;
		else
			throw ExceptionUtil.createException(
					"Transaction has an invalid isolation level (attribute [isolation], valid values are [read_uncommitted,read_committed,repeatable_read,serializable])" );
	}

	/**
	 * @param isolation The isolation to set.
	 */
	public void setSavepoint( String savepoint ) {
		logger.atInfo().log( "Transaction setSavepoint running" );
		if ( Util.isEmpty( savepoint, true ) )
			this.savepoint = null;
		else
			this.savepoint = savepoint.trim().toLowerCase();
	}

	@Override
	public int doStartTag() throws PageException {
		logger.atInfo().log( "Transaction doStartTag running" );
		DataSourceManager manager = pageContext.getDataSourceManager();
		// first transaction
		if ( manager.isAutoCommit() ) {
			manager.begin( isolation );
			return EVAL_BODY_INCLUDE;
		}
		// inside transaction
		innerTag = true;
		switch ( action ) {
			case ACTION_NONE :
			case ACTION_BEGIN :
				// nested transaction no longer throw an exception, they are simply ignored
				ignore = true;
				break;

			case ACTION_COMMIT :
				manager.commit();
				break;
			case ACTION_ROLLBACK :
				manager.rollback();
				break;
			case ACTION_SET_SAVEPOINT :
				manager.savepoint();
				break;
		}

		return EVAL_BODY_INCLUDE;
	}

	@Override
	public void doCatch( Throwable t ) throws Throwable {
		logger.atInfo().log( "Transaction doCatch running..." );
		ExceptionUtil.rethrowIfNecessary( t );
		if ( innerTag || ignore )
			throw t;

		DataSourceManager manager = pageContext.getDataSourceManager();
		try {
			manager.rollback();
		} catch ( PageException e ) {
			// print.printST(e);
		}
		throw t;
	}

	/**
	 * @param hasBody
	 */
	public void hasBody( boolean hasBody ) {
	}

	@Override
	public void doFinally() {
		logger.atInfo().log( "Transaction doFinally running..." );
		if ( !ignore && !innerTag ) {
			pageContext.getDataSourceManager().end();
		}
		super.doFinally();
	}

	@Override
	public int doAfterBody() throws JspException {
		logger.atInfo().log( "Transaction doAfterBody running..." );
		if ( !ignore && !innerTag ) {
			pageContext.getDataSourceManager().commit();
		}
		return super.doAfterBody();
	}
}

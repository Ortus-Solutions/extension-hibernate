package ortus.extension.orm.tuplizer.proxy;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import lucee.runtime.Component;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.ObjectWrap;

/**
 * Proxy for "dynamic-map" entity representations. SLOW
 */
public class CFCHibernateProxy extends ComponentProxy implements HibernateProxy, ObjectWrap {

	private static final long	serialVersionUID	= 4115236247834562085L;

	private CFCLazyInitializer	li;

	@Override
	public Component getComponent() {
		return li.getCFC();
	}

	public CFCHibernateProxy( CFCLazyInitializer li ) {
		this.li = li;
	}

	@Override
	public Object writeReplace() {
		return this;
	}

	@Override
	public LazyInitializer getHibernateLazyInitializer() {
		return li;
	}

	@Override
	public Object getEmbededObject( Object defaultValue ) {
		return getComponent();
	}

	@Override
	public Object getEmbededObject() throws PageException {
		return getComponent();
	}
}
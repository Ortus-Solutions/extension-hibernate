package ortus.extension.orm.util;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.io.StringWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

/**
 * Utility class for reading (parsing) and writing XML documents.
 *
 * Most of the hard work is phoned out (reflected) to Lucee's own XMLUtil, `lucee.runtime.text.xml.XMLUtil`.
 */
public class XMLUtil {

	private XMLUtil() {
		throw new IllegalStateException( "Utility class; please don't instantiate!" );
	}

	/**
	 * Hardcoded path to Lucee's XML util which we use via reflection.
	 */
	public static final String LUCEE_XML_UTIL_PATH = "lucee.runtime.text.xml.XMLUtil";

	public static InputSource toInputSource( Object obj ) throws PageException {
		// FUTURE use interface from loader
		try {
			Class<?>	clazz	= CFMLEngineFactory.getInstance().getClassUtil().loadClass( XMLUtil.LUCEE_XML_UTIL_PATH );
			Method		method	= clazz.getMethod( "toInputSource", new Class[] { Object.class } );
			return ( InputSource ) method.invoke( null, obj );
		} catch ( Exception e ) {
			throw ExceptionUtil.toPageException( e );
		}
	}

	public static InputSource toInputSource( Resource res, Charset cs ) throws PageException {
		// FUTURE use interface from loader
		try {
			Class<?>	clazz	= CFMLEngineFactory.getInstance().getClassUtil().loadClass( XMLUtil.LUCEE_XML_UTIL_PATH );
			Method		method	= clazz.getMethod( "toInputSource", new Class[] { Resource.class, Charset.class } );
			return ( InputSource ) method.invoke( null, res, cs );
		} catch ( Exception e ) {
			throw ExceptionUtil.toPageException( e );
		}
	}

	public static final Document parse( InputSource xml, InputSource validator, boolean isHtml ) throws PageException {
		// FUTURE use interface from loader
		try {
			Class<?>	clazz	= CFMLEngineFactory.getInstance().getClassUtil().loadClass( XMLUtil.LUCEE_XML_UTIL_PATH );
			Method		method	= clazz.getMethod( "parse", new Class[] { InputSource.class, InputSource.class, boolean.class } );
			return ( Document ) method.invoke( null, xml, validator, isHtml );
		} catch ( Exception e ) {
			throw ExceptionUtil.toPageException( e );
		}
	}

	public static Document newDocument() throws PageException {
		// FUTURE use interface from loader
		try {
			Class<?>	clazz	= CFMLEngineFactory.getInstance().getClassUtil().loadClass( XMLUtil.LUCEE_XML_UTIL_PATH );
			Method		method	= clazz.getMethod( "newDocument" );
			return ( Document ) method.invoke( null );
		} catch ( Exception e ) {
			throw ExceptionUtil.toPageException( e );
		}
	}

	/**
	 * Generate an XML string from the provided w3c Document Element.
	 *
	 * @param document
	 *                 The root element of an XML document.
	 *
	 * @return a fully-formed and formatted XML string. Does not append or prepend <xml> tags or DOCTYPE, etc.
	 *
	 * @throws PageException
	 */
	public static String toString( Element document ) throws PageException {
		try {
			TransformerFactory	tf			= TransformerFactory.newInstance();
			Transformer			transformer	= tf.newTransformer();
			transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
			StringWriter writer = new StringWriter();
			transformer.transform( new DOMSource( document ), new StreamResult( writer ) );
			return writer.toString();
		} catch ( Exception e ) {
			throw ExceptionUtil.toPageException( e );
		}
	}

	public static Node toNode( Object value ) throws PageException {
		if ( value instanceof Node )
			return ( Node ) value;
		return parse( toInputSource( value ), null, false );
	}

	public static Document getDocument( Node node ) {
		if ( node instanceof Document )
			return ( Document ) node;
		return node.getOwnerDocument();
	}

	public static void setFirst( Node parent, Node node ) {
		Node first = parent.getFirstChild();
		if ( first != null )
			parent.insertBefore( node, first );
		else
			parent.appendChild( node );
	}

}

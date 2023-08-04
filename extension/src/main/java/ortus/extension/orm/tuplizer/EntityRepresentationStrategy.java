package ortus.extension.orm.tuplizer;

import org.hibernate.metamodel.spi.ManagedTypeRepresentationStrategy;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.mapping.Property;

import ortus.extension.orm.tuplizer.accessors.CFCGetter;
import ortus.extension.orm.tuplizer.accessors.CFCSetter;

public class EntityRepresentationStrategy implements ManagedTypeRepresentationStrategy {


    /**
     * Build and return a PropertyAccess class instance which defines the Getter and Setter to use for this representation mode.
     * 
     * https://docs.jboss.org/hibernate/stable/core/javadocs/org/hibernate/property/access/spi/PropertyAccess.html
     * https://docs.jboss.org/hibernate/stable/core/javadocs/org/hibernate/mapping/Property.html
     *  
     * @param bootAttributeDescriptor
     * @return
     */
    public PropertyAccess resolvePropertyAccess​(Property bootAttributeDescriptor){
        return new PropertyAccess(){
            /**
             * Obtain the delegate for getting values of the persistent attribute.
             * 
             * https://docs.jboss.org/hibernate/stable/core/javadocs/org/hibernate/property/access/spi/Getter.html
             */
            public final Getter getGetter(){
                return new CFCGetter(null, null, null);
            }
            /**
             * Access to the PropertyAccessStrategy that created this instance.
             * 
             * https://docs.jboss.org/hibernate/stable/core/javadocs/org/hibernate/property/access/spi/PropertyAccessStrategy.html
             */
            public final PropertyAccessStrategy getPropertyAccessStrategy(){

            }
            /**
             * Obtain the delegate for setting values of the persistent attribute.
             * 
             * https://docs.jboss.org/hibernate/stable/core/javadocs/org/hibernate/property/access/spi/Setter.html
             */
            public final Setter getSetter(){
                return new CFCSetter(null, null, null);
            }
        };
    }
}
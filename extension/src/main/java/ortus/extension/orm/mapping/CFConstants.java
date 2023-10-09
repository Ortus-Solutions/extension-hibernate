package ortus.extension.orm.mapping;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CFConstants {

    private CFConstants() {
        throw new IllegalStateException( "Utility class; please don't instantiate!" );
    }

    public static class CacheUse {
        public static final String READ_ONLY            = "read-only";
        public static final String NONSTRICT_READ_WRITE = "nonstrict-read-write";
        public static final String READ_WRITE           = "read-write";
        public static final String TRANSACTIONAL        = "transactional";

        public static final String ATTRIBUTE_NAME = "cacheuse";
        public static final String HBM_KEY = "usage";

        public static List<String> getPossibleValues(){
            return Stream.of(READ_ONLY, NONSTRICT_READ_WRITE, READ_WRITE, TRANSACTIONAL).collect(Collectors.toList());
        }
        public static boolean isValidValue( String value ){
            return getPossibleValues().contains( value );
        }
    }

    public static class CollectionType {
        public static final String ARRAY  = "array";
        public static final String BAG    = "bag";
        public static final String STRUCT = "struct";
        public static final String MAP    = "map";

        public static final String ATTRIBUTE_NAME = "collectiontype";
        public static final String HBM_KEY        = "type";

        public static boolean isBagType( String typeName ){
            return ARRAY.equalsIgnoreCase(typeName) || BAG.equalsIgnoreCase(typeName);
        }
        public static boolean isMapType( String typeName ){
            return STRUCT.equalsIgnoreCase(typeName) || MAP.equalsIgnoreCase(typeName);
        }
        public static List<String> getPossibleValues(){
            return Stream.of(ARRAY, BAG, STRUCT, MAP).collect(Collectors.toList());
        }
        public static boolean isValidValue( String value ){
            return getPossibleValues().contains( value );
        }
    }

    public static class VersionDataType {
        public static final String INTEGER = "integer";
        public static final String INT     = "int";
        public static final String LONG    = "long";
        public static final String SHORT   = "short";

        public static final String ATTRIBUTE_NAME = "datatype";
        public static final String HBM_KEY        = "type";

        public static List<String> getPossibleValues(){
            return Stream.of(INTEGER, INT, LONG, SHORT).collect(Collectors.toList());
        }
        public static boolean isValidValue( String value ){
            return getPossibleValues().contains( value );
        }
    }

    public static class VersionUnsavedValue extends CFConstants.UnsavedValue {
        public static final String NEGATIVE = "negative";

        public static List<String> getPossibleValues(){
            return Stream.of(NULL, UNDEFINED,NEGATIVE).collect(Collectors.toList());
        }
    }

    public static class Fetch {
        public static final String JOIN   = "join";
        public static final String SELECT = "select";

        public static final String ATTRIBUTE_NAME = "fetch";
        public static final String HBM_KEY        = "fetch";

        public static List<String> getPossibleValues(){
            return Stream.of(JOIN, SELECT).collect(Collectors.toList());
        }
        public static boolean isValidValue( String value ){
            return getPossibleValues().contains( value );
        }
    }

    public static class Generated {
        public static final String ALWAYS = "always";
        public static final String INSERT = "insert";
        public static final String NEVER  = "never";

        public static final String ATTRIBUTE_NAME = "generated";
        public static final String HBM_KEY = "generated";

        public static List<String> getPossibleValues(){
            return Stream.of(ALWAYS, INSERT, NEVER).collect(Collectors.toList());
        }
        public static boolean isValidValue( String value ){
            return getPossibleValues().contains( value );
        }
    }

    public static class Lazy {
        public static final String PROXY    = "proxy";
        public static final String NO_PROXY = "no-proxy";
        public static final String TRUE     = "true";
        public static final String FALSE    = "false";
        public static final String EXTRA    = "extra";

        public static final String ATTRIBUTE_NAME = "lazy";
        public static final String HBM_KEY        = "lazy";

        public static List<String> getPossibleForRelationship(){
            return Stream.of(PROXY, NO_PROXY, TRUE, FALSE).collect(Collectors.toList());
        }

        public static List<String> getPossibleForSimple(){
            return Stream.of(TRUE, FALSE, EXTRA).collect(Collectors.toList());
        }

        public static boolean isValidForRelationship( String value ){
            return getPossibleForRelationship().contains( value );
        }

        public static boolean isValidForSimple( String value ){
            return getPossibleForSimple().contains( value );
        }
    }

    public static class OptimisticLock {
        public static final String ALL     = "all";
        public static final String DIRTY   = "dirty";
        public static final String NONE    = "none";
        public static final String VERSION = "version";

        public static final String ATTRIBUTE_NAME = "optimisticLock";
        public static final String HBM_KEY = "optimistic-lock";

        public static List<String> getPossibleValues(){
            return Stream.of(ALL, DIRTY, NONE, VERSION).collect(Collectors.toList());
        }
        public static boolean isValidValue( String value ){
            return getPossibleValues().contains( value );
        }
    }

    public static class Source {
        public static final String VM = "vm";
        public static final String DB = "db";

        public static final String ATTRIBUTE_NAME = "source";
        public static final String HBM_KEY = "source";

        public static List<String> getPossibleValues(){
            return Stream.of(VM, DB).collect(Collectors.toList());
        }
        public static boolean isValidValue( String value ){
            return getPossibleValues().contains( value );
        }
    }

    public static class UnsavedValue {
        public static final String NULL      = "null";
        public static final String UNDEFINED = "undefined";

        public static final String ATTRIBUTE_NAME = "source";
        public static final String HBM_KEY        = "source";

        public static List<String> getPossibleValues(){
            return Stream.of(NULL, UNDEFINED).collect(Collectors.toList());
        }
        public static boolean isValidValue( String value ){
            return getPossibleValues().contains( value );
        }
    }
    
    /**
     *  @TODO: Move this into some CFConstants class, or somewhere that both HBMCreator and HibernateCaster can reference it.
     * @TODO: For 7.0, Migrate to Map.of() or Map.ofEntries in Java 9+
     */
    public static class Relationships {
        public static final String ONE_TO_MANY     = "one-to-many";
        public static final String MANY_TO_MANY    = "many-to-many";
        public static final String MANY_TO_ONE     = "many-to-one";
        public static final String ONE_TO_ONE      = "one-to-one";
        public static final String KEY_MANY_TO_ONE = "key-many-to-one";

        private Relationships() {
            throw new IllegalStateException( "Utility class; please don't instantiate!" );
        }

        /**
         * Identify whether the given string denotes one of the four main relationship types:
         * - one-to-many
         * - one-to-one
         * - many-to-many
         * - many-to-one
         * 
         * @param fieldType Field type string from a persistent property, like "one-to-many" or "id"
         * @return Boolean
         */
        public static boolean isRelationshipType( String fieldType ){
            return 
                ONE_TO_MANY.equalsIgnoreCase(fieldType) ||
                MANY_TO_MANY.equalsIgnoreCase(fieldType) ||
                MANY_TO_ONE.equalsIgnoreCase(fieldType) ||
                ONE_TO_ONE.equalsIgnoreCase(fieldType) ||
                KEY_MANY_TO_ONE.equalsIgnoreCase(fieldType);
        }
    }
}

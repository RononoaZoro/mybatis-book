package com.luo.ibatis.session;

import com.luo.ibatis.binding.ArcherMapperRegistry;
import com.luo.ibatis.builder.xml.ArcherXMLStatementBuilder;
import com.luo.ibatis.datasource.unpooled.ArcherUnpooledDataSourceFactory;
import com.luo.ibatis.logging.ArcherLogFactory;
import com.luo.ibatis.logging.Log;
import com.luo.ibatis.logging.log4j.ArcherLog4jImpl;
import com.luo.ibatis.mapping.ArcherEnvironment;
import com.luo.ibatis.mapping.ArcherMappedStatement;
import com.luo.ibatis.mapping.ArcherParameterMap;
import com.luo.ibatis.mapping.ArcherResultMap;
import com.luo.ibatis.parsing.XNode;
import com.luo.ibatis.reflection.DefaultReflectorFactory;
import com.luo.ibatis.reflection.MetaObject;
import com.luo.ibatis.reflection.ReflectorFactory;
import com.luo.ibatis.reflection.factory.DefaultObjectFactory;
import com.luo.ibatis.reflection.factory.ObjectFactory;
import com.luo.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.luo.ibatis.reflection.wrapper.ObjectWrapperFactory;
import com.luo.ibatis.scripting.ArcherLanguageDriver;
import com.luo.ibatis.scripting.defaults.ArcherRawLanguageDriver;
import com.luo.ibatis.scripting.xmltags.ArcherXMLLanguageDriver;
import com.luo.ibatis.scripting.LanguageDriverRegistry;
import com.luo.ibatis.transaction.jdbc.ArcherJdbcTransactionFactory;
import com.luo.ibatis.type.ArcherTypeHandlerRegistry;
import com.luo.ibatis.type.TypeAliasRegistry;

import java.util.*;

public class ArcherConfiguration {

    protected ArcherEnvironment environment;

    protected boolean useActualParamName = true;

    protected Class<? extends Log> logImpl;

    protected Properties variables = new Properties();
    protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected final ArcherMapperRegistry mapperRegistry = new ArcherMapperRegistry(this);
//    protected final InterceptorChain interceptorChain = new InterceptorChain();
    protected final ArcherTypeHandlerRegistry typeHandlerRegistry = new ArcherTypeHandlerRegistry();
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    protected final Map<String, ArcherResultMap> resultMaps = new StrictMap<ArcherResultMap>("Result Maps collection");
    protected final Map<String, ArcherMappedStatement> mappedStatements = new StrictMap<ArcherMappedStatement>("Mapped Statements collection");
//    protected final Map<String, Cache> caches = new StrictMap<Cache>("Caches collection");
    protected final Map<String, ArcherParameterMap> parameterMaps = new StrictMap<ArcherParameterMap>("Parameter Maps collection");


    protected final Set<String> loadedResources = new HashSet<String>();
    protected final Map<String, XNode> sqlFragments = new StrictMap<XNode>("XML fragments parsed from previous mappers");

    // 存放解析异常的XMLStatementBuilder对象
    protected final Collection<ArcherXMLStatementBuilder> incompleteStatements = new LinkedList<ArcherXMLStatementBuilder>();




    public ArcherConfiguration(ArcherEnvironment environment) {
        this();
        this.environment = environment;
    }


    public ArcherConfiguration() {
        typeAliasRegistry.registerAlias("JDBC", ArcherJdbcTransactionFactory.class);

//        typeAliasRegistry.registerAlias("POOLED", ArcherPooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", ArcherUnpooledDataSourceFactory.class);

//        typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
//        typeAliasRegistry.registerAlias("FIFO", FifoCache.class);
//        typeAliasRegistry.registerAlias("LRU", LruCache.class);
//        typeAliasRegistry.registerAlias("SOFT", SoftCache.class);
//        typeAliasRegistry.registerAlias("WEAK", WeakCache.class);

//        typeAliasRegistry.registerAlias("DB_VENDOR", VendorDatabaseIdProvider.class);

        typeAliasRegistry.registerAlias("XML", ArcherXMLLanguageDriver.class);
        typeAliasRegistry.registerAlias("RAW", ArcherRawLanguageDriver.class);

//        typeAliasRegistry.registerAlias("SLF4J", Slf4jImpl.class);
//        typeAliasRegistry.registerAlias("COMMONS_LOGGING", JakartaCommonsLoggingImpl.class);
        typeAliasRegistry.registerAlias("LOG4J", ArcherLog4jImpl.class);
//        typeAliasRegistry.registerAlias("LOG4J2", Log4j2Impl.class);
//        typeAliasRegistry.registerAlias("JDK_LOGGING", Jdk14LoggingImpl.class);
//        typeAliasRegistry.registerAlias("STDOUT_LOGGING", StdOutImpl.class);
//        typeAliasRegistry.registerAlias("NO_LOGGING", NoLoggingImpl.class);

//        typeAliasRegistry.registerAlias("CGLIB", CglibProxyFactory.class);
//        typeAliasRegistry.registerAlias("JAVASSIST", JavassistProxyFactory.class);

        languageRegistry.setDefaultDriverClass(ArcherXMLLanguageDriver.class);
        languageRegistry.register(ArcherRawLanguageDriver.class);
    }


    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public ArcherEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(ArcherEnvironment environment) {
        this.environment = environment;
    }

    public Class<? extends Log> getLogImpl() {
        return logImpl;
    }

    public void setLogImpl(Class<? extends Log> logImpl) {
        if (logImpl != null) {
            this.logImpl = logImpl;
            // 调用LogFactory类的useCustomLogging（）方法指定日志实现类
            ArcherLogFactory.useCustomLogging(this.logImpl);
        }
    }

    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    public Map<String, XNode> getSqlFragments() {
        return sqlFragments;
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }

    public Collection<ArcherXMLStatementBuilder> getIncompleteStatements() {
        return incompleteStatements;
    }

    public void addIncompleteStatement(ArcherXMLStatementBuilder incompleteStatement) {
        incompleteStatements.add(incompleteStatement);
    }
    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }

    public void setDefaultScriptingLanguage(Class<? extends ArcherLanguageDriver> driver) {
        if (driver == null) {
            driver = ArcherXMLLanguageDriver.class;
        }
        getLanguageRegistry().setDefaultDriverClass(driver);
    }

    public ArcherLanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }

    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    public ArcherTypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    public ReflectorFactory getReflectorFactory() {
        return reflectorFactory;
    }

    public boolean isUseActualParamName() {
        return useActualParamName;
    }

    public Collection<ArcherResultMap> getResultMaps() {
        return resultMaps.values();
    }

    public ArcherResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public void addMappedStatement(ArcherMappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public Collection<String> getParameterMapNames() {
        return parameterMaps.keySet();
    }

    public Collection<ArcherParameterMap> getParameterMaps() {
        return parameterMaps.values();
    }

    public ArcherParameterMap getParameterMap(String id) {
        return parameterMaps.get(id);
    }

    protected static class StrictMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -4950446264854982944L;
        private final String name;

        public StrictMap(String name, int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
            this.name = name;
        }

        public StrictMap(String name, int initialCapacity) {
            super(initialCapacity);
            this.name = name;
        }

        public StrictMap(String name) {
            super();
            this.name = name;
        }

        public StrictMap(String name, Map<String, ? extends V> m) {
            super(m);
            this.name = name;
        }

        @SuppressWarnings("unchecked")
        public V put(String key, V value) {
            if (containsKey(key)) {
                throw new IllegalArgumentException(name + " already contains value for " + key);
            }
            if (key.contains(".")) {
                final String shortKey = getShortName(key);
                if (super.get(shortKey) == null) {
                    super.put(shortKey, value);
                } else {
                    super.put(shortKey, (V) new Ambiguity(shortKey));
                }
            }
            return super.put(key, value);
        }

        public V get(Object key) {
            V value = super.get(key);
            if (value == null) {
                throw new IllegalArgumentException(name + " does not contain value for " + key);
            }
            if (value instanceof Ambiguity) {
                throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
                        + " (try using the full name including the namespace, or rename one of the entries)");
            }
            return value;
        }

        private String getShortName(String key) {
            final String[] keyParts = key.split("\\.");
            return keyParts[keyParts.length - 1];
        }

        protected static class Ambiguity {
            final private String subject;

            public Ambiguity(String subject) {
                this.subject = subject;
            }

            public String getSubject() {
                return subject;
            }
        }
    }
}

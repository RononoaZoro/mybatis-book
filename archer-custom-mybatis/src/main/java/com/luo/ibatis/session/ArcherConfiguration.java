package com.luo.ibatis.session;

import com.luo.ibatis.binding.ArcherMapperRegistry;
import com.luo.ibatis.builder.xml.ArcherXMLStatementBuilder;
import com.luo.ibatis.cache.decorators.ArcherLruCache;
import com.luo.ibatis.cache.impl.ArcherPerpetualCache;
import com.luo.ibatis.datasource.pooled.ArcherPooledDataSourceFactory;
import com.luo.ibatis.datasource.unpooled.ArcherUnpooledDataSourceFactory;
import com.luo.ibatis.executor.ArcherBatchExecutor;
import com.luo.ibatis.executor.ArcherExecutor;
import com.luo.ibatis.executor.ArcherReuseExecutor;
import com.luo.ibatis.executor.ArcherSimpleExecutor;
import com.luo.ibatis.executor.loader.ArcherProxyFactory;
import com.luo.ibatis.executor.loader.javassist.ArcherJavassistProxyFactory;
import com.luo.ibatis.executor.parameter.ArcherParameterHandler;
import com.luo.ibatis.executor.resultset.ArcherDefaultResultSetHandler;
import com.luo.ibatis.executor.resultset.ArcherResultSetHandler;
import com.luo.ibatis.executor.statement.ArcherRoutingStatementHandler;
import com.luo.ibatis.executor.statement.ArcherStatementHandler;
import com.luo.ibatis.logging.ArcherLogFactory;
import com.luo.ibatis.logging.Log;
import com.luo.ibatis.logging.log4j.ArcherLog4jImpl;
import com.luo.ibatis.mapping.*;
import com.luo.ibatis.parsing.XNode;
import com.luo.ibatis.reflection.DefaultReflectorFactory;
import com.luo.ibatis.reflection.MetaObject;
import com.luo.ibatis.reflection.ReflectorFactory;
import com.luo.ibatis.reflection.factory.DefaultObjectFactory;
import com.luo.ibatis.reflection.factory.ObjectFactory;
import com.luo.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.luo.ibatis.reflection.wrapper.ObjectWrapperFactory;
import com.luo.ibatis.scripting.ArcherLanguageDriver;
import com.luo.ibatis.scripting.LanguageDriverRegistry;
import com.luo.ibatis.scripting.defaults.ArcherRawLanguageDriver;
import com.luo.ibatis.scripting.xmltags.ArcherXMLLanguageDriver;
import com.luo.ibatis.transaction.ArcherTransaction;
import com.luo.ibatis.transaction.jdbc.ArcherJdbcTransactionFactory;
import com.luo.ibatis.type.ArcherTypeHandlerRegistry;
import com.luo.ibatis.type.JdbcType;
import com.luo.ibatis.type.TypeAliasRegistry;

import java.util.*;

public class ArcherConfiguration {

    protected ArcherEnvironment environment;

    protected boolean useActualParamName = true;
    protected boolean useColumnLabel = true;
    protected boolean safeRowBoundsEnabled;
    protected boolean safeResultHandlerEnabled = true;
    protected boolean mapUnderscoreToCamelCase;
    protected boolean aggressiveLazyLoading;
    protected boolean multipleResultSetsEnabled = true;
    protected boolean useGeneratedKeys;
    protected boolean cacheEnabled = true;
    protected boolean callSettersOnNulls;
    protected boolean returnInstanceForEmptyRow;

    protected Class<? extends Log> logImpl;
    protected ExecutorType defaultExecutorType = ExecutorType.SIMPLE;
    protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;
    protected JdbcType jdbcTypeForNull = JdbcType.OTHER;
    protected Integer defaultStatementTimeout;
    protected Integer defaultFetchSize;
    protected AutoMappingBehavior autoMappingBehavior = AutoMappingBehavior.PARTIAL;
    protected AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior = AutoMappingUnknownColumnBehavior.NONE;
    protected Set<String> lazyLoadTriggerMethods = new HashSet<String>(Arrays.asList(new String[]{"equals", "clone", "hashCode", "toString"}));


    protected Properties variables = new Properties();
    protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected boolean lazyLoadingEnabled = false;
    protected ArcherProxyFactory proxyFactory = new ArcherJavassistProxyFactory(); // #224 Using internal Javassist instead of OGNL

    protected Class<?> configurationFactory;

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

        typeAliasRegistry.registerAlias("POOLED", ArcherPooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", ArcherUnpooledDataSourceFactory.class);

        typeAliasRegistry.registerAlias("PERPETUAL", ArcherPerpetualCache.class);
//        typeAliasRegistry.registerAlias("FIFO", FifoCache.class);
        typeAliasRegistry.registerAlias("LRU", ArcherLruCache.class);
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
        typeAliasRegistry.registerAlias("JAVASSIST", ArcherJavassistProxyFactory.class);

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

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, ArcherSqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
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

    public boolean hasStatement(String statementName) {
        return hasStatement(statementName, true);
    }

    public boolean hasStatement(String statementName, boolean validateIncompleteStatements) {
        if (validateIncompleteStatements) {
            buildAllStatements();
        }
        return mappedStatements.containsKey(statementName);
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    public void setObjectWrapperFactory(ObjectWrapperFactory objectWrapperFactory) {
        this.objectWrapperFactory = objectWrapperFactory;
    }


    public Integer getDefaultStatementTimeout() {
        return defaultStatementTimeout;
    }

    public void setDefaultStatementTimeout(Integer defaultStatementTimeout) {
        this.defaultStatementTimeout = defaultStatementTimeout;
    }

    /**
     * @since 3.3.0
     */
    public Integer getDefaultFetchSize() {
        return defaultFetchSize;
    }

    /**
     * @since 3.3.0
     */
    public void setDefaultFetchSize(Integer defaultFetchSize) {
        this.defaultFetchSize = defaultFetchSize;
    }

    public boolean isSafeResultHandlerEnabled() {
        return safeResultHandlerEnabled;
    }

    public void setSafeResultHandlerEnabled(boolean safeResultHandlerEnabled) {
        this.safeResultHandlerEnabled = safeResultHandlerEnabled;
    }

    public boolean isSafeRowBoundsEnabled() {
        return safeRowBoundsEnabled;
    }

    public void setSafeRowBoundsEnabled(boolean safeRowBoundsEnabled) {
        this.safeRowBoundsEnabled = safeRowBoundsEnabled;
    }

    public boolean isMapUnderscoreToCamelCase() {
        return mapUnderscoreToCamelCase;
    }

    public boolean isCallSettersOnNulls() {
        return callSettersOnNulls;
    }

    public void setCallSettersOnNulls(boolean callSettersOnNulls) {
        this.callSettersOnNulls = callSettersOnNulls;
    }

    public void setUseActualParamName(boolean useActualParamName) {
        this.useActualParamName = useActualParamName;
    }

    public boolean isReturnInstanceForEmptyRow() {
        return returnInstanceForEmptyRow;
    }

    public void setReturnInstanceForEmptyRow(boolean returnEmptyInstance) {
        this.returnInstanceForEmptyRow = returnEmptyInstance;
    }

    public AutoMappingBehavior getAutoMappingBehavior() {
        return autoMappingBehavior;
    }

    public void setAutoMappingBehavior(AutoMappingBehavior autoMappingBehavior) {
        this.autoMappingBehavior = autoMappingBehavior;
    }

    /**
     * @since 3.4.0
     */
    public AutoMappingUnknownColumnBehavior getAutoMappingUnknownColumnBehavior() {
        return autoMappingUnknownColumnBehavior;
    }

    /**
     * @since 3.4.0
     */
    public void setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior) {
        this.autoMappingUnknownColumnBehavior = autoMappingUnknownColumnBehavior;
    }

    public boolean isLazyLoadingEnabled() {
        return lazyLoadingEnabled;
    }

    public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
        this.lazyLoadingEnabled = lazyLoadingEnabled;
    }

    public ArcherProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public void setProxyFactory(ArcherProxyFactory proxyFactory) {
        if (proxyFactory == null) {
            proxyFactory = new ArcherJavassistProxyFactory();
        }
        this.proxyFactory = proxyFactory;
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

    public ArcherMappedStatement getMappedStatement(String id) {
        return this.getMappedStatement(id, true);
    }

    public ArcherMappedStatement getMappedStatement(String id, boolean validateIncompleteStatements) {
        if (validateIncompleteStatements) {
            buildAllStatements();
        }
        return mappedStatements.get(id);
    }

    public ExecutorType getDefaultExecutorType() {
        return defaultExecutorType;
    }

    public LocalCacheScope getLocalCacheScope() {
        return localCacheScope;
    }

    public void setLocalCacheScope(LocalCacheScope localCacheScope) {
        this.localCacheScope = localCacheScope;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public JdbcType getJdbcTypeForNull() {
        return jdbcTypeForNull;
    }

    public boolean isUseColumnLabel() {
        return useColumnLabel;
    }

    public void setUseColumnLabel(boolean useColumnLabel) {
        this.useColumnLabel = useColumnLabel;
    }

    public boolean isAggressiveLazyLoading() {
        return aggressiveLazyLoading;
    }

    public void setAggressiveLazyLoading(boolean aggressiveLazyLoading) {
        this.aggressiveLazyLoading = aggressiveLazyLoading;
    }

    public void setLazyLoadTriggerMethods(Set<String> lazyLoadTriggerMethods) {
        this.lazyLoadTriggerMethods = lazyLoadTriggerMethods;
    }

    public Set<String> getLazyLoadTriggerMethods() {
        return lazyLoadTriggerMethods;
    }

    public Class<?> getConfigurationFactory() {
        return configurationFactory;
    }

    public void setConfigurationFactory(Class<?> configurationFactory) {
        this.configurationFactory = configurationFactory;
    }

    public boolean hasResultMap(String id) {
        return resultMaps.containsKey(id);
    }


    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    public ArcherParameterHandler newParameterHandler(ArcherMappedStatement mappedStatement, Object parameterObject, ArcherBoundSql boundSql) {
        ArcherParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        // 执行拦截器链的拦截逻辑
//        parameterHandler = (ArcherParameterHandler) interceptorChain.pluginAll(parameterHandler);
        return parameterHandler;
    }

    public ArcherResultSetHandler newResultSetHandler(ArcherExecutor executor, ArcherMappedStatement mappedStatement, ArcherRowBounds rowBounds, ArcherParameterHandler parameterHandler,
                                                      ArcherResultHandler resultHandler, ArcherBoundSql boundSql) {
        ArcherResultSetHandler resultSetHandler = new ArcherDefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
        // 执行拦截器链的拦截逻辑
//        resultSetHandler = (ArcherResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
        return resultSetHandler;
    }

    public ArcherStatementHandler newStatementHandler(ArcherExecutor executor, ArcherMappedStatement mappedStatement, Object parameterObject, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler, ArcherBoundSql boundSql) {
        ArcherStatementHandler statementHandler = new ArcherRoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
        // 执行拦截器链的拦截逻辑
//        statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
        return statementHandler;
    }

    public ArcherExecutor newExecutor(ArcherTransaction transaction) {
        return newExecutor(transaction, defaultExecutorType);
    }

    public ArcherExecutor newExecutor(ArcherTransaction transaction, ExecutorType executorType) {
        executorType = executorType == null ? defaultExecutorType : executorType;
        executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
        ArcherExecutor executor;
        // 根据executor类型创建对象的Executor对象
        if (ExecutorType.BATCH == executorType) {
            executor = new ArcherBatchExecutor(this, transaction);
        } else if (ExecutorType.REUSE == executorType) {
            executor = new ArcherReuseExecutor(this, transaction);
        } else {
            executor = new ArcherSimpleExecutor(this, transaction);
        }
        // 如果cacheEnabled属性为ture，这使用CachingExecutor对上面创建的Executor进行装饰
//        if (cacheEnabled) {
//            executor = new CachingExecutor(executor);
//        }
        // 执行拦截器链的拦截逻辑
//        executor = (ArcherExecutor) interceptorChain.pluginAll(executor);
        return executor;
    }


    /*
     * Parses all the unprocessed statement nodes in the cache. It is recommended
     * to call this method once all the mappers are added as it provides fail-fast
     * statement validation.
     */
    protected void buildAllStatements() {
//        if (!incompleteResultMaps.isEmpty()) {
//            synchronized (incompleteResultMaps) {
//                // This always throws a BuilderException.
//                incompleteResultMaps.iterator().next().resolve();
//            }
//        }
//        if (!incompleteCacheRefs.isEmpty()) {
//            synchronized (incompleteCacheRefs) {
//                // This always throws a BuilderException.
//                incompleteCacheRefs.iterator().next().resolveCacheRef();
//            }
//        }
        if (!incompleteStatements.isEmpty()) {
            synchronized (incompleteStatements) {
                // This always throws a BuilderException.
                incompleteStatements.iterator().next().parseStatementNode();
            }
        }
//        if (!incompleteMethods.isEmpty()) {
//            synchronized (incompleteMethods) {
//                // This always throws a BuilderException.
//                incompleteMethods.iterator().next().resolve();
//            }
//        }
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

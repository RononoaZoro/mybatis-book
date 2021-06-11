/**
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.luo.ibatis.executor;


import com.luo.ibatis.cache.CacheKey;
import com.luo.ibatis.cache.impl.ArcherPerpetualCache;
import com.luo.ibatis.cursor.Cursor;
import com.luo.ibatis.executor.statement.StatementUtil;
import com.luo.ibatis.logging.ArcherLogFactory;
import com.luo.ibatis.logging.Log;
import com.luo.ibatis.logging.jdbc.ConnectionLogger;
import com.luo.ibatis.mapping.*;
import com.luo.ibatis.reflection.MetaObject;
import com.luo.ibatis.reflection.factory.ObjectFactory;
import com.luo.ibatis.session.ArcherConfiguration;
import com.luo.ibatis.session.ArcherResultHandler;
import com.luo.ibatis.session.ArcherRowBounds;
import com.luo.ibatis.session.LocalCacheScope;
import com.luo.ibatis.transaction.ArcherTransaction;
import com.luo.ibatis.type.ArcherTypeHandlerRegistry;

import javax.security.auth.login.Configuration;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.luo.ibatis.executor.ExecutionPlaceholder.EXECUTION_PLACEHOLDER;

/**
 * @author Clinton Begin
 */
public abstract class ArcherBaseExecutor implements ArcherExecutor {

    private static final Log log = ArcherLogFactory.getLog(ArcherBaseExecutor.class);

    protected ArcherTransaction transaction;
    protected ArcherExecutor wrapper;

    protected ConcurrentLinkedQueue<DeferredLoad> deferredLoads;
    // Mybatis一级缓存对象
    protected ArcherPerpetualCache localCache;
    // 存储过程输出参数缓存
    protected ArcherPerpetualCache localOutputParameterCache;
    protected ArcherConfiguration configuration;

    protected int queryStack;
    private boolean closed;

    protected ArcherBaseExecutor(ArcherConfiguration configuration, ArcherTransaction transaction) {
        this.transaction = transaction;
        this.deferredLoads = new ConcurrentLinkedQueue<DeferredLoad>();
        this.localCache = new ArcherPerpetualCache("LocalCache");
        this.localOutputParameterCache = new ArcherPerpetualCache("LocalOutputParameterCache");
        this.closed = false;
        this.configuration = configuration;
        this.wrapper = this;
    }

    @Override
    public ArcherTransaction getTransaction() {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        return transaction;
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            try {
                rollback(forceRollback);
            } finally {
                if (transaction != null) {
                    transaction.close();
                }
            }
        } catch (SQLException e) {
            // Ignore.  There's nothing that can be done at this point.
            log.warn("Unexpected exception on closing transaction.  Cause: " + e);
        } finally {
            transaction = null;
            deferredLoads = null;
            localCache = null;
            localOutputParameterCache = null;
            closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public int update(ArcherMappedStatement ms, Object parameter) throws SQLException {
        ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        clearLocalCache();
        return doUpdate(ms, parameter);
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return flushStatements(false);
    }

    public List<BatchResult> flushStatements(boolean isRollBack) throws SQLException {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        return doFlushStatements(isRollBack);
    }

    @Override
    public <E> List<E> query(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler) throws SQLException {
        // 获取BoundSql对象，BoundSql是对动态SQL解析生成的SQL语句和参数映射信息从VB你，。、vcxz]\asf346的封装
        ArcherBoundSql boundSql = ms.getBoundSql(parameter);
        // 创建CacheKey，用于缓存Key
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
        // 调用重载的query（）方法
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> List<E> query(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler, CacheKey key, ArcherBoundSql boundSql) throws SQLException {
        ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        if (queryStack == 0 && ms.isFlushCacheRequired()) {
            clearLocalCache();
        }
        List<E> list;
        try {
            queryStack++;
            // 从缓存中获取结果
            list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
            if (list != null) {
                handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
            } else {
                // 缓存中获取不到，则调用queryFromDatabase（）方法从数据库中查询
                list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {
            queryStack--;
        }
        if (queryStack == 0) {
            for (DeferredLoad deferredLoad : deferredLoads) {
                deferredLoad.load();
            }
            // issue #601
            deferredLoads.clear();
            if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
                // issue #482
                clearLocalCache();
            }
        }
        return list;
    }

    @Override
    public <E> Cursor<E> queryCursor(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds) throws SQLException {
        ArcherBoundSql boundSql = ms.getBoundSql(parameter);
        return doQueryCursor(ms, parameter, rowBounds, boundSql);
    }

    @Override
    public void deferLoad(ArcherMappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        DeferredLoad deferredLoad = new DeferredLoad(resultObject, property, key, localCache, configuration, targetType);
        if (deferredLoad.canLoad()) {
            deferredLoad.load();
        } else {
            deferredLoads.add(new DeferredLoad(resultObject, property, key, localCache, configuration, targetType));
        }
    }

    @Override
    public CacheKey createCacheKey(ArcherMappedStatement ms, Object parameterObject, ArcherRowBounds rowBounds, ArcherBoundSql boundSql) {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(ms.getId()); // Mapper Id
        cacheKey.update(rowBounds.getOffset()); // 偏移量
        cacheKey.update(rowBounds.getLimit()); // 条数
        cacheKey.update(boundSql.getSql()); // SQL语句
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        ArcherTypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
        // 所有参数值
        for (ParameterMapping parameterMapping : parameterMappings) {
            if (parameterMapping.getMode() != ParameterMode.OUT) {
                Object value;
                String propertyName = parameterMapping.getProperty();
                if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                cacheKey.update(value);
            }
        }
        // Environment Id
        if (configuration.getEnvironment() != null) {
            cacheKey.update(configuration.getEnvironment().getId());
        }
        return cacheKey;
    }

    @Override
    public boolean isCached(ArcherMappedStatement ms, CacheKey key) {
        return localCache.getObject(key) != null;
    }

    @Override
    public void commit(boolean required) throws SQLException {
        if (closed) {
            throw new ExecutorException("Cannot commit, transaction is already closed");
        }
        clearLocalCache();
        flushStatements();
        if (required) {
            transaction.commit();
        }
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        if (!closed) {
            try {
                clearLocalCache();
                flushStatements(true);
            } finally {
                if (required) {
                    transaction.rollback();
                }
            }
        }
    }

    @Override
    public void clearLocalCache() {
        if (!closed) {
            localCache.clear();
            localOutputParameterCache.clear();
        }
    }

    protected abstract int doUpdate(ArcherMappedStatement ms, Object parameter)
            throws SQLException;

    protected abstract List<BatchResult> doFlushStatements(boolean isRollback)
            throws SQLException;

    protected abstract <E> List<E> doQuery(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler, ArcherBoundSql boundSql)
            throws SQLException;


    protected abstract <E> Cursor<E> doQueryCursor(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherBoundSql boundSql)
            throws SQLException;

    protected void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                if (!statement.isClosed()) {
                    statement.close();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Apply a transaction timeout.
     *
     * @param statement a current statement
     * @throws SQLException if a database access error occurs, this method is called on a closed <code>Statement</code>
     * @see StatementUtil#applyTransactionTimeout(Statement, Integer, Integer)
     * @since 3.4.0
     */
    protected void applyTransactionTimeout(Statement statement) throws SQLException {
        StatementUtil.applyTransactionTimeout(statement, statement.getQueryTimeout(), transaction.getTimeout());
    }

    private void handleLocallyCachedOutputParameters(ArcherMappedStatement ms, CacheKey key, Object parameter, ArcherBoundSql boundSql) {
        if (ms.getStatementType() == StatementType.CALLABLE) {
            final Object cachedParameter = localOutputParameterCache.getObject(key);
            if (cachedParameter != null && parameter != null) {
                final MetaObject metaCachedParameter = configuration.newMetaObject(cachedParameter);
                final MetaObject metaParameter = configuration.newMetaObject(parameter);
                for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                    if (parameterMapping.getMode() != ParameterMode.IN) {
                        final String parameterName = parameterMapping.getProperty();
                        final Object cachedValue = metaCachedParameter.getValue(parameterName);
                        metaParameter.setValue(parameterName, cachedValue);
                    }
                }
            }
        }
    }

    private <E> List<E> queryFromDatabase(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler, CacheKey key, ArcherBoundSql boundSql) throws SQLException {
        List<E> list;
        localCache.putObject(key, EXECUTION_PLACEHOLDER);
        try {
            // 调用doQuery（）方法查询
            list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
        } finally {
            localCache.removeObject(key);
        }
        // 缓存查询结果
        localCache.putObject(key, list);
        if (ms.getStatementType() == StatementType.CALLABLE) {
            localOutputParameterCache.putObject(key, parameter);
        }
        return list;
    }

    protected Connection getConnection(Log statementLog) throws SQLException {
        Connection connection = transaction.getConnection();
        if (statementLog.isDebugEnabled()) {
            return ConnectionLogger.newInstance(connection, statementLog, queryStack);
        } else {
            return connection;
        }
    }

    @Override
    public void setExecutorWrapper(ArcherExecutor wrapper) {
        this.wrapper = wrapper;
    }

    private static class DeferredLoad {

        private final MetaObject resultObject;
        private final String property;
        private final Class<?> targetType;
        private final CacheKey key;
        private final ArcherPerpetualCache localCache;
        private final ObjectFactory objectFactory;
        private final ArcherResultExtractor resultExtractor;

        // issue #781
        public DeferredLoad(MetaObject resultObject,
                            String property,
                            CacheKey key,
                            ArcherPerpetualCache localCache,
                            ArcherConfiguration configuration,
                            Class<?> targetType) {
            this.resultObject = resultObject;
            this.property = property;
            this.key = key;
            this.localCache = localCache;
            this.objectFactory = configuration.getObjectFactory();
            this.resultExtractor = new ArcherResultExtractor(configuration, objectFactory);
            this.targetType = targetType;
        }

        public boolean canLoad() {
            return localCache.getObject(key) != null && localCache.getObject(key) != EXECUTION_PLACEHOLDER;
        }

        public void load() {
            @SuppressWarnings("unchecked")
            // we suppose we get back a List
            List<Object> list = (List<Object>) localCache.getObject(key);
            Object value = resultExtractor.extractObjectFromList(list, targetType);
            resultObject.setValue(property, value);
        }

    }

}

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
package com.luo.ibatis.executor.loader;


import com.luo.ibatis.cursor.Cursor;
import com.luo.ibatis.executor.ArcherBaseExecutor;
import com.luo.ibatis.executor.BatchResult;
import com.luo.ibatis.executor.ExecutorException;
import com.luo.ibatis.logging.ArcherLogFactory;
import com.luo.ibatis.logging.Log;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherMappedStatement;
import com.luo.ibatis.reflection.MetaObject;
import com.luo.ibatis.session.ArcherConfiguration;
import com.luo.ibatis.session.ArcherResultHandler;
import com.luo.ibatis.session.ArcherRowBounds;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Clinton Begin
 * @author Franta Mejta
 */
public class ArcherResultLoaderMap {

    private final Map<String, LoadPair> loaderMap = new HashMap<String, LoadPair>();

    public void addLoader(String property, MetaObject metaResultObject, ArcherResultLoader resultLoader) {
        String upperFirst = getUppercaseFirstProperty(property);
        if (!upperFirst.equalsIgnoreCase(property) && loaderMap.containsKey(upperFirst)) {
            throw new ExecutorException("Nested lazy loaded result property '" + property +
                    "' for query id '" + resultLoader.mappedStatement.getId() +
                    " already exists in the result map. The leftmost property of all lazy loaded properties must be unique within a result map.");
        }
        loaderMap.put(upperFirst, new LoadPair(property, metaResultObject, resultLoader));
    }

    public final Map<String, LoadPair> getProperties() {
        return new HashMap<String, LoadPair>(this.loaderMap);
    }

    public Set<String> getPropertyNames() {
        return loaderMap.keySet();
    }

    public int size() {
        return loaderMap.size();
    }

    public boolean hasLoader(String property) {
        return loaderMap.containsKey(property.toUpperCase(Locale.ENGLISH));
    }

    public boolean load(String property) throws SQLException {
        LoadPair pair = loaderMap.remove(property.toUpperCase(Locale.ENGLISH));
        if (pair != null) {
            pair.load();
            return true;
        }
        return false;
    }

    public void remove(String property) {
        loaderMap.remove(property.toUpperCase(Locale.ENGLISH));
    }

    public void loadAll() throws SQLException {
        final Set<String> methodNameSet = loaderMap.keySet();
        String[] methodNames = methodNameSet.toArray(new String[methodNameSet.size()]);
        for (String methodName : methodNames) {
            load(methodName);
        }
    }

    private static String getUppercaseFirstProperty(String property) {
        String[] parts = property.split("\\.");
        return parts[0].toUpperCase(Locale.ENGLISH);
    }

    /**
     * Property which was not loaded yet.
     */
    public static class LoadPair implements Serializable {

        private static final long serialVersionUID = 20130412;
        /**
         * Name of factory method which returns database connection.
         */
        private static final String FACTORY_METHOD = "getConfiguration";
        /**
         * Object to check whether we went through serialization..
         */
        private final transient Object serializationCheck = new Object();
        /**
         * Meta object which sets loaded properties.
         */
        private transient MetaObject metaResultObject;
        /**
         * Result loader which loads unread properties.
         */
        private transient ArcherResultLoader resultLoader;
        /**
         * Wow, logger.
         */
        private transient Log log;
        /**
         * Factory class through which we get database connection.
         */
        private Class<?> configurationFactory;
        /**
         * Name of the unread property.
         */
        private String property;
        /**
         * ID of SQL statement which loads the property.
         */
        private String mappedStatement;
        /**
         * Parameter of the sql statement.
         */
        private Serializable mappedParameter;

        private LoadPair(final String property, MetaObject metaResultObject, ArcherResultLoader resultLoader) {
            this.property = property;
            this.metaResultObject = metaResultObject;
            this.resultLoader = resultLoader;

            /* Save required information only if original object can be serialized. */
            if (metaResultObject != null && metaResultObject.getOriginalObject() instanceof Serializable) {
                final Object mappedStatementParameter = resultLoader.parameterObject;

                /* @todo May the parameter be null? */
                if (mappedStatementParameter instanceof Serializable) {
                    this.mappedStatement = resultLoader.mappedStatement.getId();
                    this.mappedParameter = (Serializable) mappedStatementParameter;

                    this.configurationFactory = resultLoader.configuration.getConfigurationFactory();
                } else {
                    Log log = this.getLogger();
                    if (log.isDebugEnabled()) {
                        log.debug("Property [" + this.property + "] of ["
                                + metaResultObject.getOriginalObject().getClass() + "] cannot be loaded "
                                + "after deserialization. Make sure it's loaded before serializing "
                                + "forenamed object.");
                    }
                }
            }
        }

        public void load() throws SQLException {
            /* These field should not be null unless the loadpair was serialized.
             * Yet in that case this method should not be called. */
            if (this.metaResultObject == null) {
                throw new IllegalArgumentException("metaResultObject is null");
            }
            if (this.resultLoader == null) {
                throw new IllegalArgumentException("resultLoader is null");
            }

            this.load(null);
        }

        public void load(final Object userObject) throws SQLException {
            if (this.metaResultObject == null || this.resultLoader == null) {
                if (this.mappedParameter == null) {
                    throw new ExecutorException("Property [" + this.property + "] cannot be loaded because "
                            + "required parameter of mapped statement ["
                            + this.mappedStatement + "] is not serializable.");
                }

                final ArcherConfiguration config = this.getConfiguration();
                final ArcherMappedStatement ms = config.getMappedStatement(this.mappedStatement);
                if (ms == null) {
                    throw new ExecutorException("Cannot lazy load property [" + this.property
                            + "] of deserialized object [" + userObject.getClass()
                            + "] because configuration does not contain statement ["
                            + this.mappedStatement + "]");
                }

                this.metaResultObject = config.newMetaObject(userObject);
                this.resultLoader = new ArcherResultLoader(config, new ClosedExecutor(), ms, this.mappedParameter,
                        metaResultObject.getSetterType(this.property), null, null);
            }

            /* We are using a new executor because we may be (and likely are) on a new thread
             * and executors aren't thread safe. (Is this sufficient?)
             *
             * A better approach would be making executors thread safe. */
            if (this.serializationCheck == null) {
                final ArcherResultLoader old = this.resultLoader;
                this.resultLoader = new ArcherResultLoader(old.configuration, new ClosedExecutor(), old.mappedStatement,
                        old.parameterObject, old.targetType, old.cacheKey, old.boundSql);
            }

            this.metaResultObject.setValue(property, this.resultLoader.loadResult());
        }

        private ArcherConfiguration getConfiguration() {
            if (this.configurationFactory == null) {
                throw new ExecutorException("Cannot get Configuration as configuration factory was not set.");
            }

            Object configurationObject = null;
            try {
                final Method factoryMethod = this.configurationFactory.getDeclaredMethod(FACTORY_METHOD);
                if (!Modifier.isStatic(factoryMethod.getModifiers())) {
                    throw new ExecutorException("Cannot get Configuration as factory method ["
                            + this.configurationFactory + "]#["
                            + FACTORY_METHOD + "] is not static.");
                }

                if (!factoryMethod.isAccessible()) {
                    configurationObject = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                        @Override
                        public Object run() throws Exception {
                            try {
                                factoryMethod.setAccessible(true);
                                return factoryMethod.invoke(null);
                            } finally {
                                factoryMethod.setAccessible(false);
                            }
                        }
                    });
                } else {
                    configurationObject = factoryMethod.invoke(null);
                }
            } catch (final ExecutorException ex) {
                throw ex;
            } catch (final NoSuchMethodException ex) {
                throw new ExecutorException("Cannot get Configuration as factory class ["
                        + this.configurationFactory + "] is missing factory method of name ["
                        + FACTORY_METHOD + "].", ex);
            } catch (final PrivilegedActionException ex) {
                throw new ExecutorException("Cannot get Configuration as factory method ["
                        + this.configurationFactory + "]#["
                        + FACTORY_METHOD + "] threw an exception.", ex.getCause());
            } catch (final Exception ex) {
                throw new ExecutorException("Cannot get Configuration as factory method ["
                        + this.configurationFactory + "]#["
                        + FACTORY_METHOD + "] threw an exception.", ex);
            }

            if (!(configurationObject instanceof ArcherConfiguration)) {
                throw new ExecutorException("Cannot get Configuration as factory method ["
                        + this.configurationFactory + "]#["
                        + FACTORY_METHOD + "] didn't return [" + ArcherConfiguration.class + "] but ["
                        + (configurationObject == null ? "null" : configurationObject.getClass()) + "].");
            }

            return ArcherConfiguration.class.cast(configurationObject);
        }

        private Log getLogger() {
            if (this.log == null) {
                this.log = ArcherLogFactory.getLog(this.getClass());
            }
            return this.log;
        }
    }

    private static final class ClosedExecutor extends ArcherBaseExecutor {

        public ClosedExecutor() {
            super(null, null);
        }

        @Override
        public boolean isClosed() {
            return true;
        }

        @Override
        protected int doUpdate(ArcherMappedStatement ms, Object parameter) throws SQLException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected <E> List<E> doQuery(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler, ArcherBoundSql boundSql) throws SQLException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected <E> Cursor<E> doQueryCursor(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherBoundSql boundSql) throws SQLException {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}

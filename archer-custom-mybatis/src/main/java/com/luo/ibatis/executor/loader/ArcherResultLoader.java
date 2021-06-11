/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.luo.ibatis.executor.loader;


import com.luo.ibatis.cache.CacheKey;
import com.luo.ibatis.executor.ArcherExecutor;
import com.luo.ibatis.executor.ArcherResultExtractor;
import com.luo.ibatis.executor.ExecutorException;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherEnvironment;
import com.luo.ibatis.mapping.ArcherMappedStatement;
import com.luo.ibatis.reflection.factory.ObjectFactory;
import com.luo.ibatis.session.ArcherConfiguration;
import com.luo.ibatis.session.ArcherRowBounds;
import com.luo.ibatis.session.ExecutorType;
import com.luo.ibatis.transaction.ArcherTransaction;
import com.luo.ibatis.transaction.ArcherTransactionFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class ArcherResultLoader {

  protected final ArcherConfiguration configuration;
  protected final ArcherExecutor executor;
  protected final ArcherMappedStatement mappedStatement;
  protected final Object parameterObject;
  protected final Class<?> targetType;
  protected final ObjectFactory objectFactory;
  protected final CacheKey cacheKey;
  protected final ArcherBoundSql boundSql;
  protected final ArcherResultExtractor resultExtractor;
  protected final long creatorThreadId;

  protected boolean loaded;
  protected Object resultObject;

  public ArcherResultLoader(ArcherConfiguration config, ArcherExecutor executor, ArcherMappedStatement mappedStatement, Object parameterObject, Class<?> targetType, CacheKey cacheKey, ArcherBoundSql boundSql) {
    this.configuration = config;
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.parameterObject = parameterObject;
    this.targetType = targetType;
    this.objectFactory = configuration.getObjectFactory();
    this.cacheKey = cacheKey;
    this.boundSql = boundSql;
    this.resultExtractor = new ArcherResultExtractor(configuration, objectFactory);
    this.creatorThreadId = Thread.currentThread().getId();
  }

  public Object loadResult() throws SQLException {
    List<Object> list = selectList();
    resultObject = resultExtractor.extractObjectFromList(list, targetType);
    return resultObject;
  }

  private <E> List<E> selectList() throws SQLException {
    ArcherExecutor localExecutor = executor;
    if (Thread.currentThread().getId() != this.creatorThreadId || localExecutor.isClosed()) {
      localExecutor = newExecutor();
    }
    try {
      return localExecutor.<E> query(mappedStatement, parameterObject, ArcherRowBounds.DEFAULT, ArcherExecutor.NO_RESULT_HANDLER, cacheKey, boundSql);
    } finally {
      if (localExecutor != executor) {
        localExecutor.close(false);
      }
    }
  }

  private ArcherExecutor newExecutor() {
    final ArcherEnvironment environment = configuration.getEnvironment();
    if (environment == null) {
      throw new ExecutorException("ResultLoader could not load lazily.  Environment was not configured.");
    }
    final DataSource ds = environment.getDataSource();
    if (ds == null) {
      throw new ExecutorException("ResultLoader could not load lazily.  DataSource was not configured.");
    }
    final ArcherTransactionFactory transactionFactory = environment.getTransactionFactory();
    final ArcherTransaction tx = transactionFactory.newTransaction(ds, null, false);
    return configuration.newExecutor(tx, ExecutorType.SIMPLE);
  }

  public boolean wasNull() {
    return resultObject == null;
  }

}

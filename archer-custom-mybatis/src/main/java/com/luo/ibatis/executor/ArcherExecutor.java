/**
 * Copyright 2009-2015 the original author or authors.
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
import com.luo.ibatis.cursor.Cursor;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherMappedStatement;
import com.luo.ibatis.reflection.MetaObject;
import com.luo.ibatis.session.ArcherResultHandler;
import com.luo.ibatis.session.ArcherRowBounds;
import com.luo.ibatis.transaction.ArcherTransaction;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Clinton Begin
 */
public interface ArcherExecutor {

    ArcherResultHandler NO_RESULT_HANDLER = null;

    int update(ArcherMappedStatement ms, Object parameter) throws SQLException;

    <E> List<E> query(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler, CacheKey cacheKey, ArcherBoundSql boundSql) throws SQLException;

    <E> List<E> query(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler) throws SQLException;

    <E> Cursor<E> queryCursor(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds) throws SQLException;

    List<BatchResult> flushStatements() throws SQLException;

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    CacheKey createCacheKey(ArcherMappedStatement ms, Object parameterObject, ArcherRowBounds rowBounds, ArcherBoundSql boundSql);

    boolean isCached(ArcherMappedStatement ms, CacheKey key);

    void clearLocalCache();

    void deferLoad(ArcherMappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

    ArcherTransaction getTransaction();

    void close(boolean forceRollback);

    boolean isClosed();

    void setExecutorWrapper(ArcherExecutor executor);

}

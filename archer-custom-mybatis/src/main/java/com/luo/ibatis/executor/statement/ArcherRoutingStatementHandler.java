/**
 * Copyright 2009-2016 the original author or authors.
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
package com.luo.ibatis.executor.statement;

import com.luo.ibatis.cursor.Cursor;
import com.luo.ibatis.executor.ArcherExecutor;
import com.luo.ibatis.executor.ExecutorException;
import com.luo.ibatis.executor.parameter.ArcherParameterHandler;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherMappedStatement;
import com.luo.ibatis.session.ArcherResultHandler;
import com.luo.ibatis.session.ArcherRowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class ArcherRoutingStatementHandler implements ArcherStatementHandler {

    private final ArcherStatementHandler delegate;

    public ArcherRoutingStatementHandler(ArcherExecutor executor, ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler, ArcherBoundSql boundSql) {

        switch (ms.getStatementType()) {
//            case STATEMENT:
//                delegate = new ArcherSimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
//                break;
            case PREPARED:
                delegate = new ArcherPreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                break;
//            case CALLABLE:
//                delegate = new ArcherCallableStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
//                break;
            default:
                throw new ExecutorException("Unknown statement type: " + ms.getStatementType());
        }

    }

    @Override
    public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {
        return delegate.prepare(connection, transactionTimeout);
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        delegate.parameterize(statement);
    }


    @Override
    public void batch(Statement statement) throws SQLException {
        delegate.batch(statement);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        return delegate.update(statement);
    }

    @Override
    public <E> List<E> query(Statement statement, ArcherResultHandler resultHandler) throws SQLException {
        return delegate.<E>query(statement, resultHandler);
    }

    @Override
    public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
        return delegate.queryCursor(statement);
    }

    @Override
    public ArcherBoundSql getBoundSql() {
        return delegate.getBoundSql();
    }

    @Override
    public ArcherParameterHandler getParameterHandler() {
        return delegate.getParameterHandler();
    }
}

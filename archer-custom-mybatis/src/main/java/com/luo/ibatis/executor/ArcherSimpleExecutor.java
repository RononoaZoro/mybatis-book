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
package com.luo.ibatis.executor;

import com.luo.ibatis.cursor.Cursor;
import com.luo.ibatis.logging.Log;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherMappedStatement;
import com.luo.ibatis.session.ArcherConfiguration;
import com.luo.ibatis.session.ArcherResultHandler;
import com.luo.ibatis.session.ArcherRowBounds;
import com.luo.ibatis.executor.statement.ArcherStatementHandler;
import com.luo.ibatis.transaction.ArcherTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class ArcherSimpleExecutor extends ArcherBaseExecutor {

    public ArcherSimpleExecutor(ArcherConfiguration configuration, ArcherTransaction transaction) {
        super(configuration, transaction);
    }

    @Override
    public int doUpdate(ArcherMappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            ArcherConfiguration configuration = ms.getConfiguration();
            ArcherStatementHandler handler = configuration.newStatementHandler(this, ms, parameter, ArcherRowBounds.DEFAULT, null, null);
            stmt = prepareStatement(handler, ms.getStatementLog());
            return handler.update(stmt);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    public <E> List<E> doQuery(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler, ArcherBoundSql boundSql) throws SQLException {
        Statement stmt = null;
        try {
            ArcherConfiguration configuration = ms.getConfiguration();
            // 获取StatementHandler对象
            ArcherStatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
            // 调用prepareStatement（）方法,创建Statement对象，并进行设置参数等操作
            stmt = prepareStatement(handler, ms.getStatementLog());
            // 调用StatementHandler对象的query（）方法执行查询操作
            return handler.<E>query(stmt, resultHandler);
        } finally {
            closeStatement(stmt);
        }
    }


    @Override
    protected <E> Cursor<E> doQueryCursor(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherBoundSql boundSql) throws SQLException {
        ArcherConfiguration configuration = ms.getConfiguration();
        ArcherStatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
        Statement stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.<E>queryCursor(stmt);
    }

    @Override
    public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
        return Collections.emptyList();
    }

    private Statement prepareStatement(ArcherStatementHandler handler, Log statementLog) throws SQLException {
        Statement stmt;
        // 获取JDBC中的Connection对象
        Connection connection = getConnection(statementLog);
        // 调用StatementHandler的prepare（）方法创建Statement对象
        stmt = handler.prepare(connection, transaction.getTimeout());
        // 调用StatementHandler对象的parameterize（）方法设置参数
        handler.parameterize(stmt);
        return stmt;
    }

}

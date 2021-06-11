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
import com.luo.ibatis.executor.statement.ArcherStatementHandler;
import com.luo.ibatis.logging.Log;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherMappedStatement;
import com.luo.ibatis.session.ArcherConfiguration;
import com.luo.ibatis.session.ArcherResultHandler;
import com.luo.ibatis.session.ArcherRowBounds;
import com.luo.ibatis.transaction.ArcherTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Clinton Begin
 */
public class ArcherReuseExecutor extends ArcherBaseExecutor {

    private final Map<String, Statement> statementMap = new HashMap<String, Statement>();

    public ArcherReuseExecutor(ArcherConfiguration configuration, ArcherTransaction transaction) {
        super(configuration, transaction);
    }

    @Override
    public int doUpdate(ArcherMappedStatement ms, Object parameter) throws SQLException {
        ArcherConfiguration configuration = ms.getConfiguration();
        ArcherStatementHandler handler = configuration.newStatementHandler(this, ms, parameter, ArcherRowBounds.DEFAULT, null, null);
        Statement stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.update(stmt);
    }

    @Override
    public <E> List<E> doQuery(ArcherMappedStatement ms, Object parameter, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler, ArcherBoundSql boundSql) throws SQLException {
        ArcherConfiguration configuration = ms.getConfiguration();
        ArcherStatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
        Statement stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.<E>query(stmt, resultHandler);
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
        for (Statement stmt : statementMap.values()) {
            closeStatement(stmt);
        }
        statementMap.clear();
        return Collections.emptyList();
    }

    private Statement prepareStatement(ArcherStatementHandler handler, Log statementLog) throws SQLException {
        Statement stmt;
        ArcherBoundSql boundSql = handler.getBoundSql();
        String sql = boundSql.getSql();
        if (hasStatementFor(sql)) {
            stmt = getStatement(sql);
            applyTransactionTimeout(stmt);
        } else {
            Connection connection = getConnection(statementLog);
            stmt = handler.prepare(connection, transaction.getTimeout());
            putStatement(sql, stmt);
        }
        handler.parameterize(stmt);
        return stmt;
    }

    private boolean hasStatementFor(String sql) {
        try {
            return statementMap.keySet().contains(sql) && !statementMap.get(sql).getConnection().isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private Statement getStatement(String s) {
        return statementMap.get(s);
    }

    private void putStatement(String sql, Statement stmt) {
        statementMap.put(sql, stmt);
    }

}

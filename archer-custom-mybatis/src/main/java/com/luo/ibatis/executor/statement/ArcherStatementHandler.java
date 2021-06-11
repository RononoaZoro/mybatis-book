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
import com.luo.ibatis.executor.parameter.ArcherParameterHandler;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.session.ArcherResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Clinton Begin
 */
public interface ArcherStatementHandler {

    Statement prepare(Connection connection, Integer transactionTimeout)
            throws SQLException;

    void parameterize(Statement statement)
            throws SQLException;

    void batch(Statement statement)
            throws SQLException;

    int update(Statement statement)
            throws SQLException;

    <E> List<E> query(Statement statement, ArcherResultHandler resultHandler)
            throws SQLException;

    <E> Cursor<E> queryCursor(Statement statement)
            throws SQLException;

    ArcherBoundSql getBoundSql();

    ArcherParameterHandler getParameterHandler();

}

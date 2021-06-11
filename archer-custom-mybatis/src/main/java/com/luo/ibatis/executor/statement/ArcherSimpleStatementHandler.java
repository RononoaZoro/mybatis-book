///**
// * Copyright 2009-2015 the original author or authors.
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.luo.ibatis.executor.statement;
//
//import com.luo.ibatis.executor.ArcherExecutor;
//import com.luo.ibatis.mapping.ArcherBoundSql;
//import com.luo.ibatis.mapping.ArcherMappedStatement;
//import com.luo.ibatis.session.ArcherResultHandler;
//import com.luo.ibatis.session.ArcherRowBounds;
//
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.List;
//
///**
// * @author Clinton Begin
// */
//public class ArcherSimpleStatementHandler extends ArcherBaseStatementHandler {
//
//    public ArcherSimpleStatementHandler(ArcherExecutor executor, ArcherMappedStatement mappedStatement, Object parameter, ArcherRowBounds rowBounds, ArcherResultHandler resultHandler, ArcherBoundSql boundSql) {
//        super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
//    }
//
//    //  @Override
////  public int update(Statement statement) throws SQLException {
////    String sql = boundSql.getSql();
////    Object parameterObject = boundSql.getParameterObject();
////    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
////    int rows;
////    if (keyGenerator instanceof Jdbc3KeyGenerator) {
////      statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
////      rows = statement.getUpdateCount();
////      keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
////    } else if (keyGenerator instanceof SelectKeyGenerator) {
////      statement.execute(sql);
////      rows = statement.getUpdateCount();
////      keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
////    } else {
////      statement.execute(sql);
////      rows = statement.getUpdateCount();
////    }
////    return rows;
////  }
////
////  @Override
////  public void batch(Statement statement) throws SQLException {
////    String sql = boundSql.getSql();
////    statement.addBatch(sql);
////  }
////
//    @Override
//    public <E> List<E> query(Statement statement, ArcherResultHandler resultHandler) throws SQLException {
//        String sql = boundSql.getSql();
//        statement.execute(sql);
//        return resultSetHandler.<E>handleResultSets(statement);
//    }
////
////  @Override
////  public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
////    String sql = boundSql.getSql();
////    statement.execute(sql);
////    return resultSetHandler.<E>handleCursorResultSets(statement);
////  }
////
////  @Override
////  protected Statement instantiateStatement(Connection connection) throws SQLException {
////    if (mappedStatement.getResultSetType() != null) {
////      return connection.createStatement(mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
////    } else {
////      return connection.createStatement();
////    }
////  }
////
////  @Override
////  public void parameterize(Statement statement) throws SQLException {
////    // N/A
////  }
//
//}

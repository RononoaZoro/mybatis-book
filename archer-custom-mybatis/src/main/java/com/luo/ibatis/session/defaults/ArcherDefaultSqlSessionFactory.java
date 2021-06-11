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
package com.luo.ibatis.session.defaults;

import com.luo.ibatis.exceptions.ExceptionFactory;
import com.luo.ibatis.executor.ArcherExecutor;
import com.luo.ibatis.executor.ErrorContext;
import com.luo.ibatis.mapping.ArcherEnvironment;
import com.luo.ibatis.session.*;
import com.luo.ibatis.transaction.ArcherTransaction;
import com.luo.ibatis.transaction.ArcherTransactionFactory;
import com.luo.ibatis.transaction.managed.ArcherManagedTransactionFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 */
public class ArcherDefaultSqlSessionFactory implements ArcherSqlSessionFactory {

  private final ArcherConfiguration configuration;

  public ArcherDefaultSqlSessionFactory(ArcherConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public ArcherSqlSession openSession() {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
  }

  @Override
  public ArcherSqlSession openSession(boolean autoCommit) {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, autoCommit);
  }

  @Override
  public ArcherSqlSession openSession(ExecutorType execType) {
    return openSessionFromDataSource(execType, null, false);
  }

  @Override
  public ArcherSqlSession openSession(TransactionIsolationLevel level) {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), level, false);
  }

  @Override
  public ArcherSqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
    return openSessionFromDataSource(execType, level, false);
  }

  @Override
  public ArcherSqlSession openSession(ExecutorType execType, boolean autoCommit) {
    return openSessionFromDataSource(execType, null, autoCommit);
  }

  @Override
  public ArcherSqlSession openSession(Connection connection) {
    return openSessionFromConnection(configuration.getDefaultExecutorType(), connection);
  }

  @Override
  public ArcherSqlSession openSession(ExecutorType execType, Connection connection) {
    return openSessionFromConnection(execType, connection);
  }

  @Override
  public ArcherConfiguration getConfiguration() {
    return configuration;
  }

  private ArcherSqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    ArcherTransaction tx = null;
    try {
      // 获取Mybatis主配置文件配置的环境信息
      final ArcherEnvironment environment = configuration.getEnvironment();
      // 创建事务管理器工厂
      final ArcherTransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
      // 创建事务管理器
      tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
      // 根据Mybatis主配置文件中指定的Executor类型创建对应的Executor实例
      final ArcherExecutor executor = configuration.newExecutor(tx, execType);
      // 创建DefaultSqlSession实例
      return new ArcherDefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      closeTransaction(tx); // may have fetched a connection so lets call close()
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }

  private ArcherSqlSession openSessionFromConnection(ExecutorType execType, Connection connection) {
    try {
      boolean autoCommit;
      try {
        autoCommit = connection.getAutoCommit();
      } catch (SQLException e) {
        // Failover to true, as most poor drivers
        // or databases won't support transactions
        autoCommit = true;
      }      
      final ArcherEnvironment environment = configuration.getEnvironment();
      final ArcherTransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
      final ArcherTransaction tx = transactionFactory.newTransaction(connection);
      final ArcherExecutor executor = configuration.newExecutor(tx, execType);
      return new ArcherDefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }

  private ArcherTransactionFactory getTransactionFactoryFromEnvironment(ArcherEnvironment environment) {
    if (environment == null || environment.getTransactionFactory() == null) {
      return new ArcherManagedTransactionFactory();
    }
    return environment.getTransactionFactory();
  }

  private void closeTransaction(ArcherTransaction tx) {
    if (tx != null) {
      try {
        tx.close();
      } catch (SQLException ignore) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }

}

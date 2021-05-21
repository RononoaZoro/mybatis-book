package com.luo.ibatis.session.defaults;

import com.luo.ibatis.executor.ArcherExecutor;
import com.luo.ibatis.session.ArcherConfiguration;
import com.luo.ibatis.session.ArcherSqlSession;
import com.luo.ibatis.session.ArcherSqlSessionFactory;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

import java.sql.SQLException;

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
    public ArcherConfiguration getConfiguration() {
        return configuration;
    }

    private ArcherSqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
        Transaction tx = null;
        try {
            // 获取Mybatis主配置文件配置的环境信息
            final Environment environment = configuration.getEnvironment();
            // 创建事务管理器工厂
            final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
            // 创建事务管理器
            tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
            // 根据Mybatis主配置文件中指定的Executor类型创建对应的Executor实例
            final ArcherExecutor executor = configuration.newExecutorForArcher(tx, execType);
            // 创建DefaultSqlSession实例
            return new ArcherDefaultSqlSession(configuration, executor, autoCommit);
        } catch (Exception e) {
            closeTransaction(tx); // may have fetched a connection so lets call close()
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    private TransactionFactory getTransactionFactoryFromEnvironment(Environment environment) {
        if (environment == null || environment.getTransactionFactory() == null) {
            return new ManagedTransactionFactory();
        }
        return environment.getTransactionFactory();
    }

    private void closeTransaction(Transaction tx) {
        if (tx != null) {
            try {
                tx.close();
            } catch (SQLException ignore) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }
}

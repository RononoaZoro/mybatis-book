package com.luo.ibatis.session;

public interface ArcherSqlSessionFactory {

    ArcherSqlSession openSession();

//    SqlSession openSession(boolean autoCommit);
//    SqlSession openSession(Connection connection);
//    SqlSession openSession(TransactionIsolationLevel level);
//
//    SqlSession openSession(ExecutorType execType);
//    SqlSession openSession(ExecutorType execType, boolean autoCommit);
//    SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);
//    SqlSession openSession(ExecutorType execType, Connection connection);

    ArcherConfiguration getConfiguration();
}

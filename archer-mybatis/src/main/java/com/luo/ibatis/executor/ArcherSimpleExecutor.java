package com.luo.ibatis.executor;

import com.luo.ibatis.session.ArcherConfiguration;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BaseExecutor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

public class ArcherSimpleExecutor extends ArcherBaseExecutor {

    public ArcherSimpleExecutor(ArcherConfiguration configuration, Transaction transaction) {
        super(configuration, transaction);
    }



    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
        Statement stmt;
        // 获取JDBC中的Connection对象
        Connection connection = getConnection(statementLog);
        // 调用StatementHandler的prepare（）方法创建Statement对象
        stmt = handler.prepare(connection, transaction.getTimeout());
        // 调用StatementHandler对象的parameterize（）方法设置参数
        handler.parameterize(stmt);
        return stmt;
    }

    protected Connection getConnection(Log statementLog) throws SQLException {
        Connection connection = transaction.getConnection();
        if (statementLog.isDebugEnabled()) {
            return ConnectionLogger.newInstance(connection, statementLog, queryStack);
        } else {
            return connection;
        }
    }
}

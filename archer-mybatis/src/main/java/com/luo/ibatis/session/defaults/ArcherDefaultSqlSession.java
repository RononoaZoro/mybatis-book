package com.luo.ibatis.session.defaults;

import com.luo.ibatis.executor.ArcherExecutor;
import com.luo.ibatis.session.ArcherConfiguration;
import com.luo.ibatis.session.ArcherSqlSession;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.session.Configuration;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

public class ArcherDefaultSqlSession implements ArcherSqlSession {

    private final ArcherConfiguration configuration;
    private final ArcherExecutor executor;

    private final boolean autoCommit;
    private boolean dirty;
    private List<Cursor<?>> cursorList;

    public ArcherDefaultSqlSession(ArcherConfiguration configuration, ArcherExecutor executor, boolean autoCommit) {
        this.configuration = configuration;
        this.executor = executor;
        this.dirty = false;
        this.autoCommit = autoCommit;
    }

    public ArcherDefaultSqlSession(ArcherConfiguration configuration, ArcherExecutor executor) {
        this(configuration, executor, false);
    }

    @Override
    public void close() {
        try {
            executor.close(isCommitOrRollbackRequired(false));
            closeCursors();
            dirty = false;
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public ArcherConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return null;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    private boolean isCommitOrRollbackRequired(boolean force) {
        return (!autoCommit && dirty) || force;
    }

    private void closeCursors() {
        if (cursorList != null && cursorList.size() != 0) {
            for (Cursor<?> cursor : cursorList) {
                try {
                    cursor.close();
                } catch (IOException e) {
                    throw ExceptionFactory.wrapException("Error closing cursor.  Cause: " + e, e);
                }
            }
            cursorList.clear();
        }
    }
}

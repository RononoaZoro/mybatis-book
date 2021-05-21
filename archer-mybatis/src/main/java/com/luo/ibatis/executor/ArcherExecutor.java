package com.luo.ibatis.executor;

import java.sql.SQLException;

public interface ArcherExecutor {

    void close(boolean forceRollback);

    void rollback(boolean required) throws SQLException;
}

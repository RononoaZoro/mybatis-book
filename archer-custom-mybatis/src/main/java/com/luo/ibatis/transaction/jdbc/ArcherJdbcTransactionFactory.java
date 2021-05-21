/**
 *    Copyright 2009-2016 the original author or authors.
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
package com.luo.ibatis.transaction.jdbc;

import com.luo.ibatis.session.TransactionIsolationLevel;
import com.luo.ibatis.transaction.ArcherTransaction;
import com.luo.ibatis.transaction.ArcherTransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * Creates {@link ArcherJdbcTransaction} instances.
 *
 * @author Clinton Begin
 *
 * @see ArcherJdbcTransaction
 */
public class ArcherJdbcTransactionFactory implements ArcherTransactionFactory {

  @Override
  public void setProperties(Properties props) {
  }

  @Override
  public ArcherTransaction newTransaction(Connection conn) {
    return new ArcherJdbcTransaction(conn);
  }

  @Override
  public ArcherTransaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
    return new ArcherJdbcTransaction(ds, level, autoCommit);
  }
}

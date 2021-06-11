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
package com.luo.ibatis.transaction.managed;


import com.luo.ibatis.session.TransactionIsolationLevel;
import com.luo.ibatis.transaction.ArcherTransaction;
import com.luo.ibatis.transaction.ArcherTransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * Creates {@link ArcherManagedTransaction} instances.
 *
 * @author Clinton Begin
 *
 * @see ArcherManagedTransaction
 */
public class ArcherManagedTransactionFactory implements ArcherTransactionFactory {

  private boolean closeConnection = true;

  @Override
  public void setProperties(Properties props) {
    if (props != null) {
      String closeConnectionProperty = props.getProperty("closeConnection");
      if (closeConnectionProperty != null) {
        closeConnection = Boolean.valueOf(closeConnectionProperty);
      }
    }
  }

  @Override
  public ArcherTransaction newTransaction(Connection conn) {
    return new ArcherManagedTransaction(conn, closeConnection);
  }

  @Override
  public ArcherTransaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
    // Silently ignores autocommit and isolation level, as managed transactions are entirely
    // controlled by an external manager.  It's silently ignored so that
    // code remains portable between managed and unmanaged configurations.
    return new ArcherManagedTransaction(ds, level, closeConnection);
  }
}

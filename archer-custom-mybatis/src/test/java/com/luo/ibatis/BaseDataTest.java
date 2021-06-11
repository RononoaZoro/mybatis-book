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
package com.luo.ibatis;

import com.luo.ibatis.datasource.pooled.ArcherPooledDataSource;
import com.luo.ibatis.datasource.unpooled.ArcherUnpooledDataSource;
import com.luo.ibatis.io.ArcherResources;
import com.luo.ibatis.jdbc.ScriptRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public abstract class BaseDataTest {

  public static final String BLOG_PROPERTIES = "com/luo/ibatis/databases/blog/blog-derby.properties";
  public static final String BLOG_DDL = "com/luo/ibatis/databases/blog/blog-derby-schema.sql";
  public static final String BLOG_DATA = "com/luo/ibatis/databases/blog/blog-derby-dataload.sql";

  public static final String JPETSTORE_PROPERTIES = "com/luo/ibatis/databases/jpetstore/jpetstore-hsqldb.properties";
  public static final String JPETSTORE_DDL = "com/luo/ibatis/databases/jpetstore/jpetstore-hsqldb-schema.sql";
  public static final String JPETSTORE_DATA = "com/luo/ibatis/databases/jpetstore/jpetstore-hsqldb-dataload.sql";

  public static ArcherUnpooledDataSource createUnpooledDataSource(String resource) throws IOException {
    Properties props = ArcherResources.getResourceAsProperties(resource);
    ArcherUnpooledDataSource ds = new ArcherUnpooledDataSource();
    ds.setDriver(props.getProperty("driver"));
    ds.setUrl(props.getProperty("url"));
    ds.setUsername(props.getProperty("username"));
    ds.setPassword(props.getProperty("password"));
    return ds;
  }

  public static ArcherPooledDataSource createPooledDataSource(String resource) throws IOException {
    Properties props = ArcherResources.getResourceAsProperties(resource);
    ArcherPooledDataSource ds = new ArcherPooledDataSource();
    ds.setDriver(props.getProperty("driver"));
    ds.setUrl(props.getProperty("url"));
    ds.setUsername(props.getProperty("username"));
    ds.setPassword(props.getProperty("password"));
    return ds;
  }

  public static void runScript(DataSource ds, String resource) throws IOException, SQLException {
    Connection connection = ds.getConnection();
    try {
      ScriptRunner runner = new ScriptRunner(connection);
      runner.setAutoCommit(true);
      runner.setStopOnError(false);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runScript(runner, resource);
    } finally {
      connection.close();
    }
  }

  public static void runScript(ScriptRunner runner, String resource) throws IOException, SQLException {
    Reader reader = ArcherResources.getResourceAsReader(resource);
    try {
      runner.runScript(reader);
    } finally {
      reader.close();
    }
  }

  public static DataSource createBlogDataSource() throws IOException, SQLException {
    DataSource ds = createUnpooledDataSource(BLOG_PROPERTIES);
    runScript(ds, BLOG_DDL);
    runScript(ds, BLOG_DATA);
    return ds;
  }

  public static DataSource createJPetstoreDataSource() throws IOException, SQLException {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    return ds;
  }
}

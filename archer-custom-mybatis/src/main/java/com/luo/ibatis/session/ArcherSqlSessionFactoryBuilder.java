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
package com.luo.ibatis.session;

import com.luo.ibatis.builder.xml.ArcherXMLConfigBuilder;
import com.luo.ibatis.exceptions.ExceptionFactory;
import com.luo.ibatis.executor.ErrorContext;
import com.luo.ibatis.session.defaults.ArcherDefaultSqlSessionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * Builds {@link ArcherSqlSession} instances.
 *
 * @author Clinton Begin
 */
public class ArcherSqlSessionFactoryBuilder {

  public ArcherSqlSessionFactory build(Reader reader) {
    return build(reader, null, null);
  }

  public ArcherSqlSessionFactory build(Reader reader, String environment) {
    return build(reader, environment, null);
  }

  public ArcherSqlSessionFactory build(Reader reader, Properties properties) {
    return build(reader, null, properties);
  }

  public ArcherSqlSessionFactory build(Reader reader, String environment, Properties properties) {
    try {
      ArcherXMLConfigBuilder parser = new ArcherXMLConfigBuilder(reader, environment, properties);
      return build(parser.parse());
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error building SqlSession.", e);
    } finally {
      ErrorContext.instance().reset();
      try {
        reader.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }

  public ArcherSqlSessionFactory build(InputStream inputStream) {
    return build(inputStream, null, null);
  }

  public ArcherSqlSessionFactory build(InputStream inputStream, String environment) {
    return build(inputStream, environment, null);
  }

  public ArcherSqlSessionFactory build(InputStream inputStream, Properties properties) {
    return build(inputStream, null, properties);
  }

  public ArcherSqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
    try {
      ArcherXMLConfigBuilder parser = new ArcherXMLConfigBuilder(inputStream, environment, properties);
      return build(parser.parse());
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error building SqlSession.", e);
    } finally {
      ErrorContext.instance().reset();
      try {
        inputStream.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }
    
  public ArcherSqlSessionFactory build(ArcherConfiguration config) {
    return new ArcherDefaultSqlSessionFactory(config);
  }

}

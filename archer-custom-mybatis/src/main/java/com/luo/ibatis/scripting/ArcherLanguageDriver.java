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
package com.luo.ibatis.scripting;


import com.luo.ibatis.executor.parameter.ArcherParameterHandler;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherMappedStatement;
import com.luo.ibatis.mapping.ArcherSqlSource;
import com.luo.ibatis.parsing.XNode;
import com.luo.ibatis.scripting.defaults.ArcherDefaultParameterHandler;
import com.luo.ibatis.session.ArcherConfiguration;

public interface ArcherLanguageDriver {

  /**
   * Creates a {@link ArcherParameterHandler} that passes the actual parameters to the the JDBC statement.
   * 
   * @param mappedStatement The mapped statement that is being executed
   * @param parameterObject The input parameter object (can be null) 
   * @param boundSql The resulting SQL once the dynamic language has been executed.
   * @return
   * @author Frank D. Martinez [mnesarco]
   * @see ArcherDefaultParameterHandler
   */
  ArcherParameterHandler createParameterHandler(ArcherMappedStatement mappedStatement, Object parameterObject, ArcherBoundSql boundSql);

  /**
   * Creates an {@link ArcherSqlSource} that will hold the statement read from a mapper xml file.
   * It is called during startup, when the mapped statement is read from a class or an xml file.
   * 
   * @param configuration The MyBatis configuration
   * @param script XNode parsed from a XML file
   * @param parameterType input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be null.
   * @return
   */
  ArcherSqlSource createSqlSource(ArcherConfiguration configuration, XNode script, Class<?> parameterType);

  /**
   * Creates an {@link SqlSource} that will hold the statement read from an annotation.
   * It is called during startup, when the mapped statement is read from a class or an xml file.
   * 
   * @param configuration The MyBatis configuration
   * @param script The content of the annotation
   * @param parameterType input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be null.
   * @return 
   */
//  SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

}

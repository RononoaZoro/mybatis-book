/**
 *    Copyright 2009-2017 the original author or authors.
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
package com.luo.ibatis.scripting.defaults;

import com.luo.ibatis.executor.ErrorContext;
import com.luo.ibatis.executor.parameter.ArcherParameterHandler;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherMappedStatement;
import com.luo.ibatis.mapping.ParameterMapping;
import com.luo.ibatis.mapping.ParameterMode;
import com.luo.ibatis.reflection.MetaObject;
import com.luo.ibatis.session.ArcherConfiguration;
import com.luo.ibatis.type.ArcherTypeHandlerRegistry;
import com.luo.ibatis.type.JdbcType;
import com.luo.ibatis.type.TypeException;
import com.luo.ibatis.type.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class ArcherDefaultParameterHandler implements ArcherParameterHandler {

  private final ArcherTypeHandlerRegistry typeHandlerRegistry;

  private final ArcherMappedStatement mappedStatement;
  private final Object parameterObject;
  private final ArcherBoundSql boundSql;
  private final ArcherConfiguration configuration;

  public ArcherDefaultParameterHandler(ArcherMappedStatement mappedStatement, Object parameterObject, ArcherBoundSql boundSql) {
    this.mappedStatement = mappedStatement;
    this.configuration = mappedStatement.getConfiguration();
    this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
    this.parameterObject = parameterObject;
    this.boundSql = boundSql;
  }

  @Override
  public Object getParameterObject() {
    return parameterObject;
  }

  @Override
  public void setParameters(PreparedStatement ps) {
    ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
    // 获取所有参数映射信息
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings != null) {
      for (int i = 0; i < parameterMappings.size(); i++) {
        ParameterMapping parameterMapping = parameterMappings.get(i);
        if (parameterMapping.getMode() != ParameterMode.OUT) {
          Object value;
          // 参数属性名称
          String propertyName = parameterMapping.getProperty();
          // 根据参数属性名称，获取参数值
          if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
            value = boundSql.getAdditionalParameter(propertyName);
          } else if (parameterObject == null) {
            value = null;
          } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            value = parameterObject;
          } else {
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            value = metaObject.getValue(propertyName);
          }
          // 获取参数对应的TypeHandler
          TypeHandler typeHandler = parameterMapping.getTypeHandler();
          JdbcType jdbcType = parameterMapping.getJdbcType();
          if (value == null && jdbcType == null) {
            jdbcType = configuration.getJdbcTypeForNull();
          }
          try {
            // 调用TypeHandler的setParameter方法，为Statement对象参数占位符设置值
            typeHandler.setParameter(ps, i + 1, value, jdbcType);
          } catch (TypeException e) {
            throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
          } catch (SQLException e) {
            throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
          }
        }
      }
    }
  }

}

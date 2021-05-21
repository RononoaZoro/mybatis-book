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
package com.luo.ibatis.scripting.xmltags;

import com.luo.ibatis.builder.ArcherSqlSourceBuilder;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherSqlSource;
import com.luo.ibatis.session.ArcherConfiguration;

import java.util.Map;

/**
 * @author Clinton Begin
 */
public class DynamicSqlSource implements ArcherSqlSource {

  private final ArcherConfiguration configuration;
  private final SqlNode rootSqlNode;

  public DynamicSqlSource(ArcherConfiguration configuration, SqlNode rootSqlNode) {
    this.configuration = configuration;
    this.rootSqlNode = rootSqlNode;
  }

  @Override
  public ArcherBoundSql getBoundSql(Object parameterObject) {
    // 通过参数对象，创建动态SQL上下文对象
    ArcherDynamicContext context = new ArcherDynamicContext(configuration, parameterObject);
    // 以DynamicContext对象作为参数调用SqlNode的apply（）方法
    rootSqlNode.apply(context);
    // 创建SqlSourceBuilder对象
    ArcherSqlSourceBuilder sqlSourceParser = new ArcherSqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    // 调用DynamicContext的getSql()方法获取动态SQL解析后的SQL内容，
    // 然后调用SqlSourceBuilder的parse（）方法对SQL内容做进一步处理，生成StaticSqlSource对象
    ArcherSqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
    // 调用StaticSqlSource对象的getBoundSql（）方法，获得BoundSql实例
    ArcherBoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    // 將<bind>标签绑定的参数添加到BoundSql对象中
    for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
      boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
    }
    return boundSql;
  }

}

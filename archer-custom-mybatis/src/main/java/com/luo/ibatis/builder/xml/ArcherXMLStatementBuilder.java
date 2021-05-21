/**
 *    Copyright 2009-2018 the original author or authors.
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
package com.luo.ibatis.builder.xml;

import com.luo.ibatis.builder.ArcherBaseBuilder;
import com.luo.ibatis.builder.ArcherMapperBuilderAssistant;
import com.luo.ibatis.executor.keygen.KeyGenerator;
import com.luo.ibatis.executor.keygen.NoKeyGenerator;
import com.luo.ibatis.mapping.ArcherSqlSource;
import com.luo.ibatis.mapping.ResultSetType;
import com.luo.ibatis.mapping.SqlCommandType;
import com.luo.ibatis.mapping.StatementType;
import com.luo.ibatis.parsing.XNode;
import com.luo.ibatis.scripting.ArcherLanguageDriver;
import com.luo.ibatis.session.ArcherConfiguration;

import java.util.List;
import java.util.Locale;

/**
 * @author Clinton Begin
 */
public class ArcherXMLStatementBuilder extends ArcherBaseBuilder {

  private final ArcherMapperBuilderAssistant builderAssistant;
  private final XNode context;

  public ArcherXMLStatementBuilder(ArcherConfiguration configuration, ArcherMapperBuilderAssistant builderAssistant, XNode context) {
    super(configuration);
    this.builderAssistant = builderAssistant;
    this.context = context;
  }

  public void parseStatementNode() {
    String id = context.getStringAttribute("id");
    String databaseId = context.getStringAttribute("databaseId");

//    if (!databaseIdMatchesCurrent(id, databaseId, this.requiredDatabaseId)) {
//      return;
//    }

    // 解析<select|update|delete|insert>标签属性
    Integer fetchSize = context.getIntAttribute("fetchSize");
    Integer timeout = context.getIntAttribute("timeout");
    String parameterMap = context.getStringAttribute("parameterMap");
    String parameterType = context.getStringAttribute("parameterType");
    Class<?> parameterTypeClass = resolveClass(parameterType);
    String resultMap = context.getStringAttribute("resultMap");
    String resultType = context.getStringAttribute("resultType");
    // 获取LanguageDriver对象
    String lang = context.getStringAttribute("lang");
    ArcherLanguageDriver langDriver = getLanguageDriver(lang);
    // 获取Mapper返回结果类型Class对象
    Class<?> resultTypeClass = resolveClass(resultType);
    String resultSetType = context.getStringAttribute("resultSetType");
    // 默认Statement类型为PREPARED
    StatementType statementType = StatementType.valueOf(context.getStringAttribute("statementType",
            StatementType.PREPARED.toString()));
    ResultSetType resultSetTypeEnum = resolveResultSetType(resultSetType);

    String nodeName = context.getNode().getNodeName();
    SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
    boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
    boolean flushCache = context.getBooleanAttribute("flushCache", !isSelect);
    boolean useCache = context.getBooleanAttribute("useCache", isSelect);
    boolean resultOrdered = context.getBooleanAttribute("resultOrdered", false);

    // 將<include>标签内容，替换为<sql>标签定义的SQL片段
    ArcherXMLIncludeTransformer includeParser = new ArcherXMLIncludeTransformer(configuration, builderAssistant);
    includeParser.applyIncludes(context.getNode());

//    // 解析<selectKey>标签
//    processSelectKeyNodes(id, parameterTypeClass, langDriver);
//
    // 通过LanguageDriver解析SQL内容，生成SqlSource对象
    ArcherSqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);
    String resultSets = context.getStringAttribute("resultSets");
    String keyProperty = context.getStringAttribute("keyProperty");
    String keyColumn = context.getStringAttribute("keyColumn");
//    KeyGenerator keyGenerator;
//    String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
//    keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
    // 获取主键生成策略
//    if (configuration.hasKeyGenerator(keyStatementId)) {
//      keyGenerator = configuration.getKeyGenerator(keyStatementId);
//    } else {
//      keyGenerator = context.getBooleanAttribute("useGeneratedKeys",
//              configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType))
//              ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
//    }

    KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;

    builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
            fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass,
            resultSetTypeEnum, flushCache, useCache, resultOrdered,
            keyGenerator, keyProperty, keyColumn, databaseId, langDriver, resultSets);
  }


  private ArcherLanguageDriver getLanguageDriver(String lang) {
    Class<? extends ArcherLanguageDriver> langClass = null;
    if (lang != null) {
      langClass = resolveClass(lang);
    }
    return builderAssistant.getLanguageDriver(langClass);
  }


}

package com.luo.ibatis.builder.xml;

import com.luo.ibatis.builder.ArcherBaseBuilder;
import com.luo.ibatis.builder.ArcherMapperBuilderAssistant;
import com.luo.ibatis.scripting.xmltags.ArcherXMLLanguageDriver;
import com.luo.ibatis.session.ArcherConfiguration;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.util.List;
import java.util.Locale;

public class ArcherXMLStatementBuilder extends ArcherBaseBuilder {

    private final ArcherMapperBuilderAssistant builderAssistant;
    private final XNode context;
    private final String requiredDatabaseId;

    public ArcherXMLStatementBuilder(ArcherConfiguration configuration, ArcherMapperBuilderAssistant builderAssistant, XNode context) {
        this(configuration, builderAssistant, context, null);
    }

    public ArcherXMLStatementBuilder(ArcherConfiguration configuration, ArcherMapperBuilderAssistant builderAssistant, XNode context, String databaseId) {
        super(configuration);
        this.builderAssistant = builderAssistant;
        this.context = context;
        this.requiredDatabaseId = databaseId;
    }

    public void parseStatementNode() {
        String id = context.getStringAttribute("id");
        String databaseId = context.getStringAttribute("databaseId");

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
//        LanguageDriver langDriver = getLanguageDriver(lang);
        LanguageDriver langDriver = new ArcherXMLLanguageDriver();
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

//        // 解析<selectKey>标签
//        processSelectKeyNodes(id, parameterTypeClass, langDriver);
//
//        // 通过LanguageDriver解析SQL内容，生成SqlSource对象
        SqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);
        String resultSets = context.getStringAttribute("resultSets");
        String keyProperty = context.getStringAttribute("keyProperty");
        String keyColumn = context.getStringAttribute("keyColumn");
        KeyGenerator keyGenerator;
        String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
        // 获取主键生成策略
        if (configuration.hasKeyGenerator(keyStatementId)) {
            keyGenerator = configuration.getKeyGenerator(keyStatementId);
        } else {
            keyGenerator = context.getBooleanAttribute("useGeneratedKeys",
                    configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType))
                    ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
        }

        builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
                fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass,
                resultSetTypeEnum, flushCache, useCache, resultOrdered,
                keyGenerator, keyProperty, keyColumn, databaseId, langDriver, resultSets);
    }



}

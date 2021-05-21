package com.luo.ibatis.builder.xml;

import com.luo.ibatis.builder.ArcherBaseBuilder;
import com.luo.ibatis.builder.ArcherMapperBuilderAssistant;
import com.luo.ibatis.session.ArcherConfiguration;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public class ArcherXMLMapperBuilder extends ArcherBaseBuilder {

    private final XPathParser parser;
    private final ArcherMapperBuilderAssistant builderAssistant;
    private final Map<String, XNode> sqlFragments;
    private final String resource;

    @Deprecated
    public ArcherXMLMapperBuilder(Reader reader, ArcherConfiguration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
        this(reader, configuration, resource, sqlFragments);
        this.builderAssistant.setCurrentNamespace(namespace);
    }

    @Deprecated
    public ArcherXMLMapperBuilder(Reader reader, ArcherConfiguration configuration, String resource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(reader, true, configuration.getVariables(), new XMLMapperEntityResolver()),
                configuration, resource, sqlFragments);
    }

    public ArcherXMLMapperBuilder(InputStream inputStream, ArcherConfiguration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
        this(inputStream, configuration, resource, sqlFragments);
        this.builderAssistant.setCurrentNamespace(namespace);
    }

    public ArcherXMLMapperBuilder(InputStream inputStream, ArcherConfiguration configuration, String resource, Map<String, XNode> sqlFragments) {
        this(new XPathParser(inputStream, true, configuration.getVariables(), new XMLMapperEntityResolver()),
                configuration, resource, sqlFragments);
    }

    private ArcherXMLMapperBuilder(XPathParser parser, ArcherConfiguration configuration, String resource, Map<String, XNode> sqlFragments) {
        super(configuration);
        this.builderAssistant = new ArcherMapperBuilderAssistant(configuration, resource);
        this.parser = parser;
        this.sqlFragments = sqlFragments;
        this.resource = resource;
    }

    public void parse() {
        if (!configuration.isResourceLoaded(resource)) {
            // 调用XPathParser的evalNode（）方法获取根节点对应的XNode对象
            configurationElement(parser.evalNode("/mapper"));
            // 將资源路径添加到Configuration对象中
            configuration.addLoadedResource(resource);

            bindMapperForNamespace();
        }
        // 继续解析之前解析出现异常的ResultMap对象
//        parsePendingResultMaps();
        // 继续解析之前解析出现异常的CacheRef对象
//        parsePendingCacheRefs();
        // 继续解析之前解析出现异常<select|update|delete|insert>标签配置
//        parsePendingStatements();
    }

    private void configurationElement(XNode context) {
        try {
            // 获取命名空间
            String namespace = context.getStringAttribute("namespace");
            if (namespace == null || namespace.equals("")) {
                throw new BuilderException("Mapper's namespace cannot be empty");
            }
            // 设置当前正在解析的Mapper配置的命名空间
            builderAssistant.setCurrentNamespace(namespace);
            // 解析<cache-ref>标签
//            cacheRefElement(context.evalNode("cache-ref"));
//            // 解析<cache>标签
//            cacheElement(context.evalNode("cache"));
//            // 解析所有的<parameterMap>标签
//            parameterMapElement(context.evalNodes("/mapper/parameterMap"));
//            // 解析所有的<resultMap>标签
//            resultMapElements(context.evalNodes("/mapper/resultMap"));
//            // 解析所有的<sql>标签
            sqlElement(context.evalNodes("/mapper/sql"));
//            // 解析所有的<select|insert|update|delete>标签
            buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
        }
    }

    private void sqlElement(List<XNode> list) throws Exception {
        if (configuration.getDatabaseId() != null) {
            sqlElement(list, configuration.getDatabaseId());
        }
        sqlElement(list, null);
    }

    private void sqlElement(List<XNode> list, String requiredDatabaseId) throws Exception {
        for (XNode context : list) {
            String databaseId = context.getStringAttribute("databaseId");
            String id = context.getStringAttribute("id");
            id = builderAssistant.applyCurrentNamespace(id, false);
//            if (databaseIdMatchesCurrent(id, databaseId, requiredDatabaseId)) {
            sqlFragments.put(id, context);
//            }
        }
    }

    private void buildStatementFromContext(List<XNode> list) {
        if (configuration.getDatabaseId() != null) {
            buildStatementFromContext(list, configuration.getDatabaseId());
        }
        buildStatementFromContext(list, null);
    }

    private void buildStatementFromContext(List<XNode> list, String requiredDatabaseId) {
        for (XNode context : list) {
            // 通过XMLStatementBuilder对象，对<select|update|insert|delete>标签进行解析
            final ArcherXMLStatementBuilder statementParser = new ArcherXMLStatementBuilder(configuration, builderAssistant, context, requiredDatabaseId);
            try {
                // 调用parseStatementNode（）方法解析
                statementParser.parseStatementNode();
            } catch (IncompleteElementException e) {
//                configuration.addIncompleteStatement(statementParser);
            }
        }
    }

    private void bindMapperForNamespace() {
        String namespace = builderAssistant.getCurrentNamespace();
        if (namespace != null) {
            Class<?> boundType = null;
            try {
                boundType = Resources.classForName(namespace);
            } catch (ClassNotFoundException e) {
                //ignore, bound type is not required
            }
            if (boundType != null) {
                if (!configuration.hasMapper(boundType)) {
                    // Spring may not know the real resource name so we set a flag
                    // to prevent loading again this resource from the mapper interface
                    // look at MapperAnnotationBuilder#loadXmlResource
                    configuration.addLoadedResource("namespace:" + namespace);
                    configuration.addMapper(boundType);
                }
            }
        }
    }
}

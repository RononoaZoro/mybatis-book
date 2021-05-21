package com.luo.ibatis.builder.xml;

import com.luo.ibatis.builder.ArcherBaseBuilder;
import com.luo.ibatis.session.ArcherConfiguration;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.JdbcType;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

public class ArcherXMLConfigBuilder extends ArcherBaseBuilder {

    private boolean parsed;
    private final XPathParser parser;
    private String environment;
    private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();


    public ArcherXMLConfigBuilder(Reader reader) {
        this(reader, null, null);
    }

    public ArcherXMLConfigBuilder(Reader reader, String environment) {
        this(reader, environment, null);
    }

    public ArcherXMLConfigBuilder(Reader reader, String environment, Properties props) {
        this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    public ArcherXMLConfigBuilder(InputStream inputStream) {
        this(inputStream, null, null);
    }

    public ArcherXMLConfigBuilder(InputStream inputStream, String environment) {
        this(inputStream, environment, null);
    }

    public ArcherXMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    private ArcherXMLConfigBuilder(XPathParser parser, String environment, Properties props) {
        super(new ArcherConfiguration());
        ErrorContext.instance().resource("SQL Mapper Configuration");
//        this.configuration.setVariables(props);
        this.parsed = false;
        this.environment = environment;
        this.parser = parser;
    }

    public ArcherConfiguration parse() {
        // 防止parse（）方法被同一个实例多次调用
        if (parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;
        // 调用XPathParser.evalNode（）方法，创建表示configuration节点的XNode对象。
        // 调用parseConfiguration（）方法对XNode进行处理
        parseConfiguration(parser.evalNode("/configuration"));
        return configuration;
    }

    private void parseConfiguration(XNode root) {
        try {
//            issue #117 read properties first

            //properties 节点解析
//            propertiesElement(root.evalNode("properties"));

            //settings 节点解析为 Properties 对象
            Properties settings = settingsAsProperties(root.evalNode("settings"));

            //接入虚拟文件系统
//            loadCustomVfs(settings);

            //别名解析
//            typeAliasesElement(root.evalNode("typeAliases"));

            //插件解析
//            pluginElement(root.evalNode("plugins"));

            //对象工厂
//            objectFactoryElement(root.evalNode("objectFactory"));

            //对象工厂装饰类
//            objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));

            //反射
//            reflectorFactoryElement(root.evalNode("reflectorFactory"));

            // 将 settings 节点里的详细保存到 Configuration 对象中
            settingsElement(settings);

//            // read it after objectFactory and objectWrapperFactory issue #631
            //environments 节点解析 数据源信息相关
            environmentsElement(root.evalNode("environments"));

            // 数据库厂商表示节点 解析
//            databaseIdProviderElement(root.evalNode("databaseIdProvider"));

            //typeHandler 节点解析
//            typeHandlerElement(root.evalNode("typeHandlers"));

            //mapper节点 文件解析
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    private Properties settingsAsProperties(XNode context) {
        if (context == null) {
            return new Properties();
        }
        Properties props = context.getChildrenAsProperties();
        // Check that all settings are known to the configuration class
        MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
        for (Object key : props.keySet()) {
            if (!metaConfig.hasSetter(String.valueOf(key))) {
                throw new BuilderException("The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
            }
        }
        return props;
    }

    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            if (environment == null) {
                environment = context.getStringAttribute("default");
            }
            for (XNode child : context.getChildren()) {
                String id = child.getStringAttribute("id");
                if (isSpecifiedEnvironment(id)) {
                    TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
                    DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
                    DataSource dataSource = dsFactory.getDataSource();
                    Environment.Builder environmentBuilder = new Environment.Builder(id)
                            .transactionFactory(txFactory)
                            .dataSource(dataSource);
                    configuration.setEnvironment(environmentBuilder.build());
                }
            }
        }
    }

    private void settingsElement(Properties props) throws Exception {
        configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));
        configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
        @SuppressWarnings("unchecked")
        Class<? extends Log> logImpl = (Class<? extends Log>)resolveClass(props.getProperty("logImpl"));
        configuration.setLogImpl(logImpl);
    }

    private TransactionFactory transactionManagerElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            TransactionFactory factory = (TransactionFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a TransactionFactory.");
    }

    private DataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a DataSourceFactory.");
    }

    private void typeHandlerElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                if ("package".equals(child.getName())) {
                    String typeHandlerPackage = child.getStringAttribute("name");
                    typeHandlerRegistry.register(typeHandlerPackage);
                } else {
                    String javaTypeName = child.getStringAttribute("javaType");
                    String jdbcTypeName = child.getStringAttribute("jdbcType");
                    String handlerTypeName = child.getStringAttribute("handler");
                    Class<?> javaTypeClass = resolveClass(javaTypeName);
                    JdbcType jdbcType = resolveJdbcType(jdbcTypeName);
                    Class<?> typeHandlerClass = resolveClass(handlerTypeName);
                    if (javaTypeClass != null) {
                        if (jdbcType == null) {
                            typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
                        } else {
                            typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
                        }
                    } else {
                        typeHandlerRegistry.register(typeHandlerClass);
                    }
                }
            }
        }
    }

    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                // 通过<package>标签指定包名
                if ("package".equals(child.getName())) {
                    String mapperPackage = child.getStringAttribute("name");
                    configuration.addMappers(mapperPackage);
                } else {
                    String resource = child.getStringAttribute("resource");
                    String url = child.getStringAttribute("url");
                    String mapperClass = child.getStringAttribute("class");
                    // 通过resource属性指定XML文件路径
                    if (resource != null && url == null && mapperClass == null) {
                        ErrorContext.instance().resource(resource);
                        InputStream inputStream = Resources.getResourceAsStream(resource);
                        ArcherXMLMapperBuilder mapperParser = new ArcherXMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
                        mapperParser.parse();
                    }
//                    else if (resource == null && url != null && mapperClass == null) {
//                        // 通过url属性指定XML文件路径
//                        ErrorContext.instance().resource(url);
//                        InputStream inputStream = Resources.getUrlAsStream(url);
//                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
//                        mapperParser.parse();
//                    } else if (resource == null && url == null && mapperClass != null) {
//                        // 通过class属性指定接口的完全限定名
//                        Class<?> mapperInterface = Resources.classForName(mapperClass);
//                        configuration.addMapper(mapperInterface);
//                    }
                    else {
                        throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                    }
                }
            }
        }
    }


    private boolean isSpecifiedEnvironment(String id) {
        if (environment == null) {
            throw new BuilderException("No environment specified.");
        } else if (id == null) {
            throw new BuilderException("Environment requires an id attribute.");
        } else if (environment.equals(id)) {
            return true;
        }
        return false;
    }


}

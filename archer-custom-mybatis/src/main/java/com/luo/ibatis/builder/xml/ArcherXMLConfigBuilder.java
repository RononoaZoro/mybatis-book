package com.luo.ibatis.builder.xml;

import com.luo.ibatis.builder.ArcherBaseBuilder;
import com.luo.ibatis.builder.BuilderException;
import com.luo.ibatis.datasource.ArcherDataSourceFactory;
import com.luo.ibatis.io.ArcherResources;
import com.luo.ibatis.logging.Log;
import com.luo.ibatis.mapping.ArcherEnvironment;
import com.luo.ibatis.parsing.XNode;
import com.luo.ibatis.parsing.XPathParser;
import com.luo.ibatis.reflection.DefaultReflectorFactory;
import com.luo.ibatis.reflection.MetaClass;
import com.luo.ibatis.reflection.ReflectorFactory;
import com.luo.ibatis.session.ArcherConfiguration;
import com.luo.ibatis.transaction.ArcherTransactionFactory;

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
        this(new XPathParser(reader, true, null, new ArcherXMLMapperEntityResolver()), null, null);
    }

    public ArcherXMLConfigBuilder(Reader reader, String environment, Properties props) {
        this(new XPathParser(reader, true, props, new ArcherXMLMapperEntityResolver()), environment, props);
    }

    public ArcherXMLConfigBuilder(InputStream inputStream, String environment) {
        this(inputStream, environment, null);
    }

    public ArcherXMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        this(new XPathParser(inputStream, true, props, new ArcherXMLMapperEntityResolver()), environment, props);
    }

    private ArcherXMLConfigBuilder(XPathParser parser, String environment, Properties props) {
        super(new ArcherConfiguration());
        this.parsed = false;
        this.parser = parser;
        this.environment = environment;
//        this.configuration.setVariables(props);
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
            Properties settings = settingsAsProperties(root.evalNode("settings"));
            settingsElement(settings);
            environmentsElement(root.evalNode("environments"));
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
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
                        InputStream inputStream = ArcherResources.getResourceAsStream(resource);
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

    private void settingsElement(Properties props) throws Exception {
//        configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
        @SuppressWarnings("unchecked")
        Class<? extends Log> logImpl = (Class<? extends Log>) resolveClass(props.getProperty("logImpl"));
        configuration.setLogImpl(logImpl);
    }

    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            if (environment == null) {
                environment = context.getStringAttribute("default");
            }
            for (XNode child : context.getChildren()) {
                String id = child.getStringAttribute("id");
                if (isSpecifiedEnvironment(id)) {
                    ArcherTransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
                    ArcherDataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
                    DataSource dataSource = dsFactory.getDataSource();
                    ArcherEnvironment.Builder environmentBuilder = new ArcherEnvironment.Builder(id)
                            .transactionFactory(txFactory)
                            .dataSource(dataSource);
                    configuration.setEnvironment(environmentBuilder.build());
                }
            }
        }
    }

    private ArcherDataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            ArcherDataSourceFactory factory = (ArcherDataSourceFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a DataSourceFactory.");
    }

    private ArcherTransactionFactory transactionManagerElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            ArcherTransactionFactory factory = (ArcherTransactionFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a TransactionFactory.");
    }

    private Properties settingsAsProperties(XNode context) {
        if (context == null) {
            return new Properties();
        }
        Properties props = context.getChildrenAsProperties();
        // Check that all settings are known to the configuration class
        MetaClass metaConfig = MetaClass.forClass(ArcherConfiguration.class, localReflectorFactory);
        for (Object key : props.keySet()) {
            if (!metaConfig.hasSetter(String.valueOf(key))) {
                throw new BuilderException("The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
            }
        }
        return props;
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

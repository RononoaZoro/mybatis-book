package com.luo.ibatis.session;

import com.luo.ibatis.builder.xml.ArcherXMLConfigBuilder;
import com.luo.ibatis.session.defaults.ArcherDefaultSqlSessionFactory;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

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

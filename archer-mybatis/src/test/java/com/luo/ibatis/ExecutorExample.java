package com.luo.ibatis;

import com.alibaba.fastjson.JSON;
import com.blog4java.common.DbUtils;
import com.luo.ibatis.entity.UserEntity;
import com.luo.ibatis.session.ArcherConfiguration;
import com.luo.ibatis.session.ArcherSqlSession;
import com.luo.ibatis.session.ArcherSqlSessionFactory;
import com.luo.ibatis.session.ArcherSqlSessionFactoryBuilder;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

public class ExecutorExample {

    @Before
    public void initData() {
        DbUtils.initData();
    }
    @Test
    public void testExecutor() throws IOException, SQLException {
        // 获取配置文件输入流
        InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        // 通过SqlSessionFactoryBuilder的build()方法创建SqlSessionFactory实例
        ArcherSqlSessionFactory sqlSessionFactory = new ArcherSqlSessionFactoryBuilder().build(inputStream);
        // 调用openSession()方法创建SqlSession实例
        ArcherSqlSession sqlSession = sqlSessionFactory.openSession();
        ArcherConfiguration configuration = sqlSession.getConfiguration();
        // 从Configuration对象中获取描述SQL配置的MappedStatement对象
        MappedStatement listAllUserStmt = configuration.getMappedStatement(
                "com.blog4java.mybatis.com.blog4java.mybatis.com.luo.example.mapper.UserMapper.listAllUser");
        //创建ReuseExecutor实例
        Executor reuseExecutor = configuration.newExecutor(
                new JdbcTransaction(sqlSession.getConnection()),
                ExecutorType.REUSE
        );
        // 调用query()方法执行查询操作
        List<UserEntity> userList =  reuseExecutor.query(listAllUserStmt,
                null,
                RowBounds.DEFAULT,
                Executor.NO_RESULT_HANDLER);
        System.out.println(JSON.toJSON(userList));
    }


}

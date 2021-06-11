package com.luo.ibatis;

import com.alibaba.fastjson.JSON;
import com.luo.ibatis.entity.UserEntity;
import com.luo.ibatis.executor.ArcherExecutor;
import com.luo.ibatis.io.ArcherResources;
import com.luo.ibatis.mapping.ArcherMappedStatement;
import com.luo.ibatis.session.*;
import com.luo.ibatis.transaction.jdbc.ArcherJdbcTransaction;
import com.luo.ibatis.utils.DbUtils;
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
        InputStream inputStream = ArcherResources.getResourceAsStream("mybatis-config.xml");
        // 通过SqlSessionFactoryBuilder的build()方法创建SqlSessionFactory实例
        ArcherSqlSessionFactory sqlSessionFactory = new ArcherSqlSessionFactoryBuilder().build(inputStream);
        // 调用openSession()方法创建SqlSession实例
        ArcherSqlSession sqlSession = sqlSessionFactory.openSession();
        ArcherConfiguration configuration = sqlSession.getConfiguration();
        // 从Configuration对象中获取描述SQL配置的MappedStatement对象
        ArcherMappedStatement listAllUserStmt = configuration.getMappedStatement(
                "com.luo.ibatis.mapper.UserMapper.listAllUser");
        //创建ReuseExecutor实例
        ArcherExecutor reuseExecutor = configuration.newExecutor(
                new ArcherJdbcTransaction(sqlSession.getConnection()),
                ExecutorType.REUSE
        );
        // 调用query()方法执行查询操作
        List<UserEntity> userList =  reuseExecutor.query(listAllUserStmt,
                null,
                ArcherRowBounds.DEFAULT,
                ArcherExecutor.NO_RESULT_HANDLER);
        System.out.println(JSON.toJSON(userList));
    }


}

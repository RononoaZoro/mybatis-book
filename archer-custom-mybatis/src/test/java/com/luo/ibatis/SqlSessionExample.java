package com.luo.ibatis;

import com.luo.ibatis.io.ArcherResources;
import com.luo.ibatis.session.ArcherSqlSession;
import com.luo.ibatis.session.ArcherSqlSessionFactory;
import com.luo.ibatis.session.ArcherSqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

public class SqlSessionExample {

    @Test
    public void testSqlSession() throws IOException {
        // 获取Mybatis配置文件输入流
        Reader reader = ArcherResources.getResourceAsReader("mybatis-config.xml");
        // 通过SqlSessionFactoryBuilder创建SqlSessionFactory实例
        ArcherSqlSessionFactory sqlSessionFactory = new ArcherSqlSessionFactoryBuilder().build(reader);
        // 调用SqlSessionFactory的openSession（）方法，创建SqlSession实例
        ArcherSqlSession session = sqlSessionFactory.openSession();
        System.out.println();
    }
}

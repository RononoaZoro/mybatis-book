package com.luo.ibatis;

import com.luo.ibatis.builder.xml.ArcherXMLConfigBuilder;
import com.luo.ibatis.session.ArcherConfiguration;
import org.apache.ibatis.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

public class ConfigurationExample {

    @Test
    public void testConfiguration() throws IOException {
        Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
        // 创建XMLConfigBuilder实例
        ArcherXMLConfigBuilder builder = new ArcherXMLConfigBuilder(reader);
        // 调用XMLConfigBuilder.parse（）方法，解析XML创建Configuration对象
        ArcherConfiguration conf = builder.parse();
        System.out.println();
    }

}

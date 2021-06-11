package com.luo.ibatis.parsing;

import com.alibaba.fastjson.JSON;
import com.luo.ibatis.entity.UserEntity;
import com.luo.ibatis.io.ArcherResources;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.junit.Test;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XPathParserExample {

    @Test
    public void testXPathParser() throws Exception {
        Reader resource = ArcherResources.getResourceAsReader("users.xml");
        XPathParser parser = new XPathParser(resource);
        // 注册日期转换器
        DateConverter dateConverter = new DateConverter(null);
        dateConverter.setPattern("yyyy-MM-dd HH:mm:ss");
        ConvertUtils.register(dateConverter, Date.class);
        List<UserEntity> userList = new ArrayList<>();
        // 调用evalNodes（）方法获取XNode列表
        List<XNode> nodes = parser.evalNodes("/users/*");
        // 对XNode对象进行遍历，获取user相关信息
        for (XNode node : nodes) {
            UserEntity userEntity = new UserEntity();
            Long id = node.getLongAttribute("id");
            BeanUtils.setProperty(userEntity, "id", id);
            List<XNode> childNods = node.getChildren();
            for (XNode childNode : childNods) {
                    BeanUtils.setProperty(userEntity, childNode.getName(),
                            childNode.getStringBody());
            }
            userList.add(userEntity);
        }
        System.out.println(JSON.toJSONString(userList));
    }

}

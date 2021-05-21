package com.blog4java.mybatis.xpath;

import com.akulaku.platform.components.oss.StorageService;
import com.akulaku.platform.components.oss.entity.req.OssUploadReq;
import com.akulaku.platform.components.oss.entity.resp.OssUploadInfoResp;
import com.alibaba.fastjson.JSON;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XPathParserExample {

    @Test
    public void testXPathParser() throws Exception {
        Reader resource = Resources.getResourceAsReader("users.xml");
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


    @Test
    public void upload() throws FileNotFoundException {
        File file = new File("D:\\justforfun\\study-record\\技术书籍笔记\\Mybatis3源码深度解析\\素材\\TypeHandler 部分子类.png");
        OssUploadReq ossInfo = new OssUploadReq();
        ossInfo.setStorageName("tencent1");
        ossInfo.setFileName("111");
        OssUploadInfoResp url = StorageService.store(new FileInputStream(file), ossInfo);
    }
}

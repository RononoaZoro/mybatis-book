package com.luo.ibatis.scripting.xmltags;

import com.luo.ibatis.mapping.ArcherSqlSource;
import com.luo.ibatis.parsing.XNode;
import com.luo.ibatis.scripting.ArcherLanguageDriver;
import com.luo.ibatis.scripting.xmltags.ArcherXMLScriptBuilder;
import com.luo.ibatis.session.ArcherConfiguration;

public class ArcherXMLLanguageDriver implements ArcherLanguageDriver {
    @Override
    public ArcherSqlSource createSqlSource(ArcherConfiguration configuration, XNode script, Class<?> parameterType) {
        // 该方法用于解析XML文件中配置的SQL信息
        // 创建XMLScriptBuilder对象
        ArcherXMLScriptBuilder builder = new ArcherXMLScriptBuilder(configuration, script, parameterType);
        // 调用 XMLScriptBuilder对象parseScriptNode（）方法解析SQL资源
        return builder.parseScriptNode();
    }
}

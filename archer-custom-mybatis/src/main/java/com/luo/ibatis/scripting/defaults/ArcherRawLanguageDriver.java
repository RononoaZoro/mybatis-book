package com.luo.ibatis.scripting.defaults;

import com.luo.ibatis.builder.BuilderException;
import com.luo.ibatis.mapping.ArcherSqlSource;
import com.luo.ibatis.parsing.XNode;
import com.luo.ibatis.scripting.xmltags.ArcherXMLLanguageDriver;
import com.luo.ibatis.session.ArcherConfiguration;

public class ArcherRawLanguageDriver extends ArcherXMLLanguageDriver {


    @Override
    public ArcherSqlSource createSqlSource(ArcherConfiguration configuration, XNode script, Class<?> parameterType) {
        ArcherSqlSource source = super.createSqlSource(configuration, script, parameterType);
        checkIsNotDynamic(source);
        return source;
    }

//    @Override
//    public ArcherSqlSource createSqlSource(ArcherConfiguration configuration, String script, Class<?> parameterType) {
//        ArcherSqlSource source = super.createSqlSource(configuration, script, parameterType);
//        checkIsNotDynamic(source);
//        return source;
//    }


    private void checkIsNotDynamic(ArcherSqlSource source) {
        if (!ArcherRawSqlSource.class.equals(source.getClass())) {
            throw new BuilderException("Dynamic content is not allowed when using RAW language");
        }
    }
}

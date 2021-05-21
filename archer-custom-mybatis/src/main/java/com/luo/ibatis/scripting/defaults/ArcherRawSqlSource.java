package com.luo.ibatis.scripting.defaults;

import com.luo.ibatis.builder.ArcherSqlSourceBuilder;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherSqlSource;
import com.luo.ibatis.scripting.xmltags.ArcherDynamicContext;
import com.luo.ibatis.scripting.xmltags.SqlNode;
import com.luo.ibatis.session.ArcherConfiguration;

import java.util.HashMap;

public class ArcherRawSqlSource implements ArcherSqlSource {

    private final ArcherSqlSource sqlSource;

    public ArcherRawSqlSource(ArcherConfiguration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public ArcherRawSqlSource(ArcherConfiguration configuration, String sql, Class<?> parameterType) {
        ArcherSqlSourceBuilder sqlSourceParser = new ArcherSqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<String, Object>());
    }


    private static String getSql(ArcherConfiguration configuration, SqlNode rootSqlNode) {
        ArcherDynamicContext context = new ArcherDynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }

    @Override
    public ArcherBoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }
}

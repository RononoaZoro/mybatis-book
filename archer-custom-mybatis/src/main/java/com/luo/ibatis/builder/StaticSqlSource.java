/**
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.luo.ibatis.builder;

import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherSqlSource;
import com.luo.ibatis.mapping.ParameterMapping;
import com.luo.ibatis.session.ArcherConfiguration;

import java.util.List;

/**
 * @author Clinton Begin
 */
public class StaticSqlSource implements ArcherSqlSource {
    // Mapper解析后的sql内容
    private final String sql;
    // 参数映射信息
    private final List<ParameterMapping> parameterMappings;
    private final ArcherConfiguration configuration;

    public StaticSqlSource(ArcherConfiguration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(ArcherConfiguration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    @Override
    public ArcherBoundSql getBoundSql(Object parameterObject) {
        return new ArcherBoundSql(configuration, sql, parameterMappings, parameterObject);
    }

}

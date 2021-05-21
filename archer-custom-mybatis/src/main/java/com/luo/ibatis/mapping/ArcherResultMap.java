/**
 *    Copyright 2009-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.luo.ibatis.mapping;

import com.luo.ibatis.annotations.Param;
import com.luo.ibatis.builder.BuilderException;
import com.luo.ibatis.logging.ArcherLogFactory;
import com.luo.ibatis.logging.Log;
import com.luo.ibatis.reflection.Jdk;
import com.luo.ibatis.reflection.ParamNameUtil;
import com.luo.ibatis.session.ArcherConfiguration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * @author Clinton Begin
 */
public class ArcherResultMap {
  private ArcherConfiguration configuration;
  // <resultMap>标签id属性
  private String id;
  // <resultMap>标签type属性
  private Class<?> type;
  // <result>标签配置的映射信息
  private List<ArcherResultMapping> resultMappings;
  // <id>标签配置的主键映射信息
  private List<ArcherResultMapping> idResultMappings;
  // <constructor>标签配置的构造器映射信息
  private List<ArcherResultMapping> constructorResultMappings;
  // <result>标签配置的结果集映射信息
  private List<ArcherResultMapping> propertyResultMappings;
  // 存放所有映射的数据库字段信息,当使用columnPrefix配置字段前缀时，所有字段都会追加前缀
  private Set<String> mappedColumns;
  // 存放所有映射的属性信息
  private Set<String> mappedProperties;
  // <discriminator>标签配置的鉴别器信息
  private ArcherDiscriminator discriminator;
  // 是否有嵌套的<resultMap>
  private boolean hasNestedResultMaps;
  // 是否有存在嵌套查询
  private boolean hasNestedQueries;
  // autoMapping属性值，是否自动映射
  private Boolean autoMapping;

  private ArcherResultMap() {
  }

  public static class Builder {
    private static final Log log = ArcherLogFactory.getLog(Builder.class);

    private ArcherResultMap resultMap = new ArcherResultMap();

    public Builder(ArcherConfiguration configuration, String id, Class<?> type, List<ArcherResultMapping> resultMappings) {
      this(configuration, id, type, resultMappings, null);
    }

    public Builder(ArcherConfiguration configuration, String id, Class<?> type, List<ArcherResultMapping> resultMappings, Boolean autoMapping) {
      resultMap.configuration = configuration;
      resultMap.id = id;
      resultMap.type = type;
      resultMap.resultMappings = resultMappings;
      resultMap.autoMapping = autoMapping;
    }

    public Builder discriminator(ArcherDiscriminator discriminator) {
      resultMap.discriminator = discriminator;
      return this;
    }

    public Class<?> type() {
      return resultMap.type;
    }

    public ArcherResultMap build() {
      if (resultMap.id == null) {
        throw new IllegalArgumentException("ResultMaps must have an id");
      }
      resultMap.mappedColumns = new HashSet<String>();
      resultMap.mappedProperties = new HashSet<String>();
      // 將ResultMapping对象进行分类
      resultMap.idResultMappings = new ArrayList<ArcherResultMapping>();
      resultMap.constructorResultMappings = new ArrayList<ArcherResultMapping>();
      resultMap.propertyResultMappings = new ArrayList<ArcherResultMapping>();
      final List<String> constructorArgNames = new ArrayList<String>();
      for (ArcherResultMapping resultMapping : resultMap.resultMappings) {
        resultMap.hasNestedQueries = resultMap.hasNestedQueries || resultMapping.getNestedQueryId() != null;
        resultMap.hasNestedResultMaps = resultMap.hasNestedResultMaps || (resultMapping.getNestedResultMapId() != null && resultMapping.getResultSet() == null);
        final String column = resultMapping.getColumn();
        if (column != null) {
          resultMap.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
        } else if (resultMapping.isCompositeResult()) {
          for (ArcherResultMapping compositeResultMapping : resultMapping.getComposites()) {
            final String compositeColumn = compositeResultMapping.getColumn();
            if (compositeColumn != null) {
              resultMap.mappedColumns.add(compositeColumn.toUpperCase(Locale.ENGLISH));
            }
          }
        }
        final String property = resultMapping.getProperty();
        if(property != null) {
          resultMap.mappedProperties.add(property);
        }
        if (resultMapping.getFlags().contains(ResultFlag.CONSTRUCTOR)) {
          resultMap.constructorResultMappings.add(resultMapping);
          if (resultMapping.getProperty() != null) {
            constructorArgNames.add(resultMapping.getProperty());
          }
        } else {
          resultMap.propertyResultMappings.add(resultMapping);
        }
        if (resultMapping.getFlags().contains(ResultFlag.ID)) {
          resultMap.idResultMappings.add(resultMapping);
        }
      }
      if (resultMap.idResultMappings.isEmpty()) {
        resultMap.idResultMappings.addAll(resultMap.resultMappings);
      }
      if (!constructorArgNames.isEmpty()) {
        final List<String> actualArgNames = argNamesOfMatchingConstructor(constructorArgNames);
        if (actualArgNames == null) {
          throw new BuilderException("Error in result map '" + resultMap.id
              + "'. Failed to find a constructor in '"
              + resultMap.getType().getName() + "' by arg names " + constructorArgNames
              + ". There might be more info in debug log.");
        }
        Collections.sort(resultMap.constructorResultMappings, new Comparator<ArcherResultMapping>() {
          @Override
          public int compare(ArcherResultMapping o1, ArcherResultMapping o2) {
            int paramIdx1 = actualArgNames.indexOf(o1.getProperty());
            int paramIdx2 = actualArgNames.indexOf(o2.getProperty());
            return paramIdx1 - paramIdx2;
          }
        });
      }
      // ResultMap创建完毕后，属性不允许修改
      resultMap.resultMappings = Collections.unmodifiableList(resultMap.resultMappings);
      resultMap.idResultMappings = Collections.unmodifiableList(resultMap.idResultMappings);
      resultMap.constructorResultMappings = Collections.unmodifiableList(resultMap.constructorResultMappings);
      resultMap.propertyResultMappings = Collections.unmodifiableList(resultMap.propertyResultMappings);
      resultMap.mappedColumns = Collections.unmodifiableSet(resultMap.mappedColumns);
      return resultMap;
    }

    private List<String> argNamesOfMatchingConstructor(List<String> constructorArgNames) {
      Constructor<?>[] constructors = resultMap.type.getDeclaredConstructors();
      for (Constructor<?> constructor : constructors) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        if (constructorArgNames.size() == paramTypes.length) {
          List<String> paramNames = getArgNames(constructor);
          if (constructorArgNames.containsAll(paramNames)
              && argTypesMatch(constructorArgNames, paramTypes, paramNames)) {
            return paramNames;
          }
        }
      }
      return null;
    }

    private boolean argTypesMatch(final List<String> constructorArgNames,
        Class<?>[] paramTypes, List<String> paramNames) {
      for (int i = 0; i < constructorArgNames.size(); i++) {
        Class<?> actualType = paramTypes[paramNames.indexOf(constructorArgNames.get(i))];
        Class<?> specifiedType = resultMap.constructorResultMappings.get(i).getJavaType();
        if (!actualType.equals(specifiedType)) {
          if (log.isDebugEnabled()) {
            log.debug("While building result map '" + resultMap.id
                + "', found a constructor with arg names " + constructorArgNames
                + ", but the type of '" + constructorArgNames.get(i)
                + "' did not match. Specified: [" + specifiedType.getName() + "] Declared: ["
                + actualType.getName() + "]");
          }
          return false;
        }
      }
      return true;
    }

    private List<String> getArgNames(Constructor<?> constructor) {
      List<String> paramNames = new ArrayList<String>();
      List<String> actualParamNames = null;
      final Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
      int paramCount = paramAnnotations.length;
      for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
        String name = null;
        for (Annotation annotation : paramAnnotations[paramIndex]) {
          if (annotation instanceof Param) {
            name = ((Param) annotation).value();
            break;
          }
        }
        if (name == null && resultMap.configuration.isUseActualParamName() && Jdk.parameterExists) {
          if (actualParamNames == null) {
            actualParamNames = ParamNameUtil.getParamNames(constructor);
          }
          if (actualParamNames.size() > paramIndex) {
            name = actualParamNames.get(paramIndex);
          }
        }
        paramNames.add(name != null ? name : "arg" + paramIndex);
      }
      return paramNames;
    }
  }

  public String getId() {
    return id;
  }

  public boolean hasNestedResultMaps() {
    return hasNestedResultMaps;
  }

  public boolean hasNestedQueries() {
    return hasNestedQueries;
  }

  public Class<?> getType() {
    return type;
  }

  public List<ArcherResultMapping> getResultMappings() {
    return resultMappings;
  }

  public List<ArcherResultMapping> getConstructorResultMappings() {
    return constructorResultMappings;
  }

  public List<ArcherResultMapping> getPropertyResultMappings() {
    return propertyResultMappings;
  }

  public List<ArcherResultMapping> getIdResultMappings() {
    return idResultMappings;
  }

  public Set<String> getMappedColumns() {
    return mappedColumns;
  }

  public Set<String> getMappedProperties() {
    return mappedProperties;
  }

  public ArcherDiscriminator getDiscriminator() {
    return discriminator;
  }

  public void forceNestedResultMaps() {
    hasNestedResultMaps = true;
  }
  
  public Boolean getAutoMapping() {
    return autoMapping;
  }

}

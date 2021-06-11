/**
 *    Copyright 2009-2018 the original author or authors.
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
package com.luo.ibatis.scripting;

import com.luo.ibatis.executor.parameter.ArcherParameterHandler;
import com.luo.ibatis.mapping.ArcherBoundSql;
import com.luo.ibatis.mapping.ArcherMappedStatement;
import com.luo.ibatis.mapping.ArcherSqlSource;
import com.luo.ibatis.parsing.XNode;
import com.luo.ibatis.scripting.defaults.ArcherRawLanguageDriver;
import com.luo.ibatis.session.ArcherConfiguration;
import org.junit.Test;

import static com.googlecode.catchexception.apis.BDDCatchException.caughtException;
import static com.googlecode.catchexception.apis.BDDCatchException.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Kazuki Shimizu
 */
public class LanguageDriverRegistryTest {

  private LanguageDriverRegistry registry = new LanguageDriverRegistry();

  @Test
  public void registerByType() {
    registry.register(ArcherRawLanguageDriver.class);
    ArcherLanguageDriver driver = registry.getDriver(ArcherRawLanguageDriver.class);

    assertThat(driver).isInstanceOf(ArcherRawLanguageDriver.class);
  }

  @Test
  public void registerByTypeSameType() {
    registry.register(ArcherRawLanguageDriver.class);
    ArcherLanguageDriver driver = registry.getDriver(ArcherRawLanguageDriver.class);

    registry.register(ArcherRawLanguageDriver.class);

    assertThat(driver).isSameAs(registry.getDriver(ArcherRawLanguageDriver.class));
  }

  @Test
  public void registerByTypeNull() {
    when(registry).register((Class<? extends ArcherLanguageDriver>) null);
    then(caughtException()).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("null is not a valid Language Driver");
  }

  @Test
  public void registerByTypeDoesNotCreateNewInstance() {
    when(registry).register(PrivateLanguageDriver.class);
    then(caughtException()).isInstanceOf(ScriptingException.class)
      .hasMessage("Failed to load language driver for com.luo.ibatis.scripting.LanguageDriverRegistryTest$PrivateLanguageDriver");
  }

  @Test
  public void registerByInstance() {
    registry.register(new PrivateLanguageDriver());
    ArcherLanguageDriver driver = registry.getDriver(PrivateLanguageDriver.class);

    assertThat(driver).isInstanceOf(PrivateLanguageDriver.class);
  }

  @Test
  public void registerByInstanceSameType() {
    registry.register(new PrivateLanguageDriver());
    ArcherLanguageDriver driver = registry.getDriver(PrivateLanguageDriver.class);

    registry.register(new PrivateLanguageDriver());

    assertThat(driver).isSameAs(registry.getDriver(PrivateLanguageDriver.class));
  }

  @Test
  public void registerByInstanceNull() {
    when(registry).register((ArcherLanguageDriver) null);
    then(caughtException()).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("null is not a valid Language Driver");
  }

  @Test
  public void setDefaultDriverClass() {
    registry.setDefaultDriverClass(ArcherRawLanguageDriver.class);
    assertThat(registry.getDefaultDriverClass() == ArcherRawLanguageDriver.class).isTrue();
    assertThat(registry.getDefaultDriver()).isInstanceOf(ArcherRawLanguageDriver.class);
  }

  static private class PrivateLanguageDriver implements ArcherLanguageDriver {

    @Override
    public ArcherParameterHandler createParameterHandler(ArcherMappedStatement mappedStatement, Object parameterObject, ArcherBoundSql boundSql) {
      return null;
    }

    @Override
    public ArcherSqlSource createSqlSource(ArcherConfiguration configuration, XNode script, Class<?> parameterType) {
      return null;
    }

//    @Override
//    public ArcherSqlSource createSqlSource(ArcherConfiguration configuration, String script, Class<?> parameterType) {
//      return null;
//    }
  }

}

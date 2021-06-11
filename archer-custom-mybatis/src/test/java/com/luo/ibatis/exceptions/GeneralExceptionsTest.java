/**
 *    Copyright 2009-2015 the original author or authors.
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
package com.luo.ibatis.exceptions;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeneralExceptionsTest {

  private static final String EXPECTED_MESSAGE = "Test Message";
  private static final Exception EXPECTED_CAUSE = new Exception("Nested Exception");

  @Test
  public void should() {
    RuntimeException thrown = ExceptionFactory.wrapException(EXPECTED_MESSAGE, EXPECTED_CAUSE);
    assertTrue("Exception should be wrapped in RuntimeSqlException.", thrown instanceof PersistenceException);
    testThrowException(thrown);
  }

//  @Test
//  public void shouldInstantiateAndThrowAllCustomExceptions() throws Exception {
//    Class<?>[] exceptionTypes = {
//        BindingException.class,
//        CacheException.class,
//        DataSourceException.class,
//        ExecutorException.class,
//        LogException.class,
//        ParsingException.class,
//        BuilderException.class,
//        PluginException.class,
//        ReflectionException.class,
//        PersistenceException.class,
//        SqlSessionException.class,
//        TransactionException.class,
//        TypeException.class,
//        ScriptingException.class
//    };
//    for (Class<?> exceptionType : exceptionTypes) {
//      testExceptionConstructors(exceptionType);
//    }
//
//  }

  private void testExceptionConstructors(Class<?> exceptionType) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    Exception e = (Exception) exceptionType.newInstance();
    testThrowException(e);
    e = (Exception) exceptionType.getConstructor(String.class).newInstance(EXPECTED_MESSAGE);
    testThrowException(e);
    e = (Exception) exceptionType.getConstructor(String.class, Throwable.class).newInstance(EXPECTED_MESSAGE, EXPECTED_CAUSE);
    testThrowException(e);
    e = (Exception) exceptionType.getConstructor(Throwable.class).newInstance(EXPECTED_CAUSE);
    testThrowException(e);
  }

  private void testThrowException(Exception thrown) {
    try {
      throw thrown;
    } catch (Exception caught) {
      assertEquals(thrown.getMessage(), caught.getMessage());
      assertEquals(thrown.getCause(), caught.getCause());
    }
  }

}

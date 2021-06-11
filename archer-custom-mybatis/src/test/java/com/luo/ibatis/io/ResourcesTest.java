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
package com.luo.ibatis.io;

import com.luo.ibatis.BaseDataTest;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import static org.junit.Assert.*;

public class ResourcesTest extends BaseDataTest {

  private static final ClassLoader CLASS_LOADER = ResourcesTest.class.getClassLoader();

  @Test
  public void shouldGetUrlForResource() throws Exception {
    URL url = ArcherResources.getResourceURL(JPETSTORE_PROPERTIES);
    assertTrue(url.toString().endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldGetUrlAsProperties() throws Exception {
    URL url = ArcherResources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Properties props = ArcherResources.getUrlAsProperties(url.toString());
    assertNotNull(props.getProperty("driver"));
  }

  @Test
  public void shouldGetResourceAsProperties() throws Exception {
    Properties props = ArcherResources.getResourceAsProperties(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(props.getProperty("driver"));
  }

  @Test
  public void shouldGetUrlAsStream() throws Exception {
    URL url = ArcherResources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    try (InputStream in = ArcherResources.getUrlAsStream(url.toString())) {
      assertNotNull(in);
    }
  }

  @Test
  public void shouldGetUrlAsReader() throws Exception {
    URL url = ArcherResources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    try (Reader in = ArcherResources.getUrlAsReader(url.toString())) {
      assertNotNull(in);
    }
  }

  @Test
  public void shouldGetResourceAsStream() throws Exception {
    try (InputStream in = ArcherResources.getResourceAsStream(CLASS_LOADER, JPETSTORE_PROPERTIES)) {
      assertNotNull(in);
    }
  }

  @Test
  public void shouldGetResourceAsReader() throws Exception {
    try(Reader in = ArcherResources.getResourceAsReader(CLASS_LOADER, JPETSTORE_PROPERTIES)) {
      assertNotNull(in);
    }
  }

  @Test
  public void shouldGetResourceAsFile() throws Exception {
    File file = ArcherResources.getResourceAsFile(JPETSTORE_PROPERTIES);
    assertTrue(file.getAbsolutePath().replace('\\', '/').endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldGetResourceAsFileWithClassloader() throws Exception {
    File file = ArcherResources.getResourceAsFile(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertTrue(file.getAbsolutePath().replace('\\', '/').endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldGetResourceAsPropertiesWithOutClassloader() throws Exception {
    Properties file = ArcherResources.getResourceAsProperties(JPETSTORE_PROPERTIES);
    assertNotNull(file);
  }

  @Test
  public void shouldGetResourceAsPropertiesWithClassloader() throws Exception {
    Properties file = ArcherResources.getResourceAsProperties(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(file);
  }

  @Test
  public void shouldAllowDefaultClassLoaderToBeSet() {
    ArcherResources.setDefaultClassLoader(this.getClass().getClassLoader());
    assertEquals(this.getClass().getClassLoader(), ArcherResources.getDefaultClassLoader());
  }

  @Test
  public void shouldAllowDefaultCharsetToBeSet() {
    ArcherResources.setCharset(Charset.defaultCharset());
    assertEquals(Charset.defaultCharset(), ArcherResources.getCharset());
  }

  @Test
  public void shouldGetClassForName() throws Exception {
    Class<?> clazz = ArcherResources.classForName(ResourcesTest.class.getName());
    assertNotNull(clazz);
  }

  @Test(expected = ClassNotFoundException.class)
  public void shouldNotFindThisClass() throws ClassNotFoundException {
    ArcherResources.classForName("some.random.class.that.does.not.Exist");
  }

  @Test
  public void shouldGetReader() throws IOException {

    // save the value
    Charset charset = ArcherResources.getCharset();

    // charset
    ArcherResources.setCharset(Charset.forName("US-ASCII"));
    assertNotNull(ArcherResources.getResourceAsReader(JPETSTORE_PROPERTIES));

    // no charset
    ArcherResources.setCharset(null);
    assertNotNull(ArcherResources.getResourceAsReader(JPETSTORE_PROPERTIES));

    // clean up
    ArcherResources.setCharset(charset);

  }

  @Test
  public void shouldGetReaderWithClassLoader() throws IOException {

    // save the value
    Charset charset = ArcherResources.getCharset();

    // charset
    ArcherResources.setCharset(Charset.forName("US-ASCII"));
    assertNotNull(ArcherResources.getResourceAsReader(getClass().getClassLoader(), JPETSTORE_PROPERTIES));

    // no charset
    ArcherResources.setCharset(null);
    assertNotNull(ArcherResources.getResourceAsReader(getClass().getClassLoader(), JPETSTORE_PROPERTIES));

    // clean up
    ArcherResources.setCharset(charset);

  }

  @Test
  public void stupidJustForCoverage() {
    assertNotNull(new ArcherResources());
  }
}

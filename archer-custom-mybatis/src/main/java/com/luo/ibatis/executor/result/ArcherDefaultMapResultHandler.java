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
package com.luo.ibatis.executor.result;

import com.luo.ibatis.reflection.MetaObject;
import com.luo.ibatis.reflection.ReflectorFactory;
import com.luo.ibatis.reflection.factory.ObjectFactory;
import com.luo.ibatis.reflection.wrapper.ObjectWrapperFactory;
import com.luo.ibatis.session.ArcherResultContext;
import com.luo.ibatis.session.ArcherResultHandler;

import java.util.Map;

/**
 * @author Clinton Begin
 */
public class ArcherDefaultMapResultHandler<K, V> implements ArcherResultHandler<V> {

  private final Map<K, V> mappedResults;
  private final String mapKey;
  private final ObjectFactory objectFactory;
  private final ObjectWrapperFactory objectWrapperFactory;
  private final ReflectorFactory reflectorFactory;

  @SuppressWarnings("unchecked")
  public ArcherDefaultMapResultHandler(String mapKey, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    this.objectFactory = objectFactory;
    this.objectWrapperFactory = objectWrapperFactory;
    this.reflectorFactory = reflectorFactory;
    this.mappedResults = objectFactory.create(Map.class);
    this.mapKey = mapKey;
  }

  @Override
  public void handleResult(ArcherResultContext<? extends V> context) {
    final V value = context.getResultObject();
    final MetaObject mo = MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
    // TODO is that assignment always true?
    final K key = (K) mo.getValue(mapKey);
    mappedResults.put(key, value);
  }

  public Map<K, V> getMappedResults() {
    return mappedResults;
  }
}

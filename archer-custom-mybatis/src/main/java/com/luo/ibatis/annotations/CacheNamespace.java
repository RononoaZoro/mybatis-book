/**
 *    Copyright 2009-2016 the original author or authors.
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
package com.luo.ibatis.annotations;

import com.luo.ibatis.cache.ArcherCache;
import com.luo.ibatis.cache.decorators.ArcherLruCache;
import com.luo.ibatis.cache.impl.ArcherPerpetualCache;

import java.lang.annotation.*;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CacheNamespace {
  Class<? extends ArcherCache> implementation() default ArcherPerpetualCache.class;

  Class<? extends ArcherCache> eviction() default ArcherLruCache.class;

  long flushInterval() default 0;

  int size() default 1024;

  boolean readWrite() default true;
  
  boolean blocking() default false;

  /**
   * Property values for a implementation object.
   * @since 3.4.2
   */
  Property[] properties() default {};
  
}

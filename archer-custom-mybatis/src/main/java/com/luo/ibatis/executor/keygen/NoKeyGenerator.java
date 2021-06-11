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
package com.luo.ibatis.executor.keygen;

import com.luo.ibatis.executor.ArcherExecutor;
import com.luo.ibatis.mapping.ArcherMappedStatement;

import java.sql.Statement;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class NoKeyGenerator implements KeyGenerator {

    /**
     * A shared instance.
     *
     * @since 3.4.3
     */
    public static final NoKeyGenerator INSTANCE = new NoKeyGenerator();

    @Override
    public void processBefore(ArcherExecutor executor, ArcherMappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }

    @Override
    public void processAfter(ArcherExecutor executor, ArcherMappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }

}

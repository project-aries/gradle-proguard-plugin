/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aries.gradle.proguard.plugin;

/*

    Constants to use globally across plugin code base.

*/
public class ProguardConstants {

    public static final def JAVA_LIBS = ["${System.getProperty('java.home')}/lib/rt.jar",
                                         "${System.getProperty('java.home')}/lib/jsse.jar",
                                         "${System.getProperty('java.home')}/lib/jce.jar"].asImmutable()

    private ProguardConstants() {
        throw new UnsupportedOperationException('Cannot instantiate instance of this class');
    }
}

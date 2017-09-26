/*
 *      Licensed to the Apache Software Foundation (ASF) under one or more
 *      contributor license agreements.  See the NOTICE file distributed with
 *      this work for additional information regarding copyright ownership.
 *      The ASF licenses this file to You under the Apache License, Version 2.0
 *      (the "License"); you may not use this file except in compliance with
 *      the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package org.jboss.budapest.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * A class that contains necessary utility methods
 *
 * @author <a href="mailto:tw.techlist@gmail.com">Tyronne Wickramarathne</a>
 */


public final class SetupManager {

    private SetupManager(){}

    public static String getProperty(String key) {
        Properties properties =  new Properties();
        try( FileReader fileReader = new FileReader("budapest.properties") ){
            properties.load(fileReader);
        } catch(IOException e){
            e.printStackTrace();
        }
        return properties.getProperty(key).toString();
    }
}

/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.xxl.job.spring.boot.dto;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class MapUtil {
    public MapUtil() {
    }

    public static MultiValueMap<String, String> obj2Map(Object obj) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap();
        Field[] fields = obj.getClass().getDeclaredFields();
        int i = 0;

        for(int len = fields.length; i < len; ++i) {
            String varName = fields[i].getName();

            try {
                boolean accessFlag = fields[i].isAccessible();
                fields[i].setAccessible(true);
                Object o = fields[i].get(obj);
                if (o != null) {
                    map.put(varName, Collections.singletonList(o.toString()));
                }

                fields[i].setAccessible(accessFlag);
            } catch (IllegalArgumentException var8) {
                var8.printStackTrace();
            } catch (IllegalAccessException var9) {
                var9.printStackTrace();
            }
        }

        return map;
    }

    public static MultiValueMap<String, String> obj2MapWithNull(Object obj) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap();
        Field[] fields = obj.getClass().getDeclaredFields();
        int i = 0;

        for(int len = fields.length; i < len; ++i) {
            String varName = fields[i].getName();

            try {
                boolean accessFlag = fields[i].isAccessible();
                fields[i].setAccessible(true);
                Object o = fields[i].get(obj);
                if (o != null) {
                    map.put(varName, Collections.singletonList(o.toString()));
                } else {
                    map.put(varName, (List<String>)null);
                }

                fields[i].setAccessible(accessFlag);
            } catch (IllegalArgumentException var8) {
                var8.printStackTrace();
            } catch (IllegalAccessException var9) {
                var9.printStackTrace();
            }
        }

        return map;
    }

    public static MultiValueMap<String, String> obj2MapWithString(Object obj) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap();
        Field[] fields = obj.getClass().getDeclaredFields();
        int i = 0;

        for(int len = fields.length; i < len; ++i) {
            String varName = fields[i].getName();

            try {
                boolean accessFlag = fields[i].isAccessible();
                fields[i].setAccessible(true);
                Object o = fields[i].get(obj);
                if (o != null) {
                    map.put(varName, Collections.singletonList(o.toString()));
                } else {
                    map.put(varName, Collections.singletonList(""));
                }

                fields[i].setAccessible(accessFlag);
            } catch (IllegalArgumentException var8) {
                var8.printStackTrace();
            } catch (IllegalAccessException var9) {
                var9.printStackTrace();
            }
        }

        return map;
    }
}
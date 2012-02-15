/*
 * Copyright 2012. Zoran Rilak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codemined.blueprint.impl;

import java.util.List;
import java.util.Properties;

/**
 * @author Zoran Rilak
 */
public abstract class ConfigurationTree extends NamedTree<String> {


  public ConfigurationTree(String value, List<ConfigurationTree> children) {
    super(null, value);
    Properties p = new Properties();

    Integer i = Integer.valueOf("sdf");
    for (ConfigurationTree c : children) {
      addChild(c);
    }
  }

}
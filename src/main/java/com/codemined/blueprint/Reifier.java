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

package com.codemined.blueprint;

import java.util.*;

/**
 * @author Zoran Rilak
 */
public class Reifier {

  public static <E> Collection<E> reifyCollection(Class<?> type) {
    if (! type.isInterface()) {
      throw new BlueprintException("Only interfaces can be reified");
    }

    if (type.isAssignableFrom(LinkedList.class)) {
      return new LinkedList<E>();
    }
    if (type.isAssignableFrom(TreeSet.class)) {
      return new TreeSet<E>();
    }

    throw new BlueprintException("Don't know how to reify " + type);
  }

  public static <E> Map<String, E> reifyStringMap() {
    return new HashMap<String, E>();
  }

}

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

  /* Stub(Interface, ConfigurationSource) or
     Stub(Interface, Prefix)
     ================================================================
     - Interface: class that this Stub will be proxying for
     - ConfigurationSource: source to query for configuration key-value pairs
     - RootPath: ConfigurationSource-specific "root path" for this stub to map onto
     - Stub implements some common behaviors and caches/generates the rest.
     - Stub caching strategy might be configurable if we allow the configurations to change
       (but keep validations in mind: do we re-run them, and if so, when?)

   */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * - Implements common fluff from Object(): toString(), equals(), hashCode(), ...
 * - Scans Interface for contained interfaces and methods
 * - Sub-interfaces are processed by creating and caching proxy classes
 * - Methods are processed thus:
 *   - Simple return type (Integer, Boolean...) is passed on to the deserializer
 *   - Sub-interface return type creates/reuses a child instance of BlueprintStub
 *   - Collection return type
 * - Weak references will be used for caching large values, especially if we allow
 *   for polymorphic deserialization: <T> T url(Class<T> deserializeAs)
 * >>> Check how Guice, Jersey and Jackson capture generic types.
 *
 * @author Zoran Rilak
 * @version 0.1
 * @since 0.1
 */
class Stub<I> implements InvocationHandler {
  private final Class<I> iface;
  private final Source source;
  private final Deserializer deserializer;
  private final String prefix;
  private final Map<MethodInvocation, Object> cache;
  private final I proxy;


  public Stub(Class<I> iface, Source source, String prefix) {
    this.iface = iface;
    this.source = source;
    this.deserializer = new Deserializer(source, iface.getClassLoader());
    this.prefix = prefix;
    this.cache = Collections.synchronizedMap(new WeakHashMap<MethodInvocation, Object>());
    this.proxy = createProxy();
  }


  public I getProxy() {
    return proxy;
  }

  /* Methods from InvocationHandler --------------------------------- */

  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
          throws Throwable {
    // route methods not declared on the blueprint interface to self
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(this, args);
    }

    // Values are cached by (method, args) pairs to support type hinting in arguments.
    final String key = source.composePath(prefix, method.getName());
    try {
      final MethodInvocation invocation = new MethodInvocation(method, args);
      Object o;
      if ((o = cache.get(invocation)) == null) {
        o = deserializer.deserialize(
                invocation.getReturnType(),
                invocation.getHintedType(),
                key);
        cache.put(invocation, o);
      }
      return o;
      
    } catch (BlueprintException e) {
      throw new BlueprintException(buildExceptionMessage(e.getMessage(), key, method, args), e);
    }
  }


  /* Methods from Object -------------------------------------------- */


  @Override
  public boolean equals(Object o) {
    return proxy == o;
  }


  @Override
  public int hashCode() {
    int result = iface != null ? iface.hashCode() : 0;
    result = 31 * result + (source != null ? source.hashCode() : 0);
    return result;
  }

  
  @Override
  public String toString() {
    return "[" + iface.getName() + " blueprint]";
  }


  /* Privates ------------------------------------------------------- */


  private I createProxy() {
    try {
      return iface.cast(Proxy.newProxyInstance(
              iface.getClassLoader(), new Class[]{ iface, BlueprintProxy.class }, this));
    } catch (IllegalArgumentException e) {
      throw new BlueprintException("Error creating proxy instance for " + iface.getName(), e);
    } catch (ClassCastException e) {
      throw new BlueprintException("Error casting proxy instance to " + iface.getName(), e);
    }
  }


  private String buildExceptionMessage(String cause, String key, Method method, Object[] args) {
    final StringBuilder sb = new StringBuilder(cause);
    sb.append(", in class ").append(iface.getName());
    sb.append(", method ").append(method.getName()).append('(');
    if (args != null && args.length > 0) {
      int commas = args.length - 1;
      for (Object arg : args) {
        sb.append(arg.getClass().getCanonicalName());
        if (commas > 0) {
          sb.append(", ");
          commas--;
        }
      }
    }
    sb.append(')');
    sb.append(", key \"").append(key).append('"');
    return sb.toString();
  }

}

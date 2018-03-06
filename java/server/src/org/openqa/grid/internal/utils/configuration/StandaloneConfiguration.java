// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.grid.internal.utils.configuration;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

import org.openqa.grid.common.exception.GridConfigurationException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StandaloneConfiguration {
  public static final String DEFAULT_STANDALONE_CONFIG_FILE = "org/openqa/grid/common/defaults/DefaultStandalone.json";

  /*
   * IMPORTANT - Keep these constant values in sync with the ones specified in
   * 'defaults/DefaultStandalone.json'  -- if for no other reasons documentation & consistency.
   */

  /**
   * Default client timeout
   */
  @VisibleForTesting
  static final Integer DEFAULT_TIMEOUT = 1800;

  /**
   * Default browser timeout
   */
  @VisibleForTesting
  static final Integer DEFAULT_BROWSER_TIMEOUT = 0;

  /**
   * Default standalone role
   */
  @VisibleForTesting
  static final String DEFAULT_ROLE = "standalone";

  /**
   * Default standalone port
   */
  @VisibleForTesting
  static final Integer DEFAULT_PORT = 4444;

  /**
   * Default state of LogeLevel.FINE log output toggle
   */
  @VisibleForTesting
  static final Boolean DEFAULT_DEBUG_TOGGLE = false;


  /*
   * config parameters which do not serialize to json
   */
  @Expose( serialize = false )
  // initially defaults to false from boolean primitive type
  private boolean avoidProxy;

  @Expose( serialize = false )
  // initially defaults to false from boolean primitive type
  private boolean browserSideLog;

  @Expose( serialize = false )
  // initially defaults to false from boolean primitive type
  private boolean captureLogsOnQuit;

  /*
   * config parameters which serialize and deserialize to/from json
   */

  /**
   * Browser timeout. Default 0 (indefinite wait).
   */
  @Expose
  public Integer browserTimeout = DEFAULT_BROWSER_TIMEOUT;

  /**
   * Enable {@code LogLevel.FINE} log messages. Default {@code false}.
   */
  @Expose
  public Boolean debug = DEFAULT_DEBUG_TOGGLE;

  /**
   *   Max threads for Jetty. Defaults to {@code null}.
   */
  @Expose
  public Integer jettyMaxThreads;

  /**
   *   Filename to use for logging. Defaults to {@code null}.
   */
  @Expose
  public String log;

  /**
   * Port to bind to. Default determined by configuration type.
   */
  @Expose
  public Integer port = DEFAULT_PORT;

  /**
   * Server role. Default determined by configuration type.
   */
  @Expose
  public String role = DEFAULT_ROLE;

  /**
   * Client timeout. Default 1800 sec.
   */
  @Expose
  public Integer timeout = DEFAULT_TIMEOUT;

  /**
   * Creates a new configuration using the default values.
   */
  public StandaloneConfiguration() {
    // nothing to do.
  }

  /**
   * @param filePath node config json file to load configuration from
   */
  public static StandaloneConfiguration loadFromJSON(String filePath) {
    return loadFromJSON(loadJSONFromResourceOrFile(filePath));
  }

  /**
   * @param json JsonObject to load configuration from
   */
  public static StandaloneConfiguration loadFromJSON(JsonObject json) {
    try {
      GsonBuilder builder = new GsonBuilder();
      StandaloneConfiguration config =
        builder.excludeFieldsWithoutExposeAnnotation().create().fromJson(json, StandaloneConfiguration.class);
      return config;
    } catch (Throwable e) {
      throw new GridConfigurationException("Error with the JSON of the config : " + e.getMessage(),
                                           e);
    }
  }

  /**
   * copy another configuration's values into this one if they are set.
   */
  public void merge(StandaloneConfiguration other) {
    if (other == null) {
      return;
    }

    if (isMergeAble(other.browserTimeout, browserTimeout)) {
      browserTimeout = other.browserTimeout;
    }
    if (isMergeAble(other.jettyMaxThreads, jettyMaxThreads)) {
      jettyMaxThreads = other.jettyMaxThreads;
    }
    if (isMergeAble(other.timeout, timeout)) {
      timeout = other.timeout;
    }
    // role, port, log, debug, version, enablePassThrough, and help are not merged, they are only consumed by the
    // immediately running process and should never affect a remote
  }

  /**
   * Determines if one object can be merged onto another object. Checks for {@code null},
   * and empty (Collections & Maps) to make decision.
   *
   * @param other the object to merge. must be the same type as the 'target'
   * @param target the object to merge on to. must be the same type as the 'other'
   * @return whether the 'other' can be merged onto the 'target'
   */
  protected boolean isMergeAble(Object other, Object target) {
    // don't merge a null value
    if (other == null) {
      return false;
    } else {
      // allow any non-null value to merge over a null target.
      if (target == null) {
        return true;
      }
    }

    // we know we have two objects with value.. Make sure the types are the same and
    // perform additional checks.

    if (! target.getClass().getSuperclass().getTypeName()
        .equals(other.getClass().getSuperclass().getTypeName())) {
      return false;
    }

    if (target instanceof Collection) {
      return !((Collection<?>) other).isEmpty();
    }

    if (target instanceof Map) {
      return !((Map<?, ?>) other).isEmpty();
    }

    return true;
  }

  public String toString(String format) {
    StringBuilder sb = new StringBuilder();
    sb.append(toString(format, "browserTimeout", browserTimeout));
    sb.append(toString(format, "debug", debug));
    sb.append(toString(format, "jettyMaxThreads", jettyMaxThreads));
    sb.append(toString(format, "log", log));
    sb.append(toString(format, "port", port));
    sb.append(toString(format, "role", role));
    sb.append(toString(format, "timeout", timeout));
    return sb.toString();
  }

  @Override
  public String toString() {
    return toString(" -%1$s %2$s");
  }

  public StringBuilder toString(String format, String name, Object value) {
    StringBuilder sb = new StringBuilder();
    List<?> iterator;
    if (value instanceof List) {
      iterator = (List<?>)value;
    } else {
      iterator = Arrays.asList(value);
    }
    for (Object v : iterator) {
      if (v != null &&
          !(v instanceof Map && ((Map<?, ?>) v).isEmpty()) &&
          !(v instanceof Collection && ((Collection<?>) v).isEmpty())) {
        sb.append(String.format(format, name, v));
      }
    }
    return sb;
  }

  /**
   * Return a JsonElement representation of the configuration. Does not serialize nulls.
   */
  public JsonElement toJson() {
    GsonBuilder builder = new GsonBuilder();
    addJsonTypeAdapter(builder);
    //Note: it's important that nulls ARE NOT serialized, for backwards compatibility
    return builder.excludeFieldsWithoutExposeAnnotation().create().toJsonTree(this);
  }

  protected void addJsonTypeAdapter(GsonBuilder builder) {
    // no default implementation
  }

  /**
   * load a JSON file from the resource or file system.
   *
   * @param resource file or jar resource location
   * @return A JsonObject representing the passed resource argument.
   */
  protected static JsonObject loadJSONFromResourceOrFile(String resource) {
    try {
      return new JsonParser().parse(readFileOrResource(resource)).getAsJsonObject();
    } catch (JsonSyntaxException e) {
      throw new GridConfigurationException("Wrong format for the JSON input : " + e.getMessage(), e);
    }
  }

  private static String readFileOrResource(String resource) {
    Stream<Function<String, InputStream>> suppliers = Stream.of(
        (path) -> {
          try {
            return new FileInputStream(path);
          } catch (FileNotFoundException e) {
            return null;
          } },
        (path) -> Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("org/openqa/grid/common/" + path),
        (path) -> Thread.currentThread().getContextClassLoader().getResourceAsStream(path)
    );

    InputStream in = suppliers
        .map(supplier -> supplier.apply(resource))
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(() -> new RuntimeException(resource + " is not a valid resource."));

    try(BufferedReader buffreader = new BufferedReader(new InputStreamReader(in))) {
      return buffreader.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new GridConfigurationException("Cannot read file or resource " + resource + ", " + e.getMessage(), e);
    } finally {
      try {
        in.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}

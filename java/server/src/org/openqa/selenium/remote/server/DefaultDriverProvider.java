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
package org.openqa.selenium.remote.server;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This driver provider uses reflection to find and call a driver constructor that accepts
 * a parameter of Capabilities type.
 */
public class DefaultDriverProvider implements DriverProvider {

  private static final Logger LOG = Logger.getLogger(DefaultDriverProvider.class.getName());

  private Capabilities capabilities;
  private Class<? extends WebDriver> driverClass;
  private String driverClassName;

  public DefaultDriverProvider(Capabilities capabilities, Class<? extends WebDriver> driverClass) {
    this.capabilities = new DesiredCapabilities(capabilities);
    this.driverClass = driverClass;
  }

  public DefaultDriverProvider(Capabilities capabilities, String driverClassName) {
    this.capabilities = new DesiredCapabilities(capabilities);
    this.driverClassName = driverClassName;
  }

  @Override
  public Capabilities getProvidedCapabilities() {
    return capabilities;
  }

  /**
   * Checks that driver class can be loaded.
   */
  @Override
  public boolean canCreateDriverInstances() {
    return getDriverClass() != null;
  }

  /**
   * Checks that the browser name set in the provided capabilities matches the browser name
   * set in the desired capabilities.
   * @param capabilities The desired capabilities
   * @return true if the browser name is the same, false otherwise
   */
  @Override
  public boolean canCreateDriverInstanceFor(Capabilities capabilities) {
    return this.capabilities.getBrowserName().equals(capabilities.getBrowserName());
  }

  private Class<? extends WebDriver> getDriverClass() {
    if (driverClass != null) {
      return driverClass;
    }
    try {
      return Class.forName(driverClassName).asSubclass(WebDriver.class);
    } catch (ClassNotFoundException e) {
      LOG.log(Level.INFO, "Driver class not found: " + driverClassName);
      return null;
    } catch (NoClassDefFoundError e) {
      LOG.log(Level.INFO, "Driver class not found: " + driverClassName);
      return null;
    } catch (UnsupportedClassVersionError e) {
      LOG.log(Level.INFO, "Driver class is built for higher Java version: " + driverClassName);
      return null;
    }
  }

  @Override
  public WebDriver newInstance(Capabilities capabilities) {
    LOG.info("Creating a new session for " + capabilities);
    // Try and call the single arg constructor that takes a capabilities first
    return callConstructor(getDriverClass(), capabilities);
  }

  private WebDriver callConstructor(Class<? extends WebDriver> from, Capabilities capabilities) {
    try {
      Constructor<? extends WebDriver> constructor = from.getConstructor(Capabilities.class);
      return constructor.newInstance(capabilities);
    } catch (NoSuchMethodException e) {
      try {
        return from.newInstance();
      } catch (ReflectiveOperationException e1) {
        throw new WebDriverException(e);
      }
    } catch (ReflectiveOperationException e) {
      throw new WebDriverException(e);
    }
  }

  @Override
  public String toString() {
    return driverClass != null ? driverClass.toString() : driverClassName;
  }
}

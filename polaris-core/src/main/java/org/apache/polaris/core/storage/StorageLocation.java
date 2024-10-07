/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.polaris.core.storage;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.apache.polaris.core.storage.azure.AzureLocation;

/** An abstraction over a storage location */
public class StorageLocation {
  private final String location;

  public static StorageLocation of(String location) {
    if (AzureLocation.isAzureLocation(location)) {
      return new AzureLocation(location);
    } else {
      return new StorageLocation(location);
    }
  }

  protected StorageLocation(@NotNull String location) {
    if (location == null) {
      this.location = null;
    } else if (location.startsWith("file:/") && !location.startsWith("file:///")) {
      this.location = URI.create(location.replaceFirst("file:/+", "file:///")).toString();
    } else {
      this.location = URI.create(location).toString();
    }
  }

  /** If a path doesn't end in `/`, this will add one */
  protected final String ensureTrailingSlash(String location) {
    if (location == null || location.endsWith("/")) {
      return location;
    } else {
      return location + "/";
    }
  }

  @Override
  public int hashCode() {
    return location.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof StorageLocation) {
      return location.equals(((StorageLocation) obj).location);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return location;
  }

  /**
   * Returns true if this StorageLocation's location string starts with the other StorageLocation's
   * location string
   */
  public boolean isChildOf(StorageLocation potentialParent) {
    if (this.location == null || potentialParent.location == null) {
      return false;
    } else {
      String slashTerminatedLocation = ensureTrailingSlash(this.location);
      String slashTerminatedParentLocation = ensureTrailingSlash(potentialParent.location);
      return slashTerminatedLocation.startsWith(slashTerminatedParentLocation);
    }
  }

  public boolean isChildOf(NavigableSet<String> potentialParents) {
    if (this.location == null || potentialParents == null) {
      return false;
    } else {
      StringBuilder builder = new StringBuilder();
      NavigableSet<String> prefixes;
      for (char c : location.toCharArray()) {
        builder.append(c);
        prefixes = potentialParents.tailSet(builder.toString(), true);
        if (prefixes.isEmpty()) {
          break;
        } else if (prefixes.first().contentEquals(builder)) {
          return true;
        }
      }
    }
    return false;
  }
}

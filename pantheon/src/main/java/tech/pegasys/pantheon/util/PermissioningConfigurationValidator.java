/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.util;

import tech.pegasys.pantheon.cli.EthNetworkConfig;
import tech.pegasys.pantheon.ethereum.permissioning.PermissioningConfiguration;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PermissioningConfigurationValidator {

  public static void areAllBootnodesAreInWhitelist(
      final EthNetworkConfig ethNetworkConfig,
      final PermissioningConfiguration permissioningConfiguration)
      throws Exception {
    List<URI> bootnodesNotInWhitelist = new ArrayList<>();
    final Collection<URI> bootnodes = ethNetworkConfig.getBootNodes();
    if (permissioningConfiguration.isNodeWhitelistEnabled() && bootnodes != null) {
      bootnodesNotInWhitelist =
          bootnodes.stream()
              .filter(enode -> !permissioningConfiguration.getNodeWhitelist().contains(enode))
              .collect(Collectors.toList());
    }
    if (!bootnodesNotInWhitelist.isEmpty()) {
      throw new Exception(
          "Bootnode(s) not in nodes-whitelist " + enodesAsStrings(bootnodesNotInWhitelist));
    }
  }

  private static Collection<String> enodesAsStrings(final List<URI> bootnodesNotInWhitelist) {
    return bootnodesNotInWhitelist
        .parallelStream()
        .map(URI::toASCIIString)
        .collect(Collectors.toList());
  }
}

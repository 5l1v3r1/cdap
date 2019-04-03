/*
 * Copyright © 2014-2019 Cask Data, Inc.
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

package io.cdap.cdap.internal.app.deploy.pipeline;

import com.google.common.reflect.TypeToken;
import io.cdap.cdap.api.app.ApplicationSpecification;
import io.cdap.cdap.api.mapreduce.MapReduceSpecification;
import io.cdap.cdap.api.service.ServiceSpecification;
import io.cdap.cdap.api.service.http.HttpServiceHandlerSpecification;
import io.cdap.cdap.api.spark.SparkSpecification;
import io.cdap.cdap.app.store.Store;
import io.cdap.cdap.common.AlreadyExistsException;
import io.cdap.cdap.data2.registry.UsageRegistry;
import io.cdap.cdap.pipeline.AbstractStage;
import io.cdap.cdap.proto.id.ApplicationId;
import io.cdap.cdap.proto.id.NamespaceId;
import io.cdap.cdap.proto.id.ProgramId;
import io.cdap.cdap.security.impersonation.OwnerAdmin;

import java.io.IOException;
import java.util.Collection;

/**
 *
 */
public class ApplicationRegistrationStage extends AbstractStage<ApplicationWithPrograms> {

  private final Store store;
  private final UsageRegistry usageRegistry;
  private final OwnerAdmin ownerAdmin;

  public ApplicationRegistrationStage(Store store, UsageRegistry usageRegistry, OwnerAdmin ownerAdmin) {
    super(TypeToken.of(ApplicationWithPrograms.class));
    this.store = store;
    this.usageRegistry = usageRegistry;
    this.ownerAdmin = ownerAdmin;
  }

  @Override
  public void process(ApplicationWithPrograms input) throws Exception {
    ApplicationSpecification applicationSpecification = input.getSpecification();
    Collection<ApplicationId> allAppVersionsAppIds = store.getAllAppVersionsAppIds(input.getApplicationId());
    boolean ownerAdded = addOwnerIfRequired(input, allAppVersionsAppIds);
    try {
      store.addApplication(input.getApplicationId(), applicationSpecification);
    } catch (Exception e) {
      // if we failed to store the app spec cleanup the owner if it was added in this call
      if (ownerAdded) {
        ownerAdmin.delete(input.getApplicationId());
      }
      // propagate the exception
      throw e;
    }
    registerDatasets(input);
    emit(input);
  }

  // adds owner information for the application if this is the first version of the application
  private boolean addOwnerIfRequired(ApplicationWithPrograms input, Collection<ApplicationId> allAppVersionsAppIds)
    throws IOException, AlreadyExistsException {
    // if allAppVersionsAppIds.isEmpty() is true that means this app is an entirely new app and no other version
    // exists so we should add the owner information in owner store if one was provided
    if (allAppVersionsAppIds.isEmpty() && input.getOwnerPrincipal() != null) {
      ownerAdmin.add(input.getApplicationId(), input.getOwnerPrincipal());
      return true;
    }
    return false;
  }

  // Register dataset usage, based upon the program specifications.
  // Note that worker specifications' datasets are not registered upon app deploy because the useDataset of the
  // WorkerConfigurer is deprecated. Workers' access to datasets is aimed to be completely dynamic. Other programs are
  // moving in this direction.
  // Also, SparkSpecifications are the same in that a Spark program's dataset access is completely dynamic.
  private void registerDatasets(ApplicationWithPrograms input) {
    ApplicationSpecification appSpec = input.getSpecification();
    ApplicationId appId = input.getApplicationId();
    NamespaceId namespaceId = appId.getParent();

    for (MapReduceSpecification program : appSpec.getMapReduce().values()) {
      ProgramId programId = appId.mr(program.getName());
      for (String dataset : program.getDataSets()) {
        usageRegistry.register(programId, namespaceId.dataset(dataset));
      }
    }

    for (SparkSpecification sparkSpec : appSpec.getSpark().values()) {
      ProgramId programId = appId.spark(sparkSpec.getName());
      for (String dataset : sparkSpec.getDatasets()) {
        usageRegistry.register(programId, namespaceId.dataset(dataset));
      }
    }

    for (ServiceSpecification serviceSpecification : appSpec.getServices().values()) {
      ProgramId programId = appId.service(serviceSpecification.getName());
      for (HttpServiceHandlerSpecification handlerSpecification : serviceSpecification.getHandlers().values()) {
        for (String dataset : handlerSpecification.getDatasets()) {
          usageRegistry.register(programId, namespaceId.dataset(dataset));
        }
      }
    }
  }
}

/*
 * Copyright © 2018-2019 Cask Data, Inc.
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

package io.cdap.cdap.internal.app.runtime.workflow;

import com.google.inject.Inject;
import io.cdap.cdap.api.workflow.WorkflowToken;
import io.cdap.cdap.internal.app.store.AppMetadataStore;
import io.cdap.cdap.proto.WorkflowNodeStateDetail;
import io.cdap.cdap.proto.id.ProgramRunId;
import io.cdap.cdap.spi.data.transaction.TransactionRunner;
import io.cdap.cdap.spi.data.transaction.TransactionRunners;

/**
 * Implementation of {@link WorkflowStateWriter} that writes to {@link AppMetadataStore} directly.
 */
public class BasicWorkflowStateWriter implements WorkflowStateWriter {

  private final TransactionRunner transactionRunner;

  @Inject
  BasicWorkflowStateWriter(TransactionRunner transactionRunner) {
    this.transactionRunner = transactionRunner;
  }


  @Override
  public void setWorkflowToken(ProgramRunId workflowRunId, WorkflowToken token) {
    TransactionRunners.run(transactionRunner, context -> {
      AppMetadataStore.create(context).setWorkflowToken(workflowRunId, token);
    });
  }

  @Override
  public void addWorkflowNodeState(ProgramRunId workflowRunId, WorkflowNodeStateDetail nodeStateDetail) {
    TransactionRunners.run(transactionRunner, context -> {
      AppMetadataStore.create(context).addWorkflowNodeState(workflowRunId, nodeStateDetail);
    });
  }
}

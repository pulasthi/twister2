//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
package edu.iu.dsc.tws.executor.threading;

import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.common.threading.CommonThreadPool;
import edu.iu.dsc.tws.comms.api.TWSChannel;
import edu.iu.dsc.tws.executor.api.ExecutionPlan;
import edu.iu.dsc.tws.executor.api.IExecution;
import edu.iu.dsc.tws.executor.api.IExecutor;
import edu.iu.dsc.tws.executor.core.ExecutorContext;
import edu.iu.dsc.tws.task.graph.OperationMode;

public class Executor {
  private Config config;

  private int workerId;

  private IExecutor executor;

  public Executor(Config cfg, int wId, TWSChannel channel) {
    this(cfg, wId, channel, OperationMode.STREAMING);
  }

  public Executor(Config cfg, int wId, TWSChannel channel, OperationMode operationMode) {
    this.config = cfg;
    this.workerId = wId;

    //initialize common thread pool
    CommonThreadPool.init(config);

    // lets start the execution
    if (operationMode == OperationMode.STREAMING) {
      executor = new StreamingSharingExecutor(config, workerId, channel);
    } else {
      String batchExecutor = ExecutorContext.getBatchExecutor(config);
      if (ExecutorContext.BATCH_EXECUTOR_SHARING_SEP_COMM.equals(batchExecutor)) {
        executor = new BatchSharingExecutor(config, workerId, channel);
      } else if (ExecutorContext.BATCH_EXECUTOR_SHARING.equals(batchExecutor)) {
        executor = new BatchSharingExecutor2(config, workerId, channel);
      } else {
        throw new RuntimeException("Un-known batch executor specified - " + batchExecutor);
      }
    }
  }

  /***
   * Communication Channel must be progressed after the task execution model
   * is initialized. It must be progressed only after execution is instantiated.
   * */
  public boolean execute(ExecutionPlan executionPlan) {
    return executor.execute(executionPlan);
  }

  /***
   * Communication Channel must be progressed after the task execution model
   * is initialized. It must be progressed only after execution is instantiated.
   * */
  public IExecution iExecute(ExecutionPlan executionPlan) {
    return executor.iExecute(executionPlan);
  }

  public boolean waitFor(ExecutionPlan executionPlan) {
    return executor.waitFor(executionPlan);
  }

  public void close() {
    executor.close();
  }
}

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
package edu.iu.dsc.tws.examples.task;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import edu.iu.dsc.tws.api.task.ComputeConnection;
import edu.iu.dsc.tws.api.task.TaskGraphBuilder;
import edu.iu.dsc.tws.api.task.TaskWorker;
import edu.iu.dsc.tws.common.resource.AllocatedResources;
import edu.iu.dsc.tws.common.resource.WorkerComputeResource;
import edu.iu.dsc.tws.examples.comms.DataGenerator;
import edu.iu.dsc.tws.examples.comms.JobParameters;
import edu.iu.dsc.tws.executor.api.ExecutionPlan;
import edu.iu.dsc.tws.task.batch.BaseBatchSource;
import edu.iu.dsc.tws.task.graph.DataFlowTaskGraph;
import edu.iu.dsc.tws.task.graph.OperationMode;
import edu.iu.dsc.tws.task.streaming.BaseStreamSource;
import edu.iu.dsc.tws.tsched.spi.scheduler.Worker;
import edu.iu.dsc.tws.tsched.spi.scheduler.WorkerPlan;

public abstract class BenchTaskWorker extends TaskWorker {
  private static final Logger LOG = Logger.getLogger(BenchTaskWorker.class.getName());

  protected static final String SOURCE = "source";

  protected static final String SINK = "sink";

  protected DataFlowTaskGraph dataFlowTaskGraph;

  protected TaskGraphBuilder taskGraphBuilder;

  protected ExecutionPlan executionPlan;

  protected ComputeConnection computeConnection;

  protected static JobParameters jobParameters;

  @Override
  public void execute() {
    jobParameters = JobParameters.build(config);
    taskGraphBuilder = TaskGraphBuilder.newBuilder(config);
    if (jobParameters.isStream()) {
      taskGraphBuilder.setMode(OperationMode.STREAMING);
    } else {
      taskGraphBuilder.setMode(OperationMode.BATCH);
    }
    buildTaskGraph();
    dataFlowTaskGraph = taskGraphBuilder.build();
    executionPlan = taskExecutor.plan(dataFlowTaskGraph);
    taskExecutor.execute(dataFlowTaskGraph, executionPlan);


  }

  public WorkerPlan createWorkerPlan(AllocatedResources resourcePlan) {
    List<Worker> workers = new ArrayList<>();
    for (WorkerComputeResource resource : resourcePlan.getWorkerComputeResources()) {
      Worker w = new Worker(resource.getId());
      workers.add(w);
    }

    return new WorkerPlan(workers);
  }

  public abstract TaskGraphBuilder buildTaskGraph();

  protected static class SourceBatchTask extends BaseBatchSource {
    private static final long serialVersionUID = -254264903510284748L;
    private int count = 0;
    private String edge;

    public SourceBatchTask() {

    }

    public SourceBatchTask(String e) {
      this.edge = e;
    }

    @Override
    public void execute() {
      Object val = generateData();
      Object last = generateEmpty();
      if (count == 1) {
        if (context.writeEnd(this.edge, last)) {
          count++;
        }
      } else if (count < 1) {
        if (context.write(this.edge, val)) {
          count++;
        }
      }
    }
  }

  protected static class KeyedSourceBatchTask extends BaseBatchSource {
    private static final long serialVersionUID = -254264903510284748L;

    private String edge;

    private int count;

    public KeyedSourceBatchTask() {
    }

    public KeyedSourceBatchTask(String edge) {
      this.edge = edge;
    }

    @Override
    public void execute() {
      Object val = generateData();
      Object last = generateEmpty();
      if (count < 1) {
        context.write(edge, "" + count, val);
      } else if (count > 1) {
        context.writeEnd(edge, "" + count, last);
      }
      count++;
    }
  }

  protected static class SourceStreamTask extends BaseStreamSource {
    private static final long serialVersionUID = -254264903510284748L;
    private int count = 0;
    private String edge;

    public SourceStreamTask() {

    }

    public SourceStreamTask(String e) {
      this.edge = e;
    }

    @Override
    public void execute() {
      Object val = generateData();
      if (count % 1 == 0) {
        if (context.write(this.edge, val)) {
          count++;
        }
      }
    }
  }

  protected static class KeyedSourceStreamTask extends BaseStreamSource {
    private static final long serialVersionUID = -254264903510284748L;

    private String edge;

    private int count;

    public KeyedSourceStreamTask() {
    }

    public KeyedSourceStreamTask(String edge) {
      this.edge = edge;
    }

    @Override
    public void execute() {
      Object val = generateData();
      Object last = generateEmpty();
      if (count % 1 == 0) {
        context.write(edge, "" + count, val);
      }
      count++;
    }
  }


  protected static Object generateData() {
    return DataGenerator.generateIntData(jobParameters.getSize());
  }

  protected static Object generateEmpty() {
    return DataGenerator.generateIntData(0);
  }
}
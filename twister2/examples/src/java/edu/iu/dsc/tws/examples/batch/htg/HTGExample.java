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
package edu.iu.dsc.tws.examples.batch.htg;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.iu.dsc.tws.api.JobConfig;
import edu.iu.dsc.tws.api.Twister2Submitter;
import edu.iu.dsc.tws.api.htgjob.Twister2HTGJob;
import edu.iu.dsc.tws.api.htgjob.Twister2HTGScheduler;
import edu.iu.dsc.tws.api.job.Twister2Job;
import edu.iu.dsc.tws.api.task.Collector;
import edu.iu.dsc.tws.api.task.ComputeConnection;
import edu.iu.dsc.tws.api.task.Receptor;
import edu.iu.dsc.tws.api.task.TaskGraphBuilder;
import edu.iu.dsc.tws.api.task.TaskWorker;
import edu.iu.dsc.tws.api.task.htg.HTGBuilder;
import edu.iu.dsc.tws.api.task.htg.HTGComputeConnection;
import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.data.api.DataType;
import edu.iu.dsc.tws.dataset.DataSet;
import edu.iu.dsc.tws.dataset.Partition;
import edu.iu.dsc.tws.executor.api.ExecutionPlan;
import edu.iu.dsc.tws.proto.system.job.HTGJobAPI;
import edu.iu.dsc.tws.rsched.core.ResourceAllocator;
import edu.iu.dsc.tws.rsched.core.SchedulerContext;
import edu.iu.dsc.tws.task.api.IFunction;
import edu.iu.dsc.tws.task.api.IMessage;
import edu.iu.dsc.tws.task.batch.BaseBatchSink;
import edu.iu.dsc.tws.task.batch.BaseBatchSource;
import edu.iu.dsc.tws.task.graph.DataFlowTaskGraph;
import edu.iu.dsc.tws.task.graph.OperationMode;
import edu.iu.dsc.tws.task.graph.htg.HierarchicalTaskGraph;
import edu.iu.dsc.tws.tsched.utils.HTGParser;

public class HTGExample extends TaskWorker {

  private static final Logger LOG = Logger.getLogger(HTGExample.class.getName());

  private HTGJobParameters jobParameters;

  @Override
  public void execute() {

    HTGSourceTask htgSourceTask = new HTGSourceTask();
    HTGReduceTask htgReduceTask = new HTGReduceTask();

    this.jobParameters = HTGJobParameters.build(config);
    int parallelismValue = jobParameters.getParallelismValue();

    TaskGraphBuilder graphBuilderX = TaskGraphBuilder.newBuilder(config);
    graphBuilderX.addSource("source1", htgSourceTask, parallelismValue);
    ComputeConnection computeConnection1 = graphBuilderX.addSink("sink1", htgReduceTask,
        parallelismValue);
    computeConnection1.allreduce("source1", "all-reduce", new Aggregator(),
        DataType.OBJECT);
    graphBuilderX.setMode(OperationMode.STREAMING);
    DataFlowTaskGraph batchGraph = graphBuilderX.build();

    TaskGraphBuilder graphBuilderY = TaskGraphBuilder.newBuilder(config);
    graphBuilderY.addSource("source2", htgSourceTask, parallelismValue);
    ComputeConnection computeConnection2 = graphBuilderY.addSink("sink2", htgReduceTask,
        parallelismValue);
    computeConnection2.allreduce("source2", "all-reduce", new Aggregator(),
        DataType.OBJECT);
    graphBuilderY.setMode(OperationMode.BATCH);
    DataFlowTaskGraph streamingGraph = graphBuilderY.build();

    HTGBuilder hierarchicalTaskGraphBuilder =
        HTGBuilder.newBuilder(config);
    hierarchicalTaskGraphBuilder.addSourceTaskGraph("sourcetaskgraph", batchGraph);
    HTGComputeConnection htgComputeConnection = hierarchicalTaskGraphBuilder.addSinkTaskGraph(
        "sinktaskgraph", streamingGraph, "source2");
    htgComputeConnection.partition("sourcetaskgraph", "sink1");
    hierarchicalTaskGraphBuilder.setMode(OperationMode.BATCH);

    HierarchicalTaskGraph hierarchicalTaskGraph =
        hierarchicalTaskGraphBuilder.buildHierarchicalTaskGraph();

    LOG.info("Batch Task Graph:" + batchGraph.getTaskVertexSet() + "\t"
        + batchGraph.getTaskVertexSet().size() + "\t"
        + "Streaming Task Graph:" + streamingGraph.getTaskVertexSet() + "\t"
        + streamingGraph.getTaskVertexSet().size());

    //To print the hierarchical dataflow task graph and its vertex names.
    LOG.info("Parent Task Graph:" + hierarchicalTaskGraph.parentsOfTaskGraph(streamingGraph)
        + "\t" + hierarchicalTaskGraph.parentsOfTaskGraph(
        streamingGraph).iterator().next().getTaskGraphName());

    //Invoke HTG Parser
    HTGParser hierarchicalTaskGraphParser =
        new HTGParser(hierarchicalTaskGraph);
    List<DataFlowTaskGraph> dataFlowTaskGraphList =
        hierarchicalTaskGraphParser.hierarchicalTaskGraphParse();

    for (int i = 0; i < dataFlowTaskGraphList.size(); i++) {
      LOG.fine("dataflow task graph list:" + dataFlowTaskGraphList.get(i).getTaskGraphName());
    }

    //TODO:Invoke HTG Executor
    //testing
    ExecutionPlan plan = taskExecutor.plan(batchGraph);
    LOG.info("########### Task Schedule Plan Details:##########" + plan);
    //taskExecutor.execute(batchGraph, plan);

  }//End of execute method

  private static class HTGSourceTask extends BaseBatchSource implements Receptor {
    private static final long serialVersionUID = -254264120110286748L;

    @Override
    public void execute() {
      context.writeEnd("all-reduce", "Hello");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add(String name, DataSet<Object> data) {
      LOG.log(Level.FINE, "Received input: " + name);
    }
  }

  private static class HTGReduceTask extends BaseBatchSink implements Collector<Object> {
    private static final long serialVersionUID = -5190777711234234L;

    @Override
    public boolean execute(IMessage message) {
      LOG.log(Level.FINE, "Received centroids: " + context.getWorkerId()
          + ":" + context.taskId());
      return true;
    }

    @Override
    public Partition<Object> get() {
      return null;
    }
  }

  /**
   * This class aggregates the cluster centroid values and sum the new centroid values.
   */
  public static class Aggregator implements IFunction {
    private static final long serialVersionUID = -254264120110286748L;

    /**
     * The actual message callback
     *
     * @param object1 the actual message
     * @param object2 the actual message
     */
    @Override
    public Object onMessage(Object object1, Object object2) throws ArrayIndexOutOfBoundsException {
      return null;
    }
  }

  public static void main(String[] args) throws ParseException {
    LOG.log(Level.INFO, "HTG Graph Job");

    // first load the configurations from command line and config files
    Config config = ResourceAllocator.loadConfig(new HashMap<>());

    // build JobConfig
    HashMap<String, Object> configurations = new HashMap<>();
    configurations.put(SchedulerContext.THREADS_PER_WORKER, 8);

    Options options = new Options();
    options.addOption(HTGConstants.ARGS_PARALLELISM_VALUE, true, "2");
    options.addOption(HTGConstants.ARGS_WORKERS, true, "2");

    @SuppressWarnings("deprecation")
    CommandLineParser commandLineParser = new DefaultParser();
    CommandLine commandLine = commandLineParser.parse(options, args);

    int instances = Integer.parseInt(commandLine.getOptionValue(HTGConstants.ARGS_WORKERS));
    int parallelismValue =
        Integer.parseInt(commandLine.getOptionValue(HTGConstants.ARGS_PARALLELISM_VALUE));

    configurations.put(HTGConstants.ARGS_WORKERS, Integer.toString(instances));
    configurations.put(HTGConstants.ARGS_PARALLELISM_VALUE, Integer.toString(parallelismValue));

    // build JobConfig
    JobConfig jobConfig = new JobConfig();
    jobConfig.putAll(configurations);

    //TODO:Design the metagraph
    //TODO:Optimize the metagraph creation
    Twister2HTGJob.Twister2HTGMetaGraph htgMetaGraph = Twister2HTGJob.newBuilder();
    htgMetaGraph.addSubGraphs(2, 512, 1.0, 2, 1, "subjob1");
    htgMetaGraph.addSubGraphs(2, 512, 2.0, 2, 1, "subjob2");
    htgMetaGraph.addRelation("subgraph1", "subgraph2", "broadcast");
    htgMetaGraph.setHTGName("htg");
    htgMetaGraph.build();

    //TODO:Invoke HTG Scheduler and send the metagraph -> start with FIFO
    HTGJobAPI.SubGraph subGraph = Twister2HTGScheduler.schedule(htgMetaGraph);

    Twister2Job.Twister2JobBuilder jobBuilder = Twister2Job.newBuilder();
    jobBuilder.setJobName(subGraph.getName());
    jobBuilder.setWorkerClass(HTGExample.class.getName());
    jobBuilder.setConfig(jobConfig);
    jobBuilder.addComputeResource(
        subGraph.getCpu(), subGraph.getRamMegaBytes(), subGraph.getInstances());

    // now submit the job
    Twister2Submitter.submitJob(jobBuilder.build(), config);

  }
}


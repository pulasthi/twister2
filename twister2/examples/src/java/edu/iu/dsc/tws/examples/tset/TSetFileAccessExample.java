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
package edu.iu.dsc.tws.examples.tset;

import java.io.File;
import java.io.IOException;

import edu.iu.dsc.tws.api.tset.TSet;
import edu.iu.dsc.tws.api.tset.fn.OneToOnePartitioner;
import edu.iu.dsc.tws.api.tset.sink.FileSink;
import edu.iu.dsc.tws.api.tset.sources.FileSource;
import edu.iu.dsc.tws.data.api.formatters.SharedTextInputPartitioner;
import edu.iu.dsc.tws.data.api.out.TextOutputWriter;
import edu.iu.dsc.tws.data.fs.FileSystem;
import edu.iu.dsc.tws.data.fs.Path;
import edu.iu.dsc.tws.examples.comms.Constants;
import edu.iu.dsc.tws.examples.utils.DataGenerator;
import edu.iu.dsc.tws.executor.api.ExecutionPlan;
import edu.iu.dsc.tws.task.graph.DataFlowTaskGraph;

public class TSetFileAccessExample extends BaseTSetWorker {
  @Override
  public void execute() {
    super.execute();

    String inputDirectory = config.getStringValue(Constants.ARGS_FNAME,
        "/tmp/twister2");
    int numFiles = config.getIntegerValue(Constants.ARGS_WORKERS, 4);
    int size = config.getIntegerValue(Constants.ARGS_SIZE, 1000);

    String input = inputDirectory + "/input";
    String output = inputDirectory + "/output";
    if (workerId == 0) {
      try {
        new File(input).mkdirs();
        new File(output).mkdirs();

        DataGenerator.generateData("txt", new Path(input),
            numFiles, size, 10);
      } catch (IOException e) {
        throw new RuntimeException("Failed to create data: " + input);
      }
    }

    TSet<String> textSource = tSetBuilder.createSource(new FileSource<>(
        new SharedTextInputPartitioner(new Path(input)))).setParallelism(
            jobParameters.getTaskStages().get(0));

    textSource.partition(new OneToOnePartitioner<>()).sink(
        new FileSink<String>(new TextOutputWriter(
            FileSystem.WriteMode.OVERWRITE,
            new Path(output)))).setParallelism(
                jobParameters.getTaskStages().get(0));

    DataFlowTaskGraph graph = tSetBuilder.build();
    ExecutionPlan executionPlan = taskExecutor.plan(graph);
    taskExecutor.execute(graph, executionPlan);
  }
}
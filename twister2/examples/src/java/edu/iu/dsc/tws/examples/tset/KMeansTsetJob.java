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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.iu.dsc.tws.api.tset.MapFunction;
import edu.iu.dsc.tws.api.tset.Source;
import edu.iu.dsc.tws.api.tset.TSetBatchWorker;
import edu.iu.dsc.tws.api.tset.TwisterBatchContext;
import edu.iu.dsc.tws.api.tset.link.AllReduceTLink;
import edu.iu.dsc.tws.api.tset.sets.CachedTSet;
import edu.iu.dsc.tws.api.tset.sets.MapTSet;
import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.data.api.formatters.LocalFixedInputPartitioner;
import edu.iu.dsc.tws.data.api.formatters.SharedTextInputPartitioner;
import edu.iu.dsc.tws.data.fs.Path;
import edu.iu.dsc.tws.data.fs.io.InputSplit;
import edu.iu.dsc.tws.data.utils.DataObjectConstants;
import edu.iu.dsc.tws.dataset.DataSource;
import edu.iu.dsc.tws.examples.batch.kmeans.KMeansDataGenerator;
import edu.iu.dsc.tws.examples.batch.kmeans.KMeansWorkerParameters;
import edu.iu.dsc.tws.executor.core.ExecutionRuntime;
import edu.iu.dsc.tws.executor.core.ExecutorContext;

public class KMeansTsetJob extends TSetBatchWorker implements Serializable {
  private static final Logger LOG = Logger.getLogger(KMeansTsetJob.class.getName());

  @Override
  public void execute(TwisterBatchContext tc) {
    LOG.log(Level.INFO, "TSet worker starting: " + workerId);

    KMeansWorkerParameters kMeansJobParameters = KMeansWorkerParameters.build(config);

    int parallelismValue = kMeansJobParameters.getParallelismValue();
    int dimension = kMeansJobParameters.getDimension();
    int numFiles = kMeansJobParameters.getNumFiles();
    int dsize = kMeansJobParameters.getDsize();
    int csize = kMeansJobParameters.getCsize();
    int iterations = kMeansJobParameters.getIterations();

    String dinputDirectory = kMeansJobParameters.getDatapointDirectory();
    String cinputDirectory = kMeansJobParameters.getCentroidDirectory();

    if (workerId == 0) {
      try {
        KMeansDataGenerator.generateData(
            "txt", new Path(dinputDirectory), numFiles, dsize, 100, dimension, config);
        KMeansDataGenerator.generateData(
            "txt", new Path(cinputDirectory), numFiles, csize, 100, dimension, config);
      } catch (IOException ioe) {
        throw new RuntimeException("Failed to create input data:", ioe);
      }
    }

    //TODO: consider what happens when same execEnv is used to create multiple graphs
    CachedTSet<double[][]> points = tc.createSource(new PointsSource(), parallelismValue).cache();
    CachedTSet<double[][]> centers = tc.createSource(new CenterSource(), parallelismValue).cache();

    for (int i = 0; i < iterations; i++) {
      MapTSet<double[][], double[][]> kmeansTSet = points.map(new KMeansMap());
      kmeansTSet.addInput("centers", centers);
      AllReduceTLink<double[][]> reduced = kmeansTSet.allReduce((t1, t2) -> t1);
      centers = reduced.map(new AverageCenters(), parallelismValue).cache();
    }

  }

  public class KMeansMap implements MapFunction<double[][], double[][]> {

    @Override
    public double[][] map(double[][] doubles) {
      //TODO: cast needed since the context inputmap can hold many types of TSets, Solution?
      List<double[][]> centers = (List<double[][]>) CONTEXT.getInput("centers").getData();
      return new double[0][];
    }
  }

  private class AverageCenters implements MapFunction<double[][], double[][]> {

    @Override
    public double[][] map(double[][] doubles) {
      return new double[0][];
    }
  }

  public class PointsSource implements Source<double[][]> {


    private DataSource<double[][], InputSplit<double[][]>> source;

    public PointsSource() {
      Config cfg = CONTEXT.getConfig();
      String datainputDirectory = cfg.getStringValue(DataObjectConstants.ARGS_DINPUT_DIRECTORY);
      int datasize = Integer.parseInt(cfg.getStringValue(DataObjectConstants.ARGS_DSIZE));
      ExecutionRuntime runtime = (ExecutionRuntime)
          cfg.get(ExecutorContext.TWISTER2_RUNTIME_OBJECT);
      boolean shared = cfg.getBooleanValue(DataObjectConstants.ARGS_SHARED_FILE_SYSTEM);
      if (!shared) {
        this.source = new DataSource(cfg, new LocalFixedInputPartitioner(new
            Path(datainputDirectory), CONTEXT.getParallelism(), cfg, datasize),
            CONTEXT.getParallelism());
      } else {
        this.source = new DataSource(cfg, new SharedTextInputPartitioner(new
            Path(datainputDirectory), CONTEXT.getParallelism(), cfg),
            CONTEXT.getParallelism());
      }
    }

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public double[][] next() {
      LOG.fine("Context Task Index:" + CONTEXT.getIndex());
      InputSplit<double[][]> inputSplit = source.getNextSplit(CONTEXT.getIndex());
      int totalCount = 0;
      while (inputSplit != null) {
        try {
          int count = 0;
          while (!inputSplit.reachedEnd()) {
            Object value = inputSplit.nextRecord(null);
            //TODO collect values into array or list, need to check how values are read
            if (value != null) {
              count += 1;
            }
          }
          inputSplit = source.getNextSplit(CONTEXT.getIndex());
          LOG.info("Task index:" + CONTEXT.getIndex() + " count: " + count);
        } catch (IOException e) {
          LOG.log(Level.SEVERE, "Failed to read the input", e);
        }
      }
      return new double[0][];
    }
  }


  public class CenterSource implements Source<double[][]> {

    private DataSource<double[][], InputSplit<double[][]>> source;

    public CenterSource() {
      Config cfg = CONTEXT.getConfig();
      String datainputDirectory = cfg.getStringValue(DataObjectConstants.ARGS_DINPUT_DIRECTORY);
      int datasize = Integer.parseInt(cfg.getStringValue(DataObjectConstants.ARGS_DSIZE));
      ExecutionRuntime runtime = (ExecutionRuntime)
          cfg.get(ExecutorContext.TWISTER2_RUNTIME_OBJECT);
      boolean shared = cfg.getBooleanValue(DataObjectConstants.ARGS_SHARED_FILE_SYSTEM);
      if (!shared) {
        this.source = new DataSource(cfg, new LocalFixedInputPartitioner(new
            Path(datainputDirectory), CONTEXT.getParallelism(), cfg, datasize),
            CONTEXT.getParallelism());
      } else {
        this.source = new DataSource(cfg, new SharedTextInputPartitioner(new
            Path(datainputDirectory), CONTEXT.getParallelism(), cfg),
            CONTEXT.getParallelism());
      }
    }

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public double[][] next() {
      return new double[0][];
    }
  }
}


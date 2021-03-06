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
package edu.iu.dsc.tws.examples.ml.svm.aggregate;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.iu.dsc.tws.api.compute.IMessage;
import edu.iu.dsc.tws.api.compute.graph.OperationMode;
import edu.iu.dsc.tws.api.compute.modifiers.Collector;
import edu.iu.dsc.tws.api.compute.nodes.BaseCompute;
import edu.iu.dsc.tws.api.dataset.DataPartition;
import edu.iu.dsc.tws.dataset.partition.EntityPartition;

public class SVMReduce extends BaseCompute implements Collector {

  private static final long serialVersionUID = -254264120110286748L;

  private static final Logger LOG = Logger.getLogger(SVMReduce.class.getName());

  private double[] object;

  private boolean debug = false;

  private boolean status = false;

  private OperationMode operationMode;

  public SVMReduce(OperationMode operationMode) {
    this.operationMode = operationMode;
  }

  @Override
  public DataPartition<double[]> get() {
    return new EntityPartition<>(this.context.taskIndex(), this.object);
  }

  @Override
  public boolean execute(IMessage message) {

    if (message.getContent() == null) {
      LOG.info("Something Went Wrong !!!");
      this.status = false;
    } else {

      if (message.getContent() instanceof double[]) {
        this.status = true;
        this.object = (double[]) message.getContent();
        if (debug) {
          LOG.log(Level.INFO, "Received Data from workerId: " + this.context.getWorkerId()
              + ":" + this.context.globalTaskId() + ":" + Arrays.toString(this.object));
        } else {
          LOG.info("Object Type @Reduce : " + message.getContent().getClass().getName());
        }
      }

      if (this.operationMode.equals(OperationMode.BATCH)) {
        // do batch based computation
        if (debug) {
          LOG.info("Batch Mode : " + Arrays.toString(this.object));
        }

      }

      if (this.operationMode.equals(OperationMode.STREAMING)) {
        // do streaming based computation
        if (debug) {
          LOG.info("Streaming Mode : " + Arrays.toString(this.object));
        }

      }
    }
    return this.status;
  }
}

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
package edu.iu.dsc.tws.comms.api.batch;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.iu.dsc.tws.comms.api.BulkReceiver;
import edu.iu.dsc.tws.comms.api.CommunicationContext;
import edu.iu.dsc.tws.comms.api.Communicator;
import edu.iu.dsc.tws.comms.api.DataFlowOperation;
import edu.iu.dsc.tws.comms.api.DestinationSelector;
import edu.iu.dsc.tws.comms.api.MessageReceiver;
import edu.iu.dsc.tws.comms.api.MessageType;
import edu.iu.dsc.tws.comms.api.TaskPlan;
import edu.iu.dsc.tws.comms.dfw.DataFlowMultiGather;
import edu.iu.dsc.tws.comms.dfw.DataFlowPartition;
import edu.iu.dsc.tws.comms.dfw.RingPartition;
import edu.iu.dsc.tws.comms.dfw.io.Tuple;
import edu.iu.dsc.tws.comms.dfw.io.gather.GatherMultiBatchFinalReceiver;
import edu.iu.dsc.tws.comms.dfw.io.gather.GatherMultiBatchPartialReceiver;
import edu.iu.dsc.tws.comms.dfw.io.gather.keyed.DKGatherBatchFinalReceiver;
import edu.iu.dsc.tws.comms.dfw.io.gather.keyed.KGatherBatchFinalReceiver;
import edu.iu.dsc.tws.comms.dfw.io.gather.keyed.KGatherBatchPartialReceiver;

public class BKeyedGather {

  private static final Logger LOG = Logger.getLogger(BKeyedGather.class.getName());

  private DataFlowOperation keyedGather;

  private DestinationSelector destinationSelector;

  private MessageType keyType;

  private MessageType dataType;

  /**
   * Creates an instance of BKeyedGather
   *
   * @param shuffle Uses disk based receiver if true
   */
  public BKeyedGather(Communicator comm, TaskPlan plan,
                      Set<Integer> sources, Set<Integer> destinations,
                      MessageType kType, MessageType dType,
                      BulkReceiver rcvr, DestinationSelector destSelector, boolean shuffle) {
    this.keyType = kType;
    this.dataType = dType;
    MessageReceiver finalReceiver = null;
    MessageReceiver partialReceiver = new KGatherBatchPartialReceiver(0, 100);
    if (!shuffle) {
      finalReceiver = new KGatherBatchFinalReceiver(rcvr, 100);
    } else {
      finalReceiver = new DKGatherBatchFinalReceiver(
          rcvr, true, 10, comm.getPersistentDirectory(), null);
    }

    if (CommunicationContext.TWISTER2_KEYED_GATHER_OP_GATHER.equals(
        CommunicationContext.keyedGatherOp(comm.getConfig()))) {
      LOG.warning(() -> String.format(
          "Deprecated Keyed Gather operation %s used. Consider using %s",
          CommunicationContext.TWISTER2_KEYED_GATHER_OP_GATHER,
          CommunicationContext.TWISTER2_KEYED_GATHER_OP_PARTITION
      ));
      Set<Integer> edges = destinations.stream()
          .map(d -> comm.nextEdge()).collect(Collectors.toSet());

      this.keyedGather = new DataFlowMultiGather(comm.getConfig(), comm.getChannel(),
          plan, sources, destinations,
          new GatherMultiBatchFinalReceiver(rcvr, shuffle, false, comm.getPersistentDirectory(),
              null), new GatherMultiBatchPartialReceiver(),
          edges, kType, dType);
    } else if (CommunicationContext.TWISTER2_KEYED_GATHER_OP_PARTITION.equals(
        CommunicationContext.keyedGatherOp(comm.getConfig()))) {
      if (CommunicationContext.TWISTER2_PARTITION_ALGO_SIMPLE.equals(
          CommunicationContext.partitionAlgorithm(comm.getConfig()))) {
        this.keyedGather = new DataFlowPartition(comm.getConfig(), comm.getChannel(),
            plan, sources, destinations,
            finalReceiver, partialReceiver, dataType, dataType,
            keyType, keyType, comm.nextEdge());
      } else if (CommunicationContext.TWISTER2_PARTITION_ALGO_RING.equals(
          CommunicationContext.partitionAlgorithm(comm.getConfig()))) {
        this.keyedGather = new RingPartition(comm.getConfig(), comm.getChannel(),
            plan, sources, destinations, finalReceiver, partialReceiver,
            dataType, dataType, keyType, keyType, comm.nextEdge());
      }
    }
    this.destinationSelector = destSelector;
    this.destinationSelector.prepare(comm, sources, destinations);
  }

  public BKeyedGather(Communicator comm, TaskPlan plan,
                      Set<Integer> sources, Set<Integer> destinations,
                      MessageType kType, MessageType dType, BulkReceiver rcvr,
                      Comparator<Object> comparator, DestinationSelector destSelector,
                      boolean shuffle) {
    this.keyType = kType;
    this.dataType = dType;
    if (CommunicationContext.TWISTER2_KEYED_GATHER_OP_GATHER.equals(
        CommunicationContext.keyedGatherOp(comm.getConfig()))) {
      Set<Integer> edges = new HashSet<>();

      for (int i = 0; i < destinations.size(); i++) {
        edges.add(comm.nextEdge());
      }
      this.keyedGather = new DataFlowMultiGather(comm.getConfig(), comm.getChannel(),
          plan, sources, destinations,
          new GatherMultiBatchFinalReceiver(rcvr, shuffle, true, comm.getPersistentDirectory(),
              comparator), new GatherMultiBatchPartialReceiver(), edges,
          kType, dType);
    } else if (CommunicationContext.TWISTER2_KEYED_GATHER_OP_PARTITION.equals(
        CommunicationContext.keyedGatherOp(comm.getConfig()))) {
      this.keyedGather = new DataFlowPartition(comm.getConfig(), comm.getChannel(),
          plan, sources, destinations,
          new KGatherBatchFinalReceiver(rcvr, 100),
          new KGatherBatchPartialReceiver(0, 100), dataType, dataType,
          keyType, keyType, comm.nextEdge());
    }
    this.destinationSelector = destSelector;
    this.destinationSelector.prepare(comm, sources, destinations);
  }

  public boolean gather(int source, Object key, Object data, int flags) {
    int dest = destinationSelector.next(source, key, data);
    return keyedGather.send(source, new Tuple(key, data, keyType,
        dataType), flags, dest);
  }

  public boolean hasPending() {
    return !keyedGather.isComplete();
  }

  public void finish(int source) {
    keyedGather.finish(source);
  }

  public boolean progress() {
    return keyedGather.progress();
  }

  public void close() {
    // deregister from the channel
    keyedGather.close();
  }
}

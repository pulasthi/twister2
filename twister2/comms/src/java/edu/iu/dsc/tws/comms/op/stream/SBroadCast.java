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
package edu.iu.dsc.tws.comms.op.stream;

import java.util.Set;

import edu.iu.dsc.tws.comms.api.MessageReceiver;
import edu.iu.dsc.tws.comms.api.MessageType;
import edu.iu.dsc.tws.comms.core.TaskPlan;
import edu.iu.dsc.tws.comms.dfw.DataFlowBroadcast;
import edu.iu.dsc.tws.comms.op.Communicator;

public class SBroadCast {
  private DataFlowBroadcast bCast;

  public SBroadCast(Communicator comm, TaskPlan plan,
                    int sources, Set<Integer> destinations, MessageType dataType,
                    MessageReceiver rcvr) {
    this.bCast = new DataFlowBroadcast(comm.getChannel(), sources, destinations, rcvr);
    this.bCast.init(comm.getConfig(), dataType, plan, comm.nextEdge());
  }

  public boolean bcast(int source, Object message, int flags) {
    return bCast.send(source, message, flags);
  }

  public boolean progress() {
    return bCast.progress();
  }

  public boolean hasPending() {
    return !bCast.isComplete();
  }
}

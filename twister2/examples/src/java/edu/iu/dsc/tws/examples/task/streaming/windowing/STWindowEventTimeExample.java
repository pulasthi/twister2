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
package edu.iu.dsc.tws.examples.task.streaming.windowing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import edu.iu.dsc.tws.api.comms.messaging.types.MessageTypes;
import edu.iu.dsc.tws.api.compute.IMessage;
import edu.iu.dsc.tws.api.compute.TaskContext;
import edu.iu.dsc.tws.api.compute.TaskMessage;
import edu.iu.dsc.tws.api.config.Config;
import edu.iu.dsc.tws.examples.IntData;
import edu.iu.dsc.tws.examples.task.BenchTaskWorker;
import edu.iu.dsc.tws.examples.task.streaming.windowing.data.EventTimeData;
import edu.iu.dsc.tws.examples.task.streaming.windowing.extract.EventTimeExtractor;
import edu.iu.dsc.tws.task.impl.ComputeGraphBuilder;
import edu.iu.dsc.tws.task.typed.DirectCompute;
import edu.iu.dsc.tws.task.window.BaseWindowSource;
import edu.iu.dsc.tws.task.window.api.ITimestampExtractor;
import edu.iu.dsc.tws.task.window.api.IWindowMessage;
import edu.iu.dsc.tws.task.window.api.WindowMessageImpl;
import edu.iu.dsc.tws.task.window.collectives.AggregateWindow;
import edu.iu.dsc.tws.task.window.collectives.FoldWindow;
import edu.iu.dsc.tws.task.window.collectives.ProcessWindow;
import edu.iu.dsc.tws.task.window.collectives.ReduceWindow;
import edu.iu.dsc.tws.task.window.core.BaseWindowedSink;
import edu.iu.dsc.tws.task.window.function.AggregateWindowedFunction;
import edu.iu.dsc.tws.task.window.function.FoldWindowedFunction;
import edu.iu.dsc.tws.task.window.function.ProcessWindowedFunction;
import edu.iu.dsc.tws.task.window.function.ReduceWindowedFunction;

public class STWindowEventTimeExample extends BenchTaskWorker {

  private static final Logger LOG = Logger.getLogger(STWindowEventTimeExample.class.getName());

  @Override
  public ComputeGraphBuilder buildTaskGraph() {
    List<Integer> taskStages = jobParameters.getTaskStages();
    int sourceParallelism = taskStages.get(0);
    int sinkParallelism = taskStages.get(1);

    String edge = "edge";
    BaseWindowSource g = new SourceWindowTimeStampTask(edge);

    ITimestampExtractor<EventTimeData> timestampExtractor = new EventTimeExtractor();

    // Tumbling Window
    BaseWindowedSink dw = new DirectWindowedReceivingTask()
        .withTumblingCountWindow(5);
    BaseWindowedSink dwDuration = new DirectWindowedReceivingTask()
        .withTumblingDurationWindow(2, TimeUnit.MILLISECONDS);

    // Sliding Window
    BaseWindowedSink sdw = new DirectWindowedReceivingTask()
        .withSlidingCountWindow(5, 2);

    BaseWindowedSink sdwDuration = new DirectWindowedReceivingTask()
        .withSlidingDurationWindow(2, TimeUnit.MILLISECONDS,
            1, TimeUnit.MILLISECONDS);

    BaseWindowedSink sdwDurationReduce = new DirectReduceWindowedTask(new ReduceFunctionImpl())
        .withSlidingDurationWindow(2, TimeUnit.MILLISECONDS,
            1, TimeUnit.MILLISECONDS);

    BaseWindowedSink sdwCountSlidingReduce = new DirectReduceWindowedTask(new ReduceFunctionImpl())
        .withSlidingCountWindow(5, 2);

    BaseWindowedSink sdwCountTumblingReduce = new DirectReduceWindowedTask(new ReduceFunctionImpl())
        .withTumblingCountWindow(5);

    BaseWindowedSink sdwCountTumblingAggregate
        = new STWindowExample
        .DirectAggregateWindowedTask(new AggregateFunctionImpl(1, 2))
        .withTumblingCountWindow(5);

    BaseWindowedSink sdwCountTumblingFold = new DirectFoldWindowedTask(new FoldFunctionImpl())
        .withTumblingCountWindow(5);

    BaseWindowedSink sdwCountTumblingProcess
        = new DirectProcessWindowedIntTask(new ProcessFunctionIntImpl())
        .withCustomTimestampExtractor(timestampExtractor)
        .withAllowedLateness(0, TimeUnit.MILLISECONDS)
        .withWatermarkInterval(1, TimeUnit.MILLISECONDS)
        .withTumblingDurationWindow(1, TimeUnit.MILLISECONDS);
    computeGraphBuilder.addSource(SOURCE, g, sourceParallelism);
    computeConnection = computeGraphBuilder.addCompute(SINK,
        sdwCountTumblingProcess, sinkParallelism);
    computeConnection.direct(SOURCE).viaEdge(edge).withDataType(MessageTypes.INTEGER_ARRAY);
    //computeConnection.direct(SOURCE, edge, DataType.INTEGER_ARRAY);

    return computeGraphBuilder;
  }

  protected static class DirectReceiveTask extends DirectCompute<int[]> {
    private static final long serialVersionUID = -254264903510284798L;

    private int count = 0;

    @Override
    public void prepare(Config cfg, TaskContext ctx) {
      super.prepare(cfg, ctx);
    }

    @Override
    public boolean direct(int[] content) {
      LOG.info(String.format("Direct Data Received : %s ", Arrays.toString(content)));
      return true;
    }
  }

  protected static class DirectWindowedReceivingTask extends BaseWindowedSink<int[]> {

    public DirectWindowedReceivingTask() {
    }

    /**
     * This method returns the final windowing message
     *
     * @param windowMessage Aggregated IWindowMessage is obtained here
     * windowMessage contains [expired-tuples, current-tuples]
     */
    @Override
    public boolean execute(IWindowMessage<int[]> windowMessage) {
      LOG.info(String.format("Items : %d ", windowMessage.getWindow().size()));
      return true;
    }

    @Override
    public boolean getExpire(IWindowMessage<int[]> expiredMessages) {
      return false;
    }

    @Override
    public boolean getLateMessages(IMessage<int[]> lateMessages) {
      LOG.info(String.format("Late Message : %s",
          lateMessages.getContent() != null ? Arrays.toString(lateMessages.getContent()) : "null"));
      return true;
    }
  }

  protected static class DirectReduceWindowedTask extends ReduceWindow<int[]> {


    public DirectReduceWindowedTask(ReduceWindowedFunction<int[]> reduceWindowedFunction) {
      super(reduceWindowedFunction);
    }

    @Override
    public boolean reduce(int[] content) {
      LOG.info("Window Reduced Value : " + Arrays.toString(content));
      return true;
    }

    @Override
    public boolean reduceLateMessage(int[] content) {
      return false;
    }

  }

  protected static class DirectAggregateWindowedTask extends AggregateWindow<int[]> {

    public DirectAggregateWindowedTask(AggregateWindowedFunction aggregateWindowedFunction) {
      super(aggregateWindowedFunction);
    }

    @Override
    public boolean aggregate(int[] message) {
      LOG.info("Window Aggregate Value : " + Arrays.toString(message));
      return true;
    }

    @Override
    public boolean aggregateLateMessages(int[] message) {
      return false;
    }
  }

  protected static class DirectFoldWindowedTask extends FoldWindow<int[], String> {

    public DirectFoldWindowedTask(FoldWindowedFunction<int[], String> foldWindowedFunction) {
      super(foldWindowedFunction);
    }

    @Override
    public boolean fold(String content) {
      LOG.info("Window Fold Value : " + content);
      return true;
    }

    @Override
    public boolean foldLateMessage(String lateMessage) {
      return false;
    }
  }

  protected static class DirectProcessWindowedTask extends ProcessWindow<IntData> {

    public DirectProcessWindowedTask(ProcessWindowedFunction<IntData> processWindowedFunction) {
      super(processWindowedFunction);
    }

    @Override
    public boolean process(IWindowMessage<IntData> windowMessage) {
      for (IMessage<IntData> msg : windowMessage.getWindow()) {
        int[] msgC = msg.getContent().getData();
        LOG.info("Process Window Value : " + Arrays.toString(msgC));
      }
      return true;
    }

    @Override
    public boolean processLateMessages(IMessage<IntData> lateMessage) {
      return false;
    }
  }

  protected static class DirectProcessWindowedIntTask extends ProcessWindow<EventTimeData> {

    public DirectProcessWindowedIntTask(
        ProcessWindowedFunction<EventTimeData> processWindowedFunction) {
      super(processWindowedFunction);
    }

    @Override
    public boolean process(IWindowMessage<EventTimeData> windowMessage) {
//      for (IMessage<EventTimeData> msg : windowMessage.getWindow()) {
//        int[] msgC = msg.getContent().getData();
//        LOG.info("Process Window Value : " + Arrays.toString(msgC));
//      }
      LOG.info(String.format("Num Events : %d", windowMessage.getWindow().size()));
      return true;
    }

    @Override
    public boolean processLateMessages(IMessage<EventTimeData> lateMessage) {
      return false;
    }
  }


  protected static class ReduceFunctionImpl implements ReduceWindowedFunction<int[]> {

    @Override
    public int[] onMessage(int[] object1, int[] object2) {
      int[] ans = new int[object1.length];
      for (int i = 0; i < object1.length; i++) {
        ans[i] = object1[i] + object2[i];
      }
      return ans;
    }

    @Override
    public int[] reduceLateMessage(int[] lateMessage) {
      return new int[0];
    }
  }

  protected static class AggregateFunctionImpl implements AggregateWindowedFunction<int[]> {

    private int weight1;

    private int weight2;

    public AggregateFunctionImpl(int weight1, int weight2) {
      this.weight1 = weight1;
      this.weight2 = weight2;
    }

    @Override
    public int[] onMessage(int[] object1, int[] object2) {
      int[] ans = new int[object1.length];
      for (int i = 0; i < object1.length; i++) {
        ans[i] = this.weight1 * object1[i] + this.weight2 * object2[i];
      }
      return ans;
    }
  }

  protected static class FoldFunctionImpl implements FoldWindowedFunction<int[], String> {

    private int[] ans;

    @Override
    public String computeFold() {
      String summary = "Window Value With Basic Per Window Averaging : "
          + Arrays.toString(this.ans);
      return summary;
    }

    @Override
    public int[] onMessage(int[] object1, int[] object2) {
      this.ans = new int[object1.length];
      for (int i = 0; i < object1.length; i++) {
        ans[i] = (object1[i] + object2[i]) / 2;
      }
      return ans;
    }
  }

  protected static class ProcessFunctionImpl implements ProcessWindowedFunction<IntData> {

    @Override
    public IWindowMessage<IntData> process(IWindowMessage<IntData> windowMessage) {
      IntData current = null;
      List<IMessage<IntData>> messages = new ArrayList<>(windowMessage.getWindow().size());
      for (IMessage<IntData> msg : windowMessage.getWindow()) {
        IntData value = msg.getContent();
        if (current == null) {
          current = value;
        } else {
          current = add(current, value);
          messages.add(new TaskMessage<>(current));
        }
      }
      WindowMessageImpl<IntData> windowMessage1 = new WindowMessageImpl<>(messages);
      return windowMessage1;
    }

    @Override
    public IMessage<IntData> processLateMessage(IMessage<IntData> lateMessage) {
      return null;
    }

    @Override
    public IntData onMessage(IntData object1, IntData object2) {
      return new IntData();
    }

    private int[] add(int[] a1, int[] a2) {
      int[] ans = new int[a1.length];
      for (int i = 0; i < a1.length; i++) {
        ans[i] = 2 * (a1[i] + a2[i]);
      }
      return ans;
    }

    private IntData add(IntData d1, IntData d2) {
      IntData intData = new IntData();
      long t1 = d1.getTime();
      long t2 = d2.getTime();
      int[] data = add(d1.getData(), d2.getData());
      long t = (t1 + t2) / (long) 2.0;
      intData.setData(data);
      intData.setTime(t);
      return intData;
    }
  }

  protected static class ProcessFunctionIntImpl implements ProcessWindowedFunction<EventTimeData> {

    @Override
    public IWindowMessage<EventTimeData> process(IWindowMessage<EventTimeData> windowMessage) {
      EventTimeData current = null;
      List<IMessage<EventTimeData>> messages = new ArrayList<>(windowMessage.getWindow().size());
      for (IMessage<EventTimeData> msg : windowMessage.getWindow()) {
        EventTimeData value = msg.getContent();
        if (current == null) {
          current = value;
        } else {
          current = add(current, value);
          messages.add(new TaskMessage<>(current));
        }
      }
      WindowMessageImpl<EventTimeData> windowMessage1 = new WindowMessageImpl<>(messages);
      return windowMessage1;
    }

    @Override
    public IMessage<EventTimeData> processLateMessage(IMessage<EventTimeData> lateMessage) {
      return null;
    }

    @Override
    public EventTimeData onMessage(EventTimeData object1, EventTimeData object2) {
      return add(object1, object2);
    }

    private int[] add(int[] a1, int[] a2) {
      int[] ans = new int[a1.length];
      for (int i = 0; i < a1.length; i++) {
        ans[i] = 2 * (a1[i] + a2[i]);
      }
      return ans;
    }

    private EventTimeData add(EventTimeData d1, EventTimeData d2) {
      EventTimeData eventTimeData = null;
      long t1 = d1.getTime();
      long t2 = d2.getTime();
      int[] data = add(d1.getData(), d2.getData());
      long t = (t1 + t2) / (long) 2.0;
      eventTimeData = new EventTimeData(data, d1.getId(), t);
      return eventTimeData;
    }
  }
}

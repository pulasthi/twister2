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


package edu.iu.dsc.tws.tset.links.batch;

import java.util.Iterator;

import edu.iu.dsc.tws.api.comms.structs.Tuple;
import edu.iu.dsc.tws.api.tset.fn.ApplyFunc;
import edu.iu.dsc.tws.api.tset.fn.FlatMapFunc;
import edu.iu.dsc.tws.api.tset.fn.MapFunc;
import edu.iu.dsc.tws.api.tset.schema.Schema;
import edu.iu.dsc.tws.tset.env.BatchEnvironment;
import edu.iu.dsc.tws.tset.fn.GatherFlatMapCompute;
import edu.iu.dsc.tws.tset.fn.GatherForEachCompute;
import edu.iu.dsc.tws.tset.fn.GatherMapCompute;
import edu.iu.dsc.tws.tset.sets.batch.CachedTSet;
import edu.iu.dsc.tws.tset.sets.batch.ComputeTSet;
import edu.iu.dsc.tws.tset.sets.batch.KeyedTSet;
import edu.iu.dsc.tws.tset.sets.batch.PersistedTSet;
import edu.iu.dsc.tws.tset.sinks.CacheGatherSink;
import edu.iu.dsc.tws.tset.sinks.DiskPersistGatherIterSink;

/**
 * This is the Tlinks used by gather operations. Specific operations such as map, flatmap, cache,
 * etc will be done on the tuple value only (key will be dropped, as key is an information
 * forcibly attached at the communication level). If the key information is required, users can
 * use the compute methods which enables the use of Iterator{@literal <Tuple<Integer, T>>}
 *
 * @param <T> value type
 */
public abstract class BatchGatherLink<T> extends BatchTLinkImpl<Iterator<Tuple<Integer, T>>, T> {

  BatchGatherLink(BatchEnvironment env, String n, int sourceP, Schema schema) {
    this(env, n, sourceP, sourceP, schema);
  }

  BatchGatherLink(BatchEnvironment env, String n, int sourceP, int targetP,
                  Schema schema) {
    super(env, n, sourceP, targetP, schema);
  }

  protected BatchGatherLink() {
  }

  @Override
  public <O> ComputeTSet<O> map(MapFunc<T, O> mapFn) {
    GatherMapCompute<T, O> comp = new GatherMapCompute<>(mapFn);
    return compute("map", comp);
  }

  @Override
  public <O> ComputeTSet<O> flatmap(FlatMapFunc<T, O> mapFn) {
    GatherFlatMapCompute<T, O> comp = new GatherFlatMapCompute<>(mapFn);
    return compute("map", comp);
  }

  @Override
  public <K, V> KeyedTSet<K, V> mapToTuple(MapFunc<T, Tuple<K, V>> genTupleFn) {
    return this.computeToTuple("map2tup", new GatherMapCompute<>(genTupleFn));
  }

  @Override
  public void forEach(ApplyFunc<T> applyFunction) {
    ComputeTSet<Object> set = lazyForEach(applyFunction);
    getTSetEnv().run(set);
  }

  @Override
  public ComputeTSet<Object> lazyForEach(ApplyFunc<T> applyFunction) {
    GatherForEachCompute<T> comp = new GatherForEachCompute<>(applyFunction);
    return compute("foreach", comp);
  }

  @Override
  public CachedTSet<T> lazyCache() {
    CachedTSet<T> cacheTSet = new CachedTSet<>(getTSetEnv(), new CacheGatherSink<T>(),
        getTargetParallelism(), getSchema());
    addChildToGraph(cacheTSet);
    return cacheTSet;
  }

  @Override
  public CachedTSet<T> cache() {
    CachedTSet<T> cacheTSet = lazyCache();
    getTSetEnv().run(cacheTSet);
    return cacheTSet;
  }

  @Override
  public PersistedTSet<T> lazyPersist() {
    PersistedTSet<T> persistedTSet = new PersistedTSet<>(getTSetEnv(),
        new DiskPersistGatherIterSink<>(this.getId()), getTargetParallelism(), getSchema());
    addChildToGraph(persistedTSet);
    return persistedTSet;
  }

  @Override
  public PersistedTSet<T> persist() {
    return (PersistedTSet<T>) super.persist();
  }
}

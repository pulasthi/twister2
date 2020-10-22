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
package edu.iu.dsc.tws.dl.optim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.iu.dsc.tws.api.tset.sets.batch.BatchTSet;
import edu.iu.dsc.tws.dl.criterion.Criterion;
import edu.iu.dsc.tws.dl.data.Table;
import edu.iu.dsc.tws.dl.module.AbstractModule;

/**
 * Optimizer is an abstract class which is used to train a model automatically
 * with some certain optimization algorithms.
 */
public abstract class Optimizer<T> {
  private AbstractModule model;
  private BatchTSet<T> dataset;
  private Criterion criterion;

  private Table state;
  private Map<String, OptimMethod> optimMethods;

  public Optimizer(AbstractModule dlmodel, BatchTSet<T> batchTSet, Criterion errorCriterion) {
    this.model = dlmodel;
    this.dataset = batchTSet;
    this.criterion = errorCriterion;
    this.state = new Table();
    this.optimMethods = new HashMap<>();
    this.optimMethods.put(model.getName(), null); //TODO new SGD();
  }

  public AbstractModule getModel() {
    return model;
  }

  public void setModel(AbstractModule model) {
    this.model = model;
  }

  public BatchTSet<T> getDataset() {
    return (BatchTSet<T>) dataset;
  }

  public void setDataset(BatchTSet<T> dataset) {
    this.dataset = dataset;
  }

  public Criterion getCriterion() {
    return criterion;
  }

  public void setCriterion(Criterion criterion) {
    this.criterion = criterion;
  }

  public Table getState() {
    return state;
  }

  public void setState(Table state) {
    this.state = state;
  }

  public Map<String, OptimMethod> getOptimMethods() {
    return optimMethods;
  }

  public void setOptimMethods(Map<String, OptimMethod> optimMethods) {
    this.optimMethods = optimMethods;
  }

  /**
   * Trigger the optimization process
   *
   * @return the model to be trained
   */
  public abstract AbstractModule optimize();

  /**
   * Set an optimization method
   *
   * @param method optimization method
   */
  Optimizer<T> setOptimMethod(OptimMethod method) {
    List<String> nameList = new ArrayList<>();
    nameList.add(model.getName());
    checkSubModules(model, nameList);
    this.optimMethods = new HashMap<>();
    this.optimMethods.put(model.getName(), method);
    return this;
  }

  /**
   * Check if the sub modules are in the model, if each sub modules' parameter
   * is contiguous, if sub modules' parameter is duplicated.
   *
   * @param model
   * @param subModuleNames
   */
  private void checkSubModules(AbstractModule dlmodel, List<String> subModuleNames) {
//    TensorPair modelParameters = model.getParameters();
//    subModuleNames.stream().map(subModuleName -> {
//      AbstractModule subModule = model.apply(subModuleName);
//      Util.require(subModule != null, "Optimizer: couldn't find $subModuleName in $model");
//      Tensor subModuleWeights = subModule.getParameters().getValue0();
//      Util.require(subModuleWeights.nElement() > 0, "Optimizer: $subModuleName doesn't have" +
//          " any trainable parameters, please check your model and optimMethods.");
//      // If the storage subModule's parameter is the same with the storage of the submodule,
//      // then subModule's parameter is contiguous.
// Util.require(modelParameters.getValue0().storage() == subModuleWeights.storage(), "Optimizer:" +
//          " $subModuleName's parameter is not contiguous.");
//      (subModuleName, subModuleWeights);
//    });

    //TODO complete
    // make sure if parameters in submodules aren't duplicated.
//    if (p.length != 1) {
//      int sortedWeights = p.sortWith((a, b) => a._2.storageOffset() < b._2.storageOffset())
//      var i = 0
//      while (i < sortedWeights.length - 1) {
//        int current = sortedWeights(i)
//        int next = sortedWeights(i + 1)
//    Util.require(current._2.storageOffset() + current._2.nElement() <= next._2.storageOffset(),
//            s"Optimizer: ${current._1} and ${next._1}'s parameters are duplicated." +
//                s" Please check your model and optimMethods.")
//        i += 1
//      }
//    }
  }
}
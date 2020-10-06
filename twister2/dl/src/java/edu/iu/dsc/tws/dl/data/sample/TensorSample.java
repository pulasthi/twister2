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
package edu.iu.dsc.tws.dl.data.sample;

import edu.iu.dsc.tws.dl.data.Tensor;

public class TensorSample<T> implements edu.iu.dsc.tws.dl.data.Sample<T> {
  private Tensor<T>[] featureTensors;
  private Tensor<T>[] labelTensors;
  private int[][] featureSize;
  private int[][] labelSize;

  public TensorSample(Tensor<T> feature) {
    this.featureTensors = new Tensor[]{feature};
    this.labelTensors = new Tensor[]{};
  }

  public TensorSample(Tensor<T> feature, Tensor<T> label) {
    this.featureTensors = new Tensor[]{feature};
    this.labelTensors = new Tensor[]{label};
  }

  public TensorSample(Tensor<T>[] features) {
    this.featureTensors = features;
    this.labelTensors = new Tensor[]{};
  }

  public TensorSample(Tensor<T>[] features, Tensor<T>[] labels) {
    this.featureTensors = features;
    this.labelTensors = labels;
  }

  @Override
  public int featureLength(int index) {
    return featureTensors[0].size(1);
  }

  @Override
  public int labelLength(int index) {
    return labelTensors[0].size(1);
  }

  @Override
  public int numFeature() {
    return featureTensors.length;
  }

  @Override
  public int numLabel() {
    return labelTensors.length;
  }

  @Override
  public Tensor<T> feature() {
    return labelTensors[0];
  }

  @Override
  public Tensor<T> feature(int index) {
    return labelTensors[index];
  }

  @Override
  public Tensor<T> label() {
    return labelTensors[0];
  }

  @Override
  public Tensor<T> label(int index) {
    return labelTensors[index];
  }

  @Override
  public int[][] getFeatureSize() {
    return featureSize;
  }

  @Override
  public int[][] getLabelSize() {
    return labelSize;
  }

  @Override
  public T[] getData() {
    throw new UnsupportedOperationException("Operation not supported");
  }

  /**
   * Calculate the sizes of the features and labels
   */
  private void calculateSizes() {
    featureSize = new int[this.featureTensors.length][];
    labelSize = new int[this.labelTensors.length][];

    for (int i = 0; i < featureTensors.length; i++) {
      featureSize[i] = featureTensors[i].size();
    }

    for (int i = 0; i < labelTensors.length; i++) {
      labelSize[i] = labelTensors[i].size();
    }

  }
}

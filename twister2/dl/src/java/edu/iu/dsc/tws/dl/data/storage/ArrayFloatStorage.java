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
package edu.iu.dsc.tws.dl.data.storage;

import edu.iu.dsc.tws.dl.data.Storage;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayFloatStorage extends ArrayStorage {
  private float[] values;

  public ArrayFloatStorage(float[] values) {
    this.values = values;
  }

  public ArrayFloatStorage(int size) {
    this.values = new float[size];
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  public void update(int index, float value) {
    this.values[index] = value;
  }

  @Override
  public void update(int index, double value) {
    throw new UnsupportedOperationException("float operations not supported in ArrayFloatStorage");
  }

  @Override
  public Storage copy(Storage source, int offset, int sourceOffset, int length) {
    System.arraycopy(source.toDoubleArray(), sourceOffset, this.values, offset, length);
    return this;
  }

  @Override
  public Storage fill(float value, int offset, int length) {
    Arrays.fill(values, offset - 1, offset - 1 + length, value);
    return this;
  }

  @Override
  public Storage fill(double value, int offset, int length) {
    throw new UnsupportedOperationException("float operations not supported in ArrayFloatStorage");
  }

  @Override
  public Storage resize(int size) {
    this.values = new float[size];
    return this;
  }

  @Override
  public double[] toDoubleArray() {
    throw new UnsupportedOperationException("float operations not supported in ArrayFloatStorage");
  }

  @Override
  public float[] toFloatArray() {
    return values;
  }

  @Override
  public Storage set(Storage other) {
    throw new UnsupportedOperationException("operations not supported in ArrayFloatStorage");
  }

  @Override
  public Iterator iterator() {
    throw new UnsupportedOperationException("operations not supported in ArrayFloatStorage");
  }

  @Override
  public float getFloat(int index) {
    return values[index];
  }

  @Override
  public boolean isEmpty() {
    return values.length == 0;
  }

  @Override
  public double getDouble(int index) {
    throw new UnsupportedOperationException("operations not supported in ArrayFloatStorage");
  }
}
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
package edu.iu.dsc.tws.data.api.splits;

import java.io.IOException;

import edu.iu.dsc.tws.api.data.Path;

//TODO: HAVE TO VERIFY DO WE NEED THIS
public class ArrowInputSplit extends FileInputSplit<Object> {

  public ArrowInputSplit(int num, Path file, long start, long length, String[] hosts) {
    super(num, file, start, length, hosts);
  }

  public ArrowInputSplit(int num, Path file, String[] hosts) {
    super(num, file, hosts);
  }

  @Override
  public boolean reachedEnd() throws IOException {
    return false;
  }

  @Override
  public Object nextRecord(Object reuse) throws IOException {
    return null;
  }
}

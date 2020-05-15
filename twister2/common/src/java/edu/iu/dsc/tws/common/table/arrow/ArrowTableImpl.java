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
package edu.iu.dsc.tws.common.table.arrow;

import java.util.List;

import org.apache.arrow.vector.types.pojo.Schema;

import edu.iu.dsc.tws.common.table.Table;

public class ArrowTableImpl implements Table {
  private List<ArrowColumn> columns;

  private org.apache.arrow.vector.types.pojo.Schema schema;

  private int rows;

  public ArrowTableImpl(int rows, List<ArrowColumn> columns) {
    this(null, rows, columns);
  }

  public ArrowTableImpl(Schema schema, int rows, List<ArrowColumn> columns) {
    this.columns = columns;
    this.schema = schema;
    this.rows = rows;
  }

  public Schema getSchema() {
    return schema;
  }

  @Override
  public int rowCount() {
    return rows;
  }

  public List<ArrowColumn> getColumns() {
    return columns;
  }
}

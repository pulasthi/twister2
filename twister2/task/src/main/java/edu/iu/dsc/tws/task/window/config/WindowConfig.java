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
package edu.iu.dsc.tws.task.window.config;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class WindowConfig implements Serializable {

  private static final long serialVersionUID = 5892396934750193462L;

  public static class Count implements Serializable {

    private static final long serialVersionUID = 8272120867802383759L;
    public final long value;

    public Count(long value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return "Count{"
          + "value=" + value
          + '}';
    }
  }

  public static class Duration implements Serializable {

    private static final long serialVersionUID = -8082429717934376825L;
    public final long value;

    public Duration(long value, TimeUnit timeUnit) {
      this.value = timeUnit.toMillis(value);
    }

    public static Duration of(long milliseconds) {
      return new Duration(milliseconds, TimeUnit.MILLISECONDS);
    }

    public static Duration days(long days) {
      return new Duration(days, TimeUnit.DAYS);
    }

    public static Duration hours(long hours) {
      return new Duration(hours, TimeUnit.HOURS);
    }

    public static Duration minutes(long minutes) {
      return new Duration(minutes, TimeUnit.MINUTES);
    }

    public static Duration seconds(long seconds) {
      return new Duration(seconds, TimeUnit.SECONDS);
    }

    @Override
    public String toString() {
      return "Duration{"
          + "value=" + value
          + '}';
    }
  }

}

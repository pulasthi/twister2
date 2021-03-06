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

package edu.iu.dsc.tws.api.scheduler;

import edu.iu.dsc.tws.api.config.Config;
import edu.iu.dsc.tws.proto.system.job.JobAPI;

/**
 * Launches job. The purpose of the launcher is to bring up the required processes.
 * After it brings up the required processes, the controller is used for managing the job.
 */
public interface ILauncher extends AutoCloseable {
  /**
   * Initialize with the configuration
   *
   * @param config the configuration
   */
  void initialize(Config config);

  /**
   * Cleanup any resources
   */
  void close();

  /**
   * kill the submitted job
   */
  boolean killJob(String jobID);

  /**
   * Launch the processes according to the requested resources.
   *
   * @return true if the request is granted
   */
  Twister2JobState launch(JobAPI.Job job);
}

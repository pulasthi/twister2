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
package edu.iu.dsc.tws.rsched.schedulers.k8s;

/**
 * Environment variable names passed to worker pods
 */
public enum K8sEnvVariables {
  JOB_ID,
  USER_JOB_JAR_FILE,    // java jar file for running user job
  JOB_PACKAGE_FILENAME,
  JOB_PACKAGE_FILE_SIZE, // file size of tar.gz file
  CONTAINER_NAME,
  POD_NAME,
  HOST_IP, // node ip
  HOST_NAME, // node name
  POD_MEMORY_VOLUME,
  JOB_ARCHIVE_DIRECTORY,
  CLASS_TO_RUN,
  WORKER_PORT,
  UPLOAD_METHOD,
  JOB_PACKAGE_URI,
  JOB_MASTER_IP,
  ENCODED_NODE_INFO_LIST,
  LOGGER_PROPERTIES_FILE,
  JVM_MEMORY_MB,
  JOB_SUBMISSION_TIME,
  RESTORE_JOB
}

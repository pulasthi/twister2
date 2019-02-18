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
package edu.iu.dsc.tws.tsched.spi.taskschedule;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.ImmutableSet;

/**
 * This class is responsible for generating the task schedule plan which consists of container plan,
 * task instance plan along with their resource requirements.
 */
public class TaskSchedulePlan {

  private final Set<ContainerPlan> containers;

  private final Map<Integer, ContainerPlan> containersMap;
  private int jobId;

  public TaskSchedulePlan(int id, Set<ContainerPlan> containers) {
    this.jobId = id;
    this.containers = ImmutableSet.copyOf(containers);
    containersMap = new TreeMap<>();
    for (ContainerPlan containerPlan : containers) {
      containersMap.put(containerPlan.getContainerId(), containerPlan);
    }
  }

  public Resource getMaxContainerResources() {

    double maxCpu = 0.0;
    double maxRam = 0.0;
    double maxDisk = 0.0;

    for (ContainerPlan containerPlan : getContainers()) {
      Resource containerResource =
          containerPlan.getScheduledResource().orElse(containerPlan.getRequiredResource());
      maxCpu = Math.max(maxCpu, containerResource.getCpu());
      maxRam = Math.max(maxRam, containerResource.getRam());
      maxDisk = Math.max(maxDisk, containerResource.getDisk());
    }
    return new Resource(maxRam, maxDisk, maxCpu);
  }

  private int getTaskSchedulePlanId() {
    return jobId;
  }

  public Map<Integer, ContainerPlan> getContainersMap() {
    return containersMap;
  }

  public Set<ContainerPlan> getContainers() {
    return containers;
  }

  public Map<String, Integer> getTaskCounts() {
    Map<String, Integer> taskCounts = new HashMap<>();
    for (ContainerPlan containerPlan : getContainers()) {
      for (TaskInstancePlan instancePlan : containerPlan.getTaskInstances()) {
        Integer count = 0;
        if (taskCounts.containsKey(instancePlan.getTaskName())) {
          count = taskCounts.get(instancePlan.getTaskName());
        }
        taskCounts.put(instancePlan.getTaskName(), ++count);
      }
    }
    return taskCounts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TaskSchedulePlan)) {
      return false;
    }

    TaskSchedulePlan that = (TaskSchedulePlan) o;

    return (getTaskSchedulePlanId() == that.getTaskSchedulePlanId())
        && getContainers().equals(that.getContainers());
  }

  @Override
  public int hashCode() {
    int result = containers.hashCode();
    result = 31 * result + jobId;
    return result;
  }

  /**
   * This static class is responsible for assigning the task name, task id, task index, and resource
   * requirements of the task instances.
   */
  public static class TaskInstancePlan implements Comparable<TaskInstancePlan> {
    private final String taskName;
    private final int taskId;
    private final int taskIndex;
    private final Resource resource;

    public TaskInstancePlan(String taskName, int taskId, int taskIndex, Resource resource) {
      this.taskName = taskName;
      this.taskId = taskId;
      this.taskIndex = taskIndex;
      this.resource = resource;
    }

    public TaskInstancePlan(TaskInstanceId taskInstanceId, Resource resource) {
      this.taskName = taskInstanceId.getTaskName();
      this.taskId = taskInstanceId.getTaskId();
      this.taskIndex = taskInstanceId.getTaskIndex();
      this.resource = resource;
    }

    public String getTaskName() {
      return taskName;
    }

    public int getTaskId() {
      return taskId;
    }

    public int getTaskIndex() {
      return taskIndex;
    }

    public Resource getResource() {
      return resource;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TaskInstancePlan that = (TaskInstancePlan) o;

      return getTaskName().equals(that.getTaskName())
          && getTaskId() == that.getTaskId()
          && getTaskIndex() == that.getTaskIndex()
          && getResource().equals(that.getResource());
    }

    @Override
    public int hashCode() {
      int result = getTaskName().hashCode();
      result = 31 * result + ((Integer) getTaskId()).hashCode();
      result = 31 * result + ((Integer) getTaskIndex()).hashCode();
      result = 31 * result + getResource().hashCode();
      return result;
    }

    @Override
    public int compareTo(TaskInstancePlan o) {
      if (this.taskId == o.getTaskId()) {
        return Integer.compare(this.taskIndex, o.taskIndex);
      }
      return Integer.compare(this.taskId, o.taskId);
    }
  }

  /**
   * This static class is responsible for assigning the container id, task instances, required
   * resource, and scheduled resource for the task instances.
   */
  public static class ContainerPlan implements Comparable<ContainerPlan> {
    private final int containerId;
    private final Set<TaskInstancePlan> taskInstances;
    private final Resource requiredResource;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<Resource> scheduledResource;

    public ContainerPlan(int id, Set<TaskInstancePlan> instances, Resource requiredResource) {
      this(id, instances, requiredResource, null);
    }

    public ContainerPlan(int id,
                         Set<TaskInstancePlan> taskInstances,
                         Resource requiredResource,
                         Resource scheduledResource) {
      this.containerId = id;
      //this.taskInstances = ImmutableSet.copyOf(taskInstances);
      this.taskInstances = new TreeSet<>(taskInstances);
      this.requiredResource = requiredResource;
      this.scheduledResource = Optional.ofNullable(scheduledResource);
    }

    public int getContainerId() {
      return containerId;
    }

    public Set<TaskInstancePlan> getTaskInstances() {
      return taskInstances;
    }

    private Resource getRequiredResource() {
      return requiredResource;
    }

    private Optional<Resource> getScheduledResource() {
      return scheduledResource;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ContainerPlan that = (ContainerPlan) o;

      return containerId == that.containerId
          && getRequiredResource().equals(that.getRequiredResource())
          && getScheduledResource().equals(that.getScheduledResource());
    }


    @Override
    public int hashCode() {
      int result = containerId;
      result = 31 * result + getTaskInstances().hashCode();
      result = 31 * result + getRequiredResource().hashCode();
      if (scheduledResource.isPresent()) {
        result = (31 * result) + getScheduledResource().get().hashCode();
      }
      return result;
    }

    @Override
    public int compareTo(ContainerPlan o) {
      return Integer.compare(this.containerId, o.containerId);
    }
  }
}

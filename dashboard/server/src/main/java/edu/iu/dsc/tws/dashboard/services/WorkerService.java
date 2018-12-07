package edu.iu.dsc.tws.dashboard.services;

import edu.iu.dsc.tws.dashboard.data_models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.iu.dsc.tws.dashboard.repositories.WorkerRepository;
import edu.iu.dsc.tws.dashboard.rest_models.StateChangeRequest;
import edu.iu.dsc.tws.dashboard.rest_models.WorkerCreateRequest;

import javax.persistence.EntityNotFoundException;
import java.util.Date;

@Service
public class WorkerService {

  private final WorkerRepository workerRepository;

  private final JobService jobService;

  private final NodeService nodeService;

  private final ComputeResourceService computeResourceService;

  @Autowired
  public WorkerService(WorkerRepository workerRepository, JobService jobService, NodeService nodeService, ComputeResourceService computeResourceService) {
    this.workerRepository = workerRepository;
    this.jobService = jobService;
    this.nodeService = nodeService;
    this.computeResourceService = computeResourceService;
  }

  public Iterable<Worker> getAllForJob(String jobId) {
    return workerRepository.findAllByJob_JobID(jobId);
  }

  public Iterable<Worker> getAllWorkers() {
    return workerRepository.findAll();
  }

  public Worker createWorker(WorkerCreateRequest workerCreateRequest) {
    Worker worker = new Worker();
    worker.setWorkerID(workerCreateRequest.getWorkerID());
    worker.setWorkerIP(workerCreateRequest.getWorkerIP());
    worker.setWorkerPort(workerCreateRequest.getWorkerPort());

    //job
    Job jobById = jobService.getJobById(workerCreateRequest.getJobID());
    worker.setJob(jobById);

    //node
    Node node = nodeService.createNode(workerCreateRequest.getNode());
    worker.setNode(node);

    //compute resource
    ComputeResource computeResource = computeResourceService.findById(
            workerCreateRequest.getJobID(),
            workerCreateRequest.getComputeResourceIndex()
    );
    worker.setComputeResource(computeResource);

    //additional ports
    workerCreateRequest.getAdditionalPorts().forEach((label, port) -> {
      WorkerPort workerPort = new WorkerPort();
      workerPort.setLabel(label);
      workerPort.setPort(port);
      worker.getWorkerPorts().add(workerPort);
    });


    return this.workerRepository.save(worker);
  }

  @Transactional
  public void changeState(String jobId, Long workerId, StateChangeRequest<WorkerState> stateChangeRequest) {
    int amountChanged = this.workerRepository.changeWorkerState(jobId, workerId, stateChangeRequest.getState());
    if (amountChanged == 0) {
      this.throwNoSuchWorker(jobId, workerId);
    }
  }

  public void heartbeat(String jobId, Long workerId) {
    int beatCount = this.workerRepository.heartbeat(jobId, workerId, new Date());
    if (beatCount == 0) {
      this.throwNoSuchWorker(jobId, workerId);
    }
  }

  private void throwNoSuchWorker(String jobId, Long workerId) {
    throw new EntityNotFoundException("No such worker " + workerId
            + " found in job " + jobId);
  }
}

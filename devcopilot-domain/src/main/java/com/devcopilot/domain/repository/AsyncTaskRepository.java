package com.devcopilot.domain.repository;

import com.devcopilot.domain.model.AsyncTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsyncTaskRepository extends JpaRepository<AsyncTask, Long> {

    List<AsyncTask> findByBusinessTypeAndBusinessIdOrderByIdDesc(String businessType, Long businessId);
}

package com.gl.repository.app;

import com.gl.entity.app.KycTnmDataHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface KycTnmDataHistoryRepository extends JpaRepository<KycTnmDataHistory, Long> {
}
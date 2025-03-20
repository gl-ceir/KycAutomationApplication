package com.gl.repository.app;

import com.gl.entity.app.KycAirtelDataHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KycAirtelDataHistoryRepository extends JpaRepository<KycAirtelDataHistory, Long> {
}
package com.gl.repository.app;

import com.gl.entity.app.ModulesAuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModulesAuditTrailRepository extends JpaRepository<ModulesAuditTrail, Integer> {
}

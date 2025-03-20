package com.gl.repository.app;

import com.gl.entity.app.KycTnmData;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;


@Repository
public interface KycTnmDataRepository extends JpaRepository<KycTnmData, Long> {
    Optional<KycTnmData> findByMsisdn(String msisdn);

    @Modifying
    @Transactional
    @Query("UPDATE KycTnmData u SET u.idNumber = :idNumber ,u.idProofType = :idProofType  WHERE u.msisdn = :msisdn")
    void getUpdate(@Param("idNumber") String idNumber, @Param("idProofType") String idProofType, @Param("msisdn") String msisdn );

}

package com.gl.repository.app;

import com.gl.entity.app.KycAirtelData;
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
public interface KycAirtelDataRepository extends JpaRepository<KycAirtelData, Long> {
    KycAirtelData findByMsisdn(String msisdn);

    @Modifying
    @Query("UPDATE KycAirtelData u SET u.idNumber = :idNumber ,u.idProofType = :idProofType  WHERE u.msisdn = :msisdn")
    void getUpdate( @Param("msisdn") String msisdn,  @Param("idProofType") String idProofType, @Param("idNumber") String idNumber);

//    Optional<KycAirtelData> findByMsisdnAndIdNumber(String msisdn, String idNumber);
//    List<KycAirtelData> findAllByOperator(String operatorName);
}

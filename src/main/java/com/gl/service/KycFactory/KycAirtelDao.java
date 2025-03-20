package com.gl.service.KycFactory;


import com.gl.entity.app.KycAirtelData;
import com.gl.repository.app.KycAirtelDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

@Service
public class KycAirtelDao implements IKycInterface {

    @Autowired
    KycAirtelDataRepository kycAirtelRepository;

    @Override
    public <T> T getInsert(String msisdn, String id_number, String id_proof_type) {
        return (T) kycAirtelRepository.save(new KycAirtelData(msisdn, id_number, id_proof_type));
    }

    @Override
    public  void getUpdate(String msisdn, String id_number, String id_proof_type) {
         kycAirtelRepository.getUpdate( id_number, id_proof_type ,msisdn);
    }

    @Override
    public <T> T findKycDataByMsisdn(String msisdn) {
        return (T) kycAirtelRepository.findByMsisdn(msisdn);
    }


}

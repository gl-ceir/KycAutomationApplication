package com.gl.service.KycFactory;


import com.gl.entity.app.KycTnmData;
import com.gl.repository.app.KycTnmDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KycTnmDao implements IKycInterface {

    @Autowired
    KycTnmDataRepository kycTnmRepository;

    @Override
    public <T> T getInsert(String msisdn, String id_number, String id_proof_type) {
        return (T) kycTnmRepository.save(new KycTnmData(msisdn, id_number, id_proof_type));
    }

    @Override
    public <T> T findKycDataByMsisdn(String msisdn) {
        return (T) kycTnmRepository.findByMsisdn(msisdn);
    }

    @Override
    public void getUpdate(String msisdn, String id_number, String id_proof_type) {
         kycTnmRepository.getUpdate( id_number, id_proof_type ,msisdn);;
    }
}

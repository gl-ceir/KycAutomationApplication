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
    public <T> T getInsert(String msisdn, String id_proof_type, String id_number) {
        return (T) kycAirtelRepository.save(new KycAirtelData(msisdn,id_proof_type, id_number ));
    }

    @Override
    public  void getUpdate(String msisdn, String id_proof_type, String id_number) {
         kycAirtelRepository.getUpdate(msisdn,id_proof_type, id_number);
    }

    @Override
    public <T> T findKycDataByMsisdn(String msisdn) {
        return (T) kycAirtelRepository.findByMsisdn(msisdn);
    }


}

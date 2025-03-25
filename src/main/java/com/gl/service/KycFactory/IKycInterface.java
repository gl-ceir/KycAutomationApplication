package com.gl.service.KycFactory;

import org.springframework.stereotype.Service;

@Service
public interface IKycInterface {

    public <T> T getInsert(String msisdn, String id_proof_type ,String  id_number);

    public  void getUpdate(String msisdn, String id_proof_type, String  id_number );

    public <T> T findKycDataByMsisdn(String msisdn);

}

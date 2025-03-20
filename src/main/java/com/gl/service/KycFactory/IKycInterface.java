package com.gl.service.KycFactory;

import org.springframework.stereotype.Service;

@Service
public interface IKycInterface {

    public <T> T getInsert(String msisdn, String  id_number, String id_proof_type);

    public  void getUpdate(String msisdn, String  id_number , String id_proof_type);

    public <T> T findKycDataByMsisdn(String msisdn);

}

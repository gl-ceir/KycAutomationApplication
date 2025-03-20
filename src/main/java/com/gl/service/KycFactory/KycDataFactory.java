package com.gl.service.KycFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KycDataFactory {

    @Autowired
    KycTnmDao kycTnmDao;

    @Autowired
    KycAirtelDao kycAirtelDao;

    //findKycDataByMsisdn(record.getMsisdn()

    public IKycInterface createUser(String operator) {
        if (operator.equalsIgnoreCase("AIRTEL"))
            return kycAirtelDao;
        else return kycTnmDao;
    }
}

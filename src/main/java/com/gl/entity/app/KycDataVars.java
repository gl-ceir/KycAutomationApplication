package com.gl.entity.app;


import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycDataVars {
    private int id;

    private String msisdn;
    private String idProofType;

    private String idNumber;

}

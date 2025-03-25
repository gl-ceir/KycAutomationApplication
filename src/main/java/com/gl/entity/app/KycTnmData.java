package com.gl.entity.app;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "kyc_tnm_data", uniqueConstraints = @UniqueConstraint(columnNames = {"msisdn", "id_number"}))
public class KycTnmData extends KycDataVars{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "msisdn", nullable = false)
    private String msisdn;

    @Column(name = "id_number", nullable = false)
    private String idNumber;

    @Column(name = "id_proof_type")
    private String idProofType;

    public KycTnmData() {

    }

    // Getters and Setters


    public KycTnmData(String msisdn, String idProofType ,String idNumber ) {
        this.msisdn = msisdn;
        this.idProofType = idProofType;
        this.idNumber = idNumber;
    }
}
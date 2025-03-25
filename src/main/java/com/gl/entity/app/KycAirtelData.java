package com.gl.entity.app;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "kyc_airtel_data", uniqueConstraints = @UniqueConstraint(columnNames = {"msisdn", "id_number"}))
public class KycAirtelData extends KycDataVars{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "msisdn", nullable = false)
    private String msisdn;

    @Column(name = "id_number", nullable = false)
    private String idNumber;

    @Column(name = "id_proof_type")
    private String idProofType;

    public KycAirtelData() {
    }

    public KycAirtelData(String msisdn, String idProofType, String idNumber) {
        this.msisdn = msisdn;
        this.idProofType = idProofType;
        this.idNumber = idNumber;

    }
}
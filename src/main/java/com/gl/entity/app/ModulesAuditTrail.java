//package com.gl;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "modules_audit_trails")
//public class ModulesAuditTrail {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private int id;
//    private int statusCode;
//    private int numberOfRecord;
//    private int numberOfFailRecord;
//    private int numberOfSuccessRecord;
//    private int totalNumberOfRecord;
//    private String createdOn;
//    private String modifiedOn;
//    private String status;
//    private String errorMessage;
//    private String feature;
//    private String requestURL;
//    private String operatorName;
//
//    // Getters and Setters
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public int getStatusCode() {
//        return statusCode;
//    }
//
//    public void setStatusCode(int statusCode) {
//        this.statusCode = statusCode;
//    }
//
//    public int getNumberOfRecord() {
//        return numberOfRecord;
//    }
//
//    public void setNumberOfRecord(int numberOfRecord) {
//        this.numberOfRecord = numberOfRecord;
//    }
//
//    public int getNumberOfFailRecord() {
//        return numberOfFailRecord;
//    }
//
//    public void setNumberOfFailRecord(int numberOfFailRecord) {
//        this.numberOfFailRecord = numberOfFailRecord;
//    }
//
//    public int getNumberOfSuccessRecord() {
//        return numberOfSuccessRecord;
//    }
//
//    public void setNumberOfSuccessRecord(int numberOfSuccessRecord) {
//        this.numberOfSuccessRecord = numberOfSuccessRecord;
//    }
//
//    public int getTotalNumberOfRecord() {
//        return totalNumberOfRecord;
//    }
//
//    public void setTotalNumberOfRecord(int totalNumberOfRecord) {
//        this.totalNumberOfRecord = totalNumberOfRecord;
//    }
//
//    public String getCreatedOn() {
//        return createdOn;
//    }
//
//    public void setCreatedOn(String createdOn) {
//        this.createdOn = createdOn;
//    }
//
//    public String getModifiedOn() {
//        return modifiedOn;
//    }
//
//    public void setModifiedOn(String modifiedOn) {
//        this.modifiedOn = modifiedOn;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getErrorMessage() {
//        return errorMessage;
//    }
//
//    public void setErrorMessage(String errorMessage) {
//        this.errorMessage = errorMessage;
//    }
//
//    public String getFeature() {
//        return feature;
//    }
//
//    public void setFeature(String feature) {
//        this.feature = feature;
//    }
//
//    public String getRequestURL() {
//        return requestURL;
//    }
//
//    public void setRequestURL(String requestURL) {
//        this.requestURL = requestURL;
//    }
//
//    public String getOperatorName() {
//        return operatorName;
//    }
//
//    public void setOperatorName(String operatorName) {
//        this.operatorName = operatorName;
//    }
//
//    @Override
//    public String toString() {
//        return "ModulesAuditTrail [id=" + id + ", statusCode=" + statusCode + ", numberOfRecord=" + numberOfRecord
//                + ", numberOfFailRecord=" + numberOfFailRecord + ", numberOfSuccessRecord=" + numberOfSuccessRecord
//                + ", totalNumberOfRecord=" + totalNumberOfRecord + ", createdOn=" + createdOn + ", modifiedOn=" + modifiedOn
//                + ", status=" + status + ", errorMessage=" + errorMessage + ", feature=" + feature + ", requestURL=" + requestURL
//                + ", operatorName=" + operatorName + "]";
//    }
//}

package com.gl.entity.app;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "modules_audit_trail",catalog = "aud",schema ="aud" )

public class ModulesAuditTrail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "created_on")
    private Timestamp createdOn;

    @Column(name = "modified_on")
    private Timestamp modifiedOn;

    @Column(name = "execution_time")
    private String executionTime;

    @Column(name = "status_code")
    private int statusCode;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "module_name" )
    @Value("KYC")
    private String moduleName;

    @Column(name = "feature_name")
    private String feature;

    @Column(name = "action")
    private String operatorName;

    @Column(name = "count2", columnDefinition = "int default 0")
    private int numberOfSuccessRecord=0;

    @Column(name = "info")
    private String requestURL;

    @Column(name = "server_name")
    private String serverName;

    @Column(name = "failure_count", columnDefinition = "int default 0")
    private int numberOfFailRecord=0;

    @Column(name = "count", columnDefinition = "int default 0")
    private int totalNumberOfRecord=0;

    // Getters and Setters


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    public Timestamp getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Timestamp modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public int getNumberOfSuccessRecord() {
        return numberOfSuccessRecord;
    }

    public void setNumberOfSuccessRecord(int numberOfSuccessRecord) {
        this.numberOfSuccessRecord = numberOfSuccessRecord;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getNumberOfFailRecord() {
        return numberOfFailRecord;
    }

    public void setNumberOfFailRecord(int numberOfFailRecord) {
        this.numberOfFailRecord = numberOfFailRecord;
    }

    public int getTotalNumberOfRecord() {
        return totalNumberOfRecord;
    }

    public void setTotalNumberOfRecord(int totalNumberOfRecord) {
        this.totalNumberOfRecord = totalNumberOfRecord;
    }

    @Override
    public String toString() {
        return "ModulesAuditTrail{" +
                "id=" + id +
                ", createdOn=" + createdOn +
                ", modifiedOn=" + modifiedOn +
                ", executionTime='" + executionTime + '\'' +
                ", statusCode=" + statusCode +
                ", status='" + status + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", feature='" + feature + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", numberOfSuccessRecord=" + numberOfSuccessRecord +
                ", requestURL='" + requestURL + '\'' +
                ", serverName='" + serverName + '\'' +
                ", numberOfFailRecord=" + numberOfFailRecord +
                ", totalNumberOfRecord=" + totalNumberOfRecord +
                '}';
    }
}
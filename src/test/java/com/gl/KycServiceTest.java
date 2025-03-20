/*
package com.gl;

import com.gl.*;
import com.jcraft.jsch.*;
import com.opencsv.CSVWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KycServiceTest {

    @Mock
    private KycDataRepository kycDataRepository;

    @Mock
    private ModulesAuditTrailRepository auditTrailRepository;

    @Mock
    private KycTnmDataHistoryRepository kycTnmDataHistoryRepository;

    @Mock
    private KycAirtelDataHistoryRepository kycAirtelDataHistoryRepository;

    @Mock
    private DBConnection dbConnection;

    @InjectMocks
    private KycService kycService;

    @Mock
    private JSch jsch;

    @Mock
    private Session session;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ChannelSftp mockChannelSftp;

    private GlobalPropertyReader globalPropertyReader;

    private PropertyReader propertyReader;

    @BeforeEach
    void setUp() throws JSchException {
        MockitoAnnotations.initMocks(this);
        when(jsch.getSession(anyString(), anyString(), anyInt())).thenReturn(session);
        when(session.openChannel("sftp")).thenReturn(mockChannelSftp);
        // Add additional mock setup here

        when(session.openChannel("sftp")).thenReturn(mockChannelSftp);
        doNothing().when(session).connect();
        doNothing().when(mockChannelSftp).connect();
    }

    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.username}")
    private String sftpUsername;

    @Value("${sftp.port}")
    private int sftpPort;

    @Value("${sftp.password}")
    private String sftpPassword;


    @Test
    void testGetKycDataByOperator() {
        // Act
        List<KycData> actualData = kycService.getKycDataByOperator("tnm");

        // Assert
        assertEquals(106, actualData.size()); // Check that one record is fetched
        assertEquals("265881838419", actualData.get(0).getMsisdn()); // Verify msisdn
        assertEquals("National ID", actualData.get(0).getIdProofType()); // Verify id proof type
        assertEquals("P42W9ZJD", actualData.get(0).getIdNumber()); // Verify id number
    }






    @Test
    void testCreateAuditTrailEntry() {
        String operatorName = "tnm";
        String remoteFile = "testFile.csv";

        ModulesAuditTrail auditTrail = kycService.createAuditTrailEntry(operatorName, remoteFile);
        assertNotNull(auditTrail);
        assertEquals(operatorName, auditTrail.getOperatorName());

    }

    @Test
    void testUpdateAuditTrailEntry() {
        ModulesAuditTrail auditTrail = new ModulesAuditTrail();
        int totalNumberOfRecord = 10;
        int successRecords = 8;
        int failRecords = 2;

        kycService.updateAuditTrailEntry(auditTrail, totalNumberOfRecord, successRecords, failRecords);

        assertEquals(totalNumberOfRecord, auditTrail.getTotalNumberOfRecord());
        assertEquals(successRecords, auditTrail.getNumberOfSuccessRecord());
        assertEquals(failRecords, auditTrail.getNumberOfFailRecord());
    }

    @Test
    void testHandleProcessingError() {
        ModulesAuditTrail auditTrail = new ModulesAuditTrail();
        Exception exception = new Exception("Test Exception");

        kycService.handleProcessingError(auditTrail, exception);

        assertEquals("Test Exception", auditTrail.getErrorMessage());
    }



    @Test
    public void ForFileNoException() throws JSchException, SftpException {
        // Arrange
        String operatorName = "tnm";
        String fileKeyword = "kyc";
        String remoteDir = "/u02/eirsdata/KYC/ceirtnm/kycdump";

        kycService.airtelFileKeyword = fileKeyword; // Set the airtelFileKeyword variable
        kycService.airtelRemoteDir = remoteDir; // Set the airtelRemoteDir variable
        kycService.sftpHost = "159.89.225.5"; // Set the sftpHost variable
        kycService.sftpUsername = "ceir"; // Set the sftpUsername variable
        kycService.sftpPort = 22; // Set the sftpPort variable
        kycService.sftpPassword = "ceir@1234";


        // Act and Assert
        assertDoesNotThrow(() -> {
            kycService.checkForFile(operatorName);
        });
    }

   @Test
   public void testCreateProcessedFile() throws Exception {
       // Arrange
       String remoteFile = "kyc_airtel1.csv";
       String operatorName = "tnm";
       List<KycData> kycDataList = new ArrayList<>();
       KycData kycData = new KycData();
       kycData.setMsisdn("1234567890");
       kycData.setIdProofType("PAN");
       kycData.setIdNumber("ABC123");
       kycDataList.add(kycData);
       kycService.sftpHost = "159.89.225.5"; // Set the sftpHost variable
       kycService.sftpUsername = "ceir"; // Set the sftpUsername variable
       kycService.sftpPort = 22; // Set the sftpPort variable
       kycService.sftpPassword = "ceir@1234";
       // Act and Assert
       assertDoesNotThrow(() -> {
           kycService.createProcessedFile(remoteFile, kycDataList, operatorName);
       });
   }

   @Test
   public void testMoveToBackupSuccess() throws Exception {
       // Arrange
       String remoteFile = "kyc_airtel1.csv";
       String operatorName = "tnm";
       String backupDir = "/u02/eirsdata/KYC/ceirtnm/kycbackup";
       String remoteDir = "/u02/eirsdata/KYC/ceirtnm/kycdump";
       kycService.sftpHost = "159.89.225.5"; // Set the sftpHost variable
       kycService.sftpUsername = "ceir"; // Set the sftpUsername variable
       kycService.sftpPort = 22; // Set the sftpPort variable
       kycService.sftpPassword = "ceir@1234";
       kycService.tnmBackupDir = "/u02/eirsdata/KYC/ceirtnm/kycbackup";
       kycService.tnmRemoteDir = "/u02/eirsdata/KYC/ceirtnm/kycdump";
       try {
           kycService.moveToBackup(remoteFile, operatorName);
       } catch (Exception e) {
           fail("moveToBackup threw an exception: " + e.getMessage());
       }
   }
}
*/

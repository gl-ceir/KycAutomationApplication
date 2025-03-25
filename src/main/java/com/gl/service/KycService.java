package com.gl.service;

import com.gl.entity.app.KycAirtelDataHistory;
import com.gl.entity.app.KycTnmData;
import com.gl.entity.app.KycTnmDataHistory;
import com.gl.entity.app.ModulesAuditTrail;
import com.gl.repository.app.KycAirtelDataHistoryRepository;
import com.gl.repository.app.KycTnmDataHistoryRepository;
import com.gl.repository.app.KycTnmDataRepository;
import com.gl.repository.app.ModulesAuditTrailRepository;
import com.gl.service.KycFactory.KycDataFactory;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class KycService {

    private static final Logger log = LoggerFactory.getLogger(KycService.class);
    @Value("${sftp.remote.dir.airtel}")
    String airtelRemoteDir;
    @Value("${sftp.remote.dir.tnm}")
    String tnmRemoteDir;
    @Value("${sftp.file.keyword.airtel}")
    String airtelFileKeyword;
    @Value("${sftp.backup.dir.airtel}")
    String airtelBackupDir;
    @Value("${sftp.backup.dir.tnm}")
    String tnmBackupDir;
    @Value("${sftp.processed.dir.airtel}")
    String airtelProcessedDir;
    @Value("${sftp.processed.dir.tnm}")
    String tnmProcessedDir;
    @Value("${sftp.host}")
    String sftpHost;
    @Value("${sftp.username}")
    String sftpUsername;
    @Value("${sftp.port}")
    int sftpPort;
    @Value("${sftp.password}")
    String sftpPassword;
    @Autowired
    private KycTnmDataRepository kycDataRepository;
    @Autowired
    private KycTnmDataHistoryRepository kycTnmDataHistoryRepository;
    @Autowired
    private KycAirtelDataHistoryRepository kycAirtelDataHistoryRepository;
    @Value("${sftp.file.keyword.tnm}")
    private String tnmFileKeyword;
    @Value("${sftp.delta.dir.airtel}")
    private String airtelDeltaDir;

    @Value("${sftp.delta.dir.tnm}")
    private String tnmDeltaDir;

    @Autowired
    ModulesAuditTrailRepository modulesAuditTrailRepository;


    @Autowired
    KycDataFactory kycDataFactory;


  //  @Transactional
    public void processKycFile(String operatorName) {
        String remoteFile = checkForFile(operatorName);
        if (remoteFile == null) {
            log.info("No file found. Exiting...");
            return;
        }
        log.info("No File Name  found. ..." + remoteFile);
        ModulesAuditTrail auditTrail = createAuditTrailEntry(operatorName, remoteFile);
        log.info("Audit Trail created: {}", auditTrail);
        try {
            List<KycTnmData> kycDataList = processFileFromSftp(remoteFile, operatorName);
            boolean processedFileExists = checkProcessedFileOnSftp(operatorName);
            log.info("processedFileExists . ..." + processedFileExists);
            List<KycTnmData> deltaDataList = new ArrayList<>();
            if (processedFileExists) {
                deltaDataList = createDeltaFile(operatorName, kycDataList);
                if (deltaDataList.isEmpty()) {
                    log.warn("Delta file is empty. No changes detected.");
                }
            } else {
                log.warn("Processed file does not exist on SFTP. Delta file will not be created.");
            }
            List<KycTnmData> dataToProcess = deltaDataList.isEmpty() ? kycDataList : deltaDataList;

            int numberOfRecordsToProcess = dataToProcess.size();
            int numberOfSuccessRecord = 0;
            int numberOfFailRecord = 0;

            log.info("Number of records to process: {}", numberOfRecordsToProcess);

            //   ExecutorService executorService = Executors.newFixedThreadPool(4);
            //   List<Future<Boolean>> futures = new ArrayList<>();
            var kycDataVar = kycDataFactory.createUser(operatorName);
            log.info("User  {}",kycDataVar);
            for (KycTnmData record : dataToProcess) {
                //    futures.add(executorService.submit(() -> {
                try {
                    var t = kycDataVar.findKycDataByMsisdn(record.getMsisdn());
                    if (t == null) {
                        log.info("insert {}", record.toString());
                        kycDataVar.getInsert(record.getMsisdn(), record.getIdProofType(), record.getIdNumber());
                        //  insertKycData(record, operatorName);
                    } else {
                        log.info("update :: {}", t.toString()+   "  to ->" +  record);
                        kycDataVar.getUpdate(record.getMsisdn(), record.getIdProofType(), record.getIdNumber());
                        //   updateKycData(record, operatorName);
                    }

                    numberOfSuccessRecord++;
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage() +"Error processing KYC data: {}", e.getMessage() + e);
                    numberOfFailRecord++;
                }
                //   }));
            }

            log.info("Number of records processed successfully: {}", numberOfSuccessRecord);
            log.info("Number of records failed: {}", numberOfFailRecord);

            updateAuditTrailEntry(auditTrail, kycDataList.size(), numberOfSuccessRecord, numberOfFailRecord);
            createProcessedFile(remoteFile, kycDataList, operatorName);
            moveToBackup(remoteFile, operatorName);
        } catch (Exception e) {
            handleProcessingError(auditTrail, e);
        }
    }


    private boolean checkProcessedFileOnSftp(String operatorName) {
        Session session = null;
        ChannelSftp channelSftp = null;
        String remoteDir = operatorName.equalsIgnoreCase("airtel") ? airtelProcessedDir : tnmProcessedDir;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.cd(remoteDir);
            // List files in the directory
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls("*.csv");
            for (ChannelSftp.LsEntry entry : fileList) {
                String fileName = entry.getFilename();
                // Check if the file name ends with "_processed.csv"
                if (fileName.endsWith("_processed_.csv")) {
                    return true; // Found a matching processed file
                }
            }
            return false; // No matching processed file found
        } catch (Exception e) {
            log.error("Error checking for processed file on SFTP", e);
            return false;
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }


    ModulesAuditTrail createAuditTrailEntry(String operatorName, String remoteFile) {
        try {
            ModulesAuditTrail auditTrail = new ModulesAuditTrail();
            auditTrail.setOperatorName(operatorName);
            auditTrail.setStatusCode(201);
            auditTrail.setStatus("INIT");
            auditTrail.setFeature("KYC Processing");
            auditTrail.setRequestURL(remoteFile);
            auditTrail.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
            return modulesAuditTrailRepository.save(auditTrail);
        } catch (Exception e) {
            log.error("Error creating audit trail entry", e);
            return null;

        }
    }

    void updateAuditTrailEntry(ModulesAuditTrail auditTrail, int totalRecords, int successRecords, int failRecords) {
        try {
            auditTrail.setStatusCode(200);
            auditTrail.setNumberOfSuccessRecord(successRecords);
            auditTrail.setNumberOfFailRecord(failRecords);
            auditTrail.setTotalNumberOfRecord(totalRecords);
            auditTrail.setStatus("SUCCESS");
            auditTrail.setModifiedOn(Timestamp.valueOf(LocalDateTime.now()));
            modulesAuditTrailRepository.save(auditTrail);
        } catch (Exception e) {
            log.error("Error updating audit trail entry", e);
        }
    }

    void handleProcessingError(ModulesAuditTrail auditTrail, Exception e) {
        try {
            auditTrail.setStatusCode(500);
            auditTrail.setStatus("ERROR");
            auditTrail.setErrorMessage(e.getMessage());
            auditTrail.setModifiedOn(Timestamp.valueOf(LocalDateTime.now()));
            modulesAuditTrailRepository.save(auditTrail);
        } catch (Exception ex) {
            log.error("Error handling processing error", ex);
        }
    }

    private List<KycTnmData> createDeltaFile(String operatorName, List<KycTnmData> newKycDataList) throws FileNotFoundException {
        String processedDir = operatorName.equalsIgnoreCase("airtel") ? airtelProcessedDir : tnmProcessedDir;
        String deltaDir = operatorName.equalsIgnoreCase("airtel") ? airtelDeltaDir : tnmDeltaDir;
        List<KycTnmData> oldKycDataList = findLatestProcessedFile(processedDir);
        List<KycTnmData> deltaDataList = new ArrayList<>();
        if (!oldKycDataList.isEmpty()) {
            deltaDataList = compareFiles(oldKycDataList, newKycDataList);
            // Save the delta file
            saveDeltaFile(deltaDir, deltaDataList, operatorName);
        } else {
            log.info("No processed file found. Skipping delta file creation.");
        }
        /*System.out.println("Delta Data List : "+deltaDataList);*/
        log.info("Delta Data List : " + deltaDataList);
        return deltaDataList;
    }


    private List<KycTnmData> findLatestProcessedFile(String processedDir) {
        Session session = null;
        ChannelSftp channelSftp = null;
        List<KycTnmData> latestKycDataList = new ArrayList<>();
        long latestModifiedTime = 0;
        String latestFileName = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            channelSftp.cd(processedDir); // Change to the processed directory

            // List files in the directory
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls("*.csv"); // List all CSV files
            for (ChannelSftp.LsEntry file : files) {
                // Check for processed files
                if (file.getFilename().endsWith("_processed_.csv")) {
                    // Retrieve the last modified time
                    long modifiedTime = file.getAttrs().getMTime() * 1000L; // Convert to milliseconds
                    if (modifiedTime > latestModifiedTime) {
                        latestModifiedTime = modifiedTime;
                        latestFileName = file.getFilename(); // Store the latest file name
                    }
                }
            }

            // If a latest file is found, parse it
            if (latestFileName != null) {
                InputStream inputStream = channelSftp.get(latestFileName);
                latestKycDataList = parseFile(inputStream);
            }
        } catch (Exception e) {
            log.error("Error finding the latest processed file", e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

        return latestKycDataList; // Return the list of KYC data
    }

    private List<KycTnmData> compareFiles(List<KycTnmData> oldList, List<KycTnmData> newList) {
        Map<String, KycTnmData> oldMap = new HashMap<>();
        for (KycTnmData data : oldList) {
            oldMap.put(data.getMsisdn(), data);
        }

        List<KycTnmData> deltaList = new ArrayList<>();
        for (KycTnmData newData : newList) {
            KycTnmData oldData = oldMap.get(newData.getMsisdn());
            if (oldData == null || !oldData.equals(newData)) {
                deltaList.add(newData);
            }
        }
        return deltaList;
    }


    void createProcessedFile(String remoteFile, List<KycTnmData> kycDataList, String operatorName) {
        // Get the current date and time for the file name
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String processedFileName = remoteFile.replace(".csv", timestamp + "_processed_" + ".csv");
        String processedFilePath = operatorName.equalsIgnoreCase("airtel")
                ? airtelProcessedDir + "/" + processedFileName
                : tnmProcessedDir + "/" + processedFileName;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {

            for (KycTnmData data : kycDataList) {
                writer.writeNext(new String[]{
                        data.getMsisdn(),
                        data.getIdProofType(),
                        data.getIdNumber()
                });
            }
            writer.flush();

            // Upload the processed file to the SFTP server
            uploadFileToSftp(outputStream.toByteArray(), processedFileName, operatorName.equalsIgnoreCase("airtel") ? airtelProcessedDir : tnmProcessedDir);
        } catch (IOException e) {
            log.error("Error writing processed file", e);
        }
    }


    private void saveDeltaFile(String deltaDir, List<KycTnmData> deltaDataList, String operatorName) {
        // Get the current date and time for the file name
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String deltaFileName = "delta_" + operatorName + "_" + timestamp + ".csv";

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            for (KycTnmData data : deltaDataList) {
                writer.writeNext(new String[]{
                        data.getMsisdn(),
                        data.getIdProofType(),
                        data.getIdNumber()
                });
            }
            writer.flush();
            // Upload the delta file to the SFTP server
            uploadFileToSftp(outputStream.toByteArray(), deltaFileName, deltaDir);
        } catch (IOException e) {
            log.error("Error writing delta file", e);
        }
    }

    void uploadFileToSftp(byte[] fileData, String fileName, String remoteDir) {
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            InputStream inputStream = new ByteArrayInputStream(fileData);
            channelSftp.put(inputStream, remoteDir + "/" + fileName);
            log.info("File uploaded to SFTP: {}/{}", remoteDir, fileName);
        } catch (Exception e) {
            log.error("Error uploading file to SFTP", e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    String checkForFile(String operatorName) {
        Session session = null;
        ChannelSftp channelSftp = null;
        String fileKeyword = operatorName.equalsIgnoreCase("airtel") ? airtelFileKeyword : tnmFileKeyword;
        String remoteDir = operatorName.equalsIgnoreCase("airtel") ? airtelRemoteDir : tnmRemoteDir;
        log.info(fileKeyword + "    Going for Sftp remoteDir : " + remoteDir);
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.cd(remoteDir);
            // List files in the directory and check for the keyword
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls(remoteDir);
            for (ChannelSftp.LsEntry file : files) {
                if (file.getFilename().contains(fileKeyword)) {
                    return file.getFilename(); // Return the first matching file
                }
            }
        } catch (Exception e) {
            log.error("Error checking for file", e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        return null; // No file found
    }

    private List<KycTnmData> processFileFromSftp(String remoteFile, String operatorName) {
        Session session = null;
        ChannelSftp channelSftp = null;
        List<KycTnmData> kycDataList = new ArrayList<>();
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            // Navigate to the appropriate remote directory
            String remoteDir = operatorName.equalsIgnoreCase("airtel") ? airtelRemoteDir : tnmRemoteDir;
            channelSftp.cd(remoteDir);
            // Get the input stream for the remote file
            InputStream inputStream = channelSftp.get(remoteFile);
            kycDataList = parseFile(inputStream);
            // Process records
        //    processRecords(kycDataList, operatorName);

        } catch (Exception e) {
            log.error("Error processing file from SFTP", e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        return kycDataList;
    }

    private List<KycTnmData> parseFile(InputStream inputStream) {
        List<KycTnmData> kycDataList = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] line;
            // csvReader.readNext(); // Skip header
            while ((line = csvReader.readNext()) != null) {
                // Check for missing columns
                if (line.length < 3) {
                    log.warn("Skipping entry due to missing columns: {}", Arrays.toString(line));
                    continue;
                }// Validate MSISDN (must be numeric)
                String msisdn = line[0];
                if (!isNumeric(msisdn)) {
                    log.warn("Skipping entry with non-numeric MSISDN: {}", Arrays.toString(line));
                    continue;
                }
                KycTnmData kycData = new KycTnmData();
                kycData.setMsisdn(msisdn);  // Assuming MSISDN is the first column
                kycData.setIdProofType(line[1]);  // Assuming ID Proof Type is the second column
                kycData.setIdNumber(line[2]);  // Assuming ID Number is the third column
                kycDataList.add(kycData);
            }
        } catch (Exception e) {
            log.error("Error parsing file", e);
        }
        return kycDataList;
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }



    void moveToBackup(String remoteFile, String operatorName) {
        Session session = null;
        ChannelSftp channelSftp = null;
        String backupDir = operatorName.equalsIgnoreCase("airtel") ? airtelBackupDir : tnmBackupDir;
        String remoteDir = operatorName.equalsIgnoreCase("airtel") ? airtelRemoteDir : tnmRemoteDir;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
            session.setPassword(sftpPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            try {
                channelSftp.lstat(remoteDir + "/" + remoteFile);
                log.info("Moving file from {}/{} to {}/{}", remoteDir, remoteFile, backupDir, remoteFile);
                // Move the file to backup location
                channelSftp.rename(remoteDir + "/" + remoteFile, backupDir + "/" + remoteFile);
                log.info("File moved to backup: {}/{}", backupDir, remoteFile);
            } catch (SftpException e) {
                log.error("File not found: {}/{}", remoteDir, remoteFile);
            }

        } catch (Exception e) {
            log.error("Error moving file to backup", e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }


}


//    private String getBackupDirForOperator(String operatorName) {
//        return operatorName.equalsIgnoreCase("airtel") ? airtelBackupDir : tnmBackupDir;
//    }


//    public void saveKycData(KycTnmData kycData) {
//        Optional<KycTnmData> existingKycData = kycDataRepository.findByMsisdnAndIdNumber(kycData.getMsisdn(), kycData.getIdNumber());
//        if (existingKycData.isPresent()) {
//            log.info("Duplicate entry found for msisdn: {} and id_number: {}", kycData.getMsisdn(), kycData.getIdNumber());
//        } else {
//            kycDataRepository.save(kycData);
//        }
//    }


//  private Connection conn;
//  private Connection auditConn;

//    public KycService() {
//        try {
//            DBConnection dbConnection = new DBConnection();
//         //   this.conn = dbConnection.getConnection();
//         //   this.auditConn = dbConnection.getAudConnection();
//        } catch (Exception e) {
//            log.error("Error initializing database connection", e);
//            throw new RuntimeException(e);
//        }
//    }

//    public List<KycData> getKycDataByOperator(String operatorName) {
//        log.info("Fetching KYC data for operator: {}", operatorName);
//        List<KycData> kycDataList = new ArrayList<>();
//
//        // Construct the table name dynamically based on the operatorName
//        String tableName = "kyc_" + operatorName.toLowerCase() + "_data"; // Ensure operatorName is lowercase
//
//        // Query to fetch all records from the dynamically constructed table
//        String query = "SELECT * FROM " + tableName;
//
//        try (Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//
//            while (rs.next()) {
//                KycData kycData = new KycData();
//                kycData.setMsisdn(rs.getString("msisdn"));
//                kycData.setIdProofType(rs.getString("id_proof_type"));
//                kycData.setIdNumber(rs.getString("id_number"));
//                kycDataList.add(kycData);
//            }
//        } catch (SQLException e) {
//            log.error("Error fetching KYC data for operator: {}", operatorName, e);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        return kycDataList;
//    }


//            for (Future<Boolean> future : futures) {
//                if (future.get()) {
//                    numberOfSuccessRecord++;
//                } else {
//                    numberOfFailRecord++;
//                }
//            }

//     executorService.shutdown();


//    private Optional<KycTnmData> findKycDataByMsisdn(String msisdn, String operatorName) throws SQLException {
//        String tableName = "kyc_" + operatorName.toLowerCase() + "_data";
//        String query = "SELECT msisdn, id_proof_type, id_number FROM " + tableName + " WHERE msisdn = ?";
//
//        try (PreparedStatement ps = conn.prepareStatement(query)) {
//            // Set the MSISDN parameter for the query
//            ps.setString(1, msisdn);
//
//            // Execute the query and process the result
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    // Populate the KycData object with the result
//                    KycTnmData data = new KycTnmData();
//                    data.setMsisdn(rs.getString("msisdn"));
//                    data.setIdProofType(rs.getString("id_proof_type"));
//                    data.setIdNumber(rs.getString("id_number"));
//                    return Optional.of(data);
//                }
//            }
//        } catch (SQLException e) {
//            // Log the error for better debugging
//            log.error("Error finding KYC data for MSISDN {}: {}", msisdn, e.getMessage());
//            throw e;
//        }
//
//        // Return an empty optional if no result was found
//        return Optional.empty();
//    }

//    private void updateKycData(KycTnmData kycData, String operatorName) throws SQLException {
//        String tableName = "kyc_" + operatorName.toLowerCase() + "_data";
//        String query = "UPDATE " + tableName + " SET id_proof_type = ?, id_number = ? WHERE msisdn = ?";
//
//        try (PreparedStatement ps = conn.prepareStatement(query)) {
//            // Set the parameters for the prepared statement
//            ps.setString(1, kycData.getIdProofType());
//            ps.setString(2, kycData.getIdNumber());
//            ps.setString(3, kycData.getMsisdn());
//
//            // Execute the update
//            int rowsAffected = ps.executeUpdate();
//
//            if (rowsAffected == 0) {
//                log.warn("No record updated for MSISDN: {}", kycData.getMsisdn());
//            }
//        } catch (SQLException e) {
//            log.error("Error updating KYC data for MSISDN {}: {}", kycData.getMsisdn(), e.getMessage());
//            throw e;  // Rethrow the SQLException to handle it at a higher level
//        }
//    }


//    private void insertKycData(KycTnmData kycData, String operatorName) throws SQLException {
//        String tableName = "kyc_" + operatorName.toLowerCase() + "_data";
//        String query = "INSERT INTO " + tableName + " (msisdn, id_proof_type, id_number) VALUES (?, ?, ?)";
//
//        try (PreparedStatement ps = conn.prepareStatement(query)) {
//            ps.setString(1, kycData.getMsisdn());
//            ps.setString(2, kycData.getIdProofType());
//            ps.setString(3, kycData.getIdNumber());
//
//            int rowsInserted = ps.executeUpdate();
//
//            if (rowsInserted == 0) {
//                log.warn("No record inserted for MSISDN: {}", kycData.getMsisdn());
//            }
//        } catch (SQLException e) {
//            log.error("Error inserting KYC data for MSISDN {}: {}", kycData.getMsisdn(), e.getMessage());
//            throw e;
//        }
//    }
//                    Optional<KycTnmData> existingRecord = findKycDataByMsisdn(record.getMsisdn(), operatorName);
//                    if (existingRecord.isPresent()) {
//                        log.info("Updating record: {}", record);
//                        updateKycData(record, operatorName);
//                    } else {
//                        log.info("Inserting record: {}", record);
//                        insertKycData(record, operatorName);
//                    }
//private void processRecords(List<KycTnmData> kycDataList, String operatorName) {
//    for (KycTnmData record : kycDataList) {
//        Optional<KycTnmData> existingRecord = kycDataRepository.findByMsisdn(record.getMsisdn());
//        if (existingRecord.isPresent()) {
//            // Save current state to history
//            KycTnmData data = existingRecord.get();
//            if (operatorName.equalsIgnoreCase("tnm")) {
//                KycTnmDataHistory history = new KycTnmDataHistory();
//                history.setMsisdn(data.getMsisdn());
//                history.setIdProofType(data.getIdProofType());
//                history.setIdNumber(data.getIdNumber());
//                history.setUpdatedOn(LocalDateTime.now());
//                kycTnmDataHistoryRepository.save(history);
//            } else if (operatorName.equalsIgnoreCase("airtel")) {
//                KycAirtelDataHistory history = new KycAirtelDataHistory();
//                history.setMsisdn(data.getMsisdn());
//                history.setIdProofType(data.getIdProofType());
//                history.setIdNumber(data.getIdNumber());
//                history.setUpdatedOn(LocalDateTime.now());
//                kycAirtelDataHistoryRepository.save(history);
//            }
//
//            // Update existing record
//            data.setIdProofType(record.getIdProofType());
//            data.setIdNumber(record.getIdNumber());
//            kycDataRepository.save(data);
//        } else {
//            // Insert new record
//            kycDataRepository.save(record);
//        }
//    }
//}
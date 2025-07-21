package com.microsoft.azure.samples;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import com.azure.identity.*;
import java.util.logging.Logger;
import java.io.ByteArrayInputStream;

/**
 * Azure Functions with Event Grid Blob Trigger.
 */
public class ProcessBlobUpload {
    
    private static BlobServiceClient blobServiceClient = null;
    private static final DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

    /**
     * This function is triggered by Event Grid when a blob is created in the unprocessed-pdf container.
     * It processes the blob and copies it to the processed-pdf container.
     */
    @FunctionName("processBlobUpload")
    @StorageAccount("PDFProcessorSTORAGE")
    public void processBlobUpload(
        @BlobTrigger(
            name = "blob",
            path = "unprocessed-pdf/{name}",
            source = "EventGrid"
        ) byte[] blobContent,
        @BindingName("name") String blobName,
        final ExecutionContext context) {

        Logger logger = context.getLogger();
        int fileSize = blobContent.length;
        
        logger.info(String.format("Java Blob Trigger (using Event Grid) processed blob\n Name: %s \n Size: %d bytes", 
                                  blobName, fileSize));

        try {
            // Copy to processed container - simple demonstration of an async operation
            copyToProcessedContainer(blobContent, "processed_" + blobName, logger);
            
            logger.info(String.format("PDF processing complete for %s", blobName));
        } catch (Exception error) {
            logger.severe(String.format("Error processing blob %s: %s", blobName, error.getMessage()));
            throw new RuntimeException(error);
        }
    }

    /**
     * Simple method to demonstrate uploading the processed PDF
     */
    private void copyToProcessedContainer(byte[] blobBuffer, String blobName, Logger logger) {
        logger.info(String.format("Starting copy operation for %s", blobName));
        
        try {
            // Get the reusable BlobServiceClient
            BlobServiceClient serviceClient = getBlobServiceClient(logger);
            
            // Get container client for processed PDFs
            BlobContainerClient containerClient = serviceClient.getBlobContainerClient("processed-pdf");
            
            // Get blob client
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            // Upload the blob
            blobClient.upload(new ByteArrayInputStream(blobBuffer), blobBuffer.length, true);
            
            logger.info(String.format("Successfully copied %s to processed-pdf container", blobName));
        } catch (Exception error) {
            logger.severe(String.format("Failed to copy %s to processed container: %s", blobName, error.getMessage()));
            throw new RuntimeException(error);
        }
    }

    /**
     * Get or create a BlobServiceClient instance
     */
    private BlobServiceClient getBlobServiceClient(Logger logger) {
        if (blobServiceClient == null) {
            // For local development, use the connection string directly
            String connectionString = System.getenv("PDFProcessorSTORAGE");
            
            if (connectionString == null || connectionString.isEmpty()) {
                logger.severe("Storage connection string not found. Expected PDFProcessorSTORAGE__serviceUri environment variable.");
                connectionString = System.getenv("PDFProcessorSTORAGE__serviceUri");
            }
            
            // Check if running locally with Azurite
            if ("UseDevelopmentStorage=true".equals(connectionString)) {
                // Use Azurite connection string
                blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            } else {
                // Create BlobServiceClient using the storage account URL and managed identity credentials
                blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(connectionString)
                    .credential(credential)
                    .buildClient();
            }
        }
        
        return blobServiceClient;
    }
}
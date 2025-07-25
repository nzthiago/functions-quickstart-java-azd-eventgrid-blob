package com.microsoft.azure.samples;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.azure.storage.blob.*;
import java.util.logging.Logger;
import java.io.InputStream;

/**
 * Azure Functions with Event Grid Blob Trigger using SDK type bindings.
 */
public class ProcessBlobUpload {

    /**
     * This function is triggered by Event Grid when a blob is created in the unprocessed-pdf container.
     * It processes the blob and copies it to the processed-pdf container using SDK type bindings.
     */
    @FunctionName("processBlobUpload")
    @StorageAccount("PDFProcessorSTORAGE")
    public void processBlobUpload(
        @BlobTrigger(
            name = "blob",
            path = "unprocessed-pdf/{name}",
            source = "EventGrid"
        ) InputStream blobContent,
        @BindingName("name") String blobName,
        @BlobInput(
            name = "containerClient",
            path = "processed-pdf",
            connection = "PDFProcessorSTORAGE"
        ) BlobContainerClient containerClient,
        final ExecutionContext context) {

        Logger logger = context.getLogger();
        
        logger.info(String.format("Java Blob Trigger (using Event Grid) processed blob\n Name: %s", blobName));

        try {
            // Copy to processed container using the bound container client
            copyToProcessedContainer(blobContent, "processed_" + blobName, containerClient, logger);
            
            logger.info(String.format("PDF processing complete for %s", blobName));
        } catch (Exception error) {
            logger.severe(String.format("Error processing blob %s: %s", blobName, error.getMessage()));
            throw new RuntimeException(error);
        }
    }

    /**
     * Simple method to demonstrate uploading the processed PDF using the bound container client
     */
    private void copyToProcessedContainer(InputStream blobStream, String blobName, BlobContainerClient containerClient, Logger logger) {
        logger.info(String.format("Starting copy operation for %s", blobName));
        
        try {
            // Get blob client for the processed blob
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            // Upload the blob from the input stream
            blobClient.upload(blobStream, true);
            
            logger.info(String.format("Successfully copied %s to processed-pdf container", blobName));
        } catch (Exception error) {
            logger.severe(String.format("Failed to copy %s to processed container: %s", blobName, error.getMessage()));
            throw new RuntimeException(error);
        }
    }
}
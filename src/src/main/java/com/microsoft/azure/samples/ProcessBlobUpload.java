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

        logger.info(String.format("Java Blob Trigger (using Event Grid) processed blob\n Name: %s \n", blobName));

        try {
            String processedBlobName = "processed_" + blobName;
            // Get blob client for the processed blob
            BlobClient blobClient = containerClient.getBlobClient(processedBlobName);
            
            // Upload the blob from the input stream
            blobClient.upload(blobContent, true);
            
            logger.info(String.format("PDF processing complete for %s", processedBlobName));
        } catch (Exception error) {
            logger.severe(String.format("Error processing blob %s: %s", blobName, error.getMessage()));
            throw new RuntimeException(error);
        }
    }
}
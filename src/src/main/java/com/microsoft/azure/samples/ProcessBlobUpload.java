package com.microsoft.azure.samples;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobClient;
import java.util.logging.Logger;

/**
 * Azure Functions with Event Grid Blob Trigger using SDK type bindings.
 */
public class ProcessBlobUpload {

    @FunctionName("ProcessBlobUpload")
    @StorageAccount("PDFProcessorSTORAGE")
    public void run(
        @BlobTrigger(
            name = "sourceBlob",
            path = "unprocessed-pdf/{name}",
            source = "EventGrid"
        ) BlobClient sourceBlob,
        @BindingName("name") String blobName,
        @BlobInput(
            name = "outputBlobContainerClient",
            path = "processed-pdf"
        ) BlobContainerClient outputBlobContainerClient,
        final ExecutionContext context) {

        Logger logger = context.getLogger();
        
        try {
            long blobSize = sourceBlob.getProperties().getBlobSize();
            logger.info(String.format("Java Blob Trigger (using Event Grid) processed blob\n Name: %s \n Size: %d bytes", blobName, blobSize));

            // Copy the blob to the processed container with a new name
            String newBlobName = "processed-" + blobName;
            BlobClient outputBlob = outputBlobContainerClient.getBlobClient(newBlobName);
            
            if (outputBlob.exists()) {
                logger.info(String.format("Blob %s already exists in the processed container. Skipping upload.", newBlobName));
                return;
            }

            // Here you can add any processing logic for the input blob before uploading it to the processed container.

            // Uploading the blob to the processed container using streams. You could add processing of the input stream logic here if needed.
            outputBlob.upload(sourceBlob.openInputStream(), true);
            logger.info(String.format("PDF processing complete for %s. Blob copied to processed container with new name %s.", blobName, newBlobName));
            
        } catch (Exception error) {
            logger.severe(String.format("Error processing blob %s: %s", blobName, error.getMessage()));
            throw new RuntimeException(error);
        }
    }
}
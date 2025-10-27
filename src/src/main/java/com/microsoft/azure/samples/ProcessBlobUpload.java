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

    @FunctionName("processBlobUpload")
    @StorageAccount("PDFProcessorSTORAGE")
    public void processBlobUpload(
        @BlobTrigger(
            name = "inputBlob",
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
        long blobSize = sourceBlob.getProperties().getBlobSize();
        logger.info(String.format("Java Blob Trigger (using Event Grid) processed blob\n Name: %s \n Size: %d bytes\n", blobName, blobSize));

        try {
            
            String processedBlobName = "processed_" + blobName;
            BlobClient outputBlob = outputBlobContainerClient.getBlobClient(processedBlobName);

            outputBlob.upload(sourceBlob.openInputStream(), true);
            logger.info(String.format("PDF processing complete for %s", processedBlobName));
        } catch (Exception error) {
            logger.severe(String.format("Error processing blob %s: %s", blobName, error.getMessage()));
            throw new RuntimeException(error);
        }
    }
}
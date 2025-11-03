package com.microsoft.azure.samples;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import java.util.logging.Logger;

/**
 * Azure Functions with Event Grid Blob Trigger using byte array bindings.
 */
public class ProcessBlobUpload {

    @FunctionName("ProcessBlobUpload")
    @StorageAccount("PDFProcessorSTORAGE")
    public void run(
        @BlobTrigger(
            name = "sourceBlob",
            path = "unprocessed-pdf/{name}",
            source = "EventGrid"
        ) byte[] sourceBlob,
        @BindingName("name") String blobName,
        @BlobOutput(
            name = "outputBlob",
            path = "processed-pdf/processed-{name}"
        ) OutputBinding<byte[]> outputBlob,
        final ExecutionContext context) {

        Logger logger = context.getLogger();
        
        try {
            long blobSize = sourceBlob.length;
            logger.info(String.format("Java Blob Trigger (using Event Grid) processed blob\n Name: %s \n Size: %d bytes", blobName, blobSize));

            // Here you can add any processing logic for the input blob before uploading it to the processed container.
            // For this example, we're just copying the blob content as-is
            
            outputBlob.setValue(sourceBlob);
            logger.info(String.format("PDF processing complete for %s. Blob copied to processed container with new name processed-%s.", blobName, blobName));
            
        } catch (Exception error) {
            logger.severe(String.format("Error processing blob %s: %s", blobName, error.getMessage()));
            throw new RuntimeException(error);
        }
    }
}
package com.om.integration.deal.om_api_integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "RefundResponse")
public class RefundResponse {
    @JsonProperty("MD5OfMessageBody")
    private String md5OfMessageBody;

    @JsonProperty("MD5OfMessageAttributes")
    private String md5OfMessageAttributes;

    @JsonProperty("MessageId")
    private String messageId;

    @JsonProperty("ResponseMetadata")
    private ResponseMetadata responseMetadata;

        @Data
        public static class ResponseMetadata {
            @JsonProperty("RequestId")
            private String requestId;

            @JsonProperty("HTTPStatusCode")
            private int httpStatusCode;

            @JsonProperty("HTTPHeaders")
            private HTTPHeaders httpHeaders;

            @JsonProperty("RetryAttempts")
            private int retryAttempts;

            @Data
            public static class HTTPHeaders {
                @JsonProperty("x-amzn-requestid")
                private String xAmznRequestId;
                @JsonProperty("x-amzn-trace-id")
                private String xAmznTraceId;
                private String date;
                private String contentType;
                private String contentLength;
            }
        }

}

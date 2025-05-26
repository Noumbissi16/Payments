package com.om.integration.deal.om_api_integration.model;


import com.om.integration.deal.om_api_integration.model.ennum.TransactionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "Transaction")
public class Transaction {

    @Id
    private String id;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String omTransactionId; // txnid

    private String orderId; // orderId

    private String passCode; // passcode

    private String amount; // amount

    private String createdTime; // createtime

    private String payerNumber; //subscriberMsisdn: Person that receives the money

    private String payToken; // payToken

    private String omTransactionDescription; // txnmode

    private String omInitTransactionMessage; //inittxnmessage

    private String initTransactionStatus; //inittxnstatus

    private String confirmTransactionStatus; //confirmtxnstatus

    private String confirmTransactionMessage; //confirmtxnmessage

    private String status;

    private String notificationUrl; // notifUrl

    private TransactionTypeEnum transactionType = TransactionTypeEnum.CASH_IN;

    private String channelUserMsisdn; // channelUserMsisdn: Person that sends the money (always our msisdn)

    private String fromChannelMsisdn; // fromChannelMsisdn: In case of cashout, store the OM money Number they sent us

    private String description;

    private String messageId; // MessageId from RefundResponse

    private String refundStep; // RefundStep from TransactionStatusResponse
}

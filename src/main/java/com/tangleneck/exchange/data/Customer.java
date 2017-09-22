package com.tangleneck.exchange.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "customers")
@Getter
@Setter
public class Customer {

    @Id
    private String id;

    private String email;

    private String password;

    private Date registrationDate;

    private Date confirmationDate;

}

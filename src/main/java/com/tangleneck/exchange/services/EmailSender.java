package com.tangleneck.exchange.services;

import com.sendgrid.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
class EmailSender {

    @Value("${mail.from}")
    private String fromEmail;

    @Value("${mail.apikey}")
    private String apiKey;

    Mono<Void> send(String id, String email) {
        SendGrid sendgrid = new SendGrid(apiKey);

        Email from = new Email(fromEmail);
        Email to = new Email(email);

        String subject = "Tangleneck - Email confirmation";

        String uri = "https://dashboard.heroku.com/apps/lit-ravine-74924/tangleneck/v1/confirmation?code=" + id;
        Content content = new Content("text/plain", "Hello, Thanks for registering to Tangleneck. \n" +
                "To activate your account, click on the following link: \n " + uri + ")");

        Mail mail = new Mail(from, subject, to, content);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");

        try {
            request.setBody(mail.build());
            sendgrid.api(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }

}

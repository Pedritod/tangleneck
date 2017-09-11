package com.tangleneck.exchange.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by poca on 9/11/2017.
 */
@RestController
public class LoginController {

    @GetMapping("/pepe")
    public String fetchAddresses() {
        return "hola";
    }
}

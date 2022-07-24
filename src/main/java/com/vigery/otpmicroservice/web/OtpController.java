package com.vigery.otpmicroservice.web;

import com.vigery.otpmicroservice.dao.SessionDao;
import com.vigery.otpmicroservice.model.TokenRequest;
import com.vigery.otpmicroservice.service.EmailService;
import com.vigery.otpmicroservice.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDateTime;


@RestController
@ResponseStatus(HttpStatus.OK)
public class OtpController {

    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);

    @Autowired
    private SessionDao sessionRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TokenService tokenService;

    @Value("${http.port}")
    private int port;
    @Value("${http.url}")
    private String httpUrl;
    @Value("${smtp.ip}")
    private String smtpIp;
    @Value("${smtp.port}")
    private int smtpPort;
    @Value("${smtp.ttl}")
    private int smtpTtl;
    @Value("${smtp.username}")
    private int smtpUsername;
    @Value("${smtp.password}")
    private int smtpPassword;

    @GetMapping(value="/")
    public void isRunning(){ }

    @PostMapping(value="/otp")
    public ResponseEntity<TokenRequest> TokenCreation(@RequestBody TokenRequest tokenRequest) throws Exception {

        String token = tokenService.getRandomToken();

        String sessionId = tokenService.getRandomSessionId();

        sessionRepository.session(token, sessionId, LocalDateTime.now().plusMinutes(tokenRequest.getTtl()));
        emailService.sendToken(tokenRequest.getAddress(), tokenRequest.getObject(), tokenRequest.getContent().replace("%token%",token));

        return ResponseEntity.status(HttpStatus.CREATED).header("Location",httpUrl+"/otp/"+sessionId).body(tokenRequest);
    }

    @PostMapping(value="/otp/{sessionId}")
    public ResponseEntity<String> Auth(@PathVariable int sessionId, @RequestBody String token){

        try {
             if (sessionRepository.notExpired(String.valueOf(sessionId),token)){
                 return ResponseEntity.status(HttpStatus.OK).body("OK");
             }else{
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refus");
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur");
        } catch (InterruptedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur");
        }

    }



}

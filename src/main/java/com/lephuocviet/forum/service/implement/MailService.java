package com.lephuocviet.forum.service.implement;

import com.lephuocviet.forum.dto.requests.MailRequest;
import com.lephuocviet.forum.dto.requests.PasswordRequest;
import com.lephuocviet.forum.dto.responses.MailResponse;
import com.lephuocviet.forum.enity.Accounts;
import com.lephuocviet.forum.enity.Mail_sender;
import com.lephuocviet.forum.enity.Users;
import com.lephuocviet.forum.enums.ErrorCode;
import com.lephuocviet.forum.exception.WebException;
import com.lephuocviet.forum.repository.AccountsRepository;
import com.lephuocviet.forum.repository.Mail_SenderRepository;
import com.lephuocviet.forum.repository.Token_InvalidRepository;
import com.lephuocviet.forum.repository.UsersRepository;
import com.lephuocviet.forum.service.IMailService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MailService implements IMailService {

    final Mail_SenderRepository mailSenderRepository;
    final UsersRepository usersRepository;
    final AccountsRepository accountsRepository;
    final Token_InvalidRepository tokenInvalidRepository;

    final JavaMailSender javaMailSender;

    final PasswordEncoder passwordEncoder;

    @Value("${security.jwt.signer_Key}")
    String SIGNER_KEY;

    @Value("${MAIL.URL}")
    String MAIL_URL;

    @Override
    public MailResponse sendMailActive() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users users = usersRepository.findUserByUsername(username)
                .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));
        Accounts accounts = accountsRepository.findAccountsByUsername(username)
                .orElseThrow(() -> new WebException(ErrorCode.USERNAME_NOT_FOUND));
        if (accounts.isActive()) throw new WebException(ErrorCode.ACCOUNT_IS_ACTIVE);
        String token = generateTokenActive(accounts);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(users.getEmail());
        simpleMailMessage.setSubject("Forum Language");
        simpleMailMessage.setFrom("vietyts2003@gmail.com");
        simpleMailMessage.setText("Please click on the following link to verify your email.\n" +
                "Link is only valid for 5 minutes \n" +
                MAIL_URL + "/confirmEmail?token=" + token);
        if (mailSenderRepository.existsById(accounts.getId())) {
            mailSenderRepository.deleteById(accounts.getId());
        }
        Mail_sender mailSender = Mail_sender.builder()
                .id(accounts.getId())
                .date_created(LocalDate.now())
                .email(users.getEmail())
                .token(token)
                .build();

        mailSenderRepository.save(mailSender);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable deletedMail = () -> mailSenderRepository.deleteById(accounts.getId());
        scheduledExecutorService.schedule(deletedMail, 6, java.util.concurrent.TimeUnit.MINUTES);
        scheduledExecutorService.shutdown();
        try {
            javaMailSender.send(simpleMailMessage);
        }catch (Exception e){
            throw new WebException(ErrorCode.EMAIL_INVALID);
        }
        return MailResponse.builder()
                .token(token)
                .success(true)
                .build();

    }

    @Override
    public MailResponse sendMailResetPassword(MailRequest mailRequest) {
        Users users = usersRepository.findUserByEmail(mailRequest.getEmail())
                .orElseThrow(()-> new WebException(ErrorCode.USER_NOT_FOUND));
        Accounts accounts = accountsRepository.findAccountsById(users.getAccounts().getId())
                .orElseThrow(() -> new WebException(ErrorCode.ACCOUNT_NOT_FOUND));
        String token = generateTokenReset(accounts);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(users.getEmail());
        simpleMailMessage.setSubject("Forum Language");
        simpleMailMessage.setFrom("vietyts2003@gmail.com");
        simpleMailMessage.setText("Please click on the following link to change your password.\n" +
                "Link is only valid for 5 minutes \n" +
                MAIL_URL + "/reset-password.html?token=" + token);
        if (mailSenderRepository.existsById(accounts.getId())) {
            mailSenderRepository.deleteById(accounts.getId());
        }
        Mail_sender mailSender = Mail_sender.builder()
                .id(accounts.getId())
                .date_created(LocalDate.now())
                .email(users.getEmail())
                .token(token)
                .build();

        mailSenderRepository.save(mailSender);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable deletedMail = () -> mailSenderRepository.deleteById(accounts.getId());
        scheduledExecutorService.schedule(deletedMail, 6, java.util.concurrent.TimeUnit.MINUTES);
        scheduledExecutorService.shutdown();
        javaMailSender.send(simpleMailMessage);
        return MailResponse.builder()
                .token(token)
                .success(true)
                .build();
    }

    @Override
    public MailResponse checkMailActive(String token) throws ParseException, JOSEException {
        try {
            SignedJWT signedJWT = verifyToken(token);
            String username = signedJWT.getJWTClaimsSet().getSubject();
            String name = signedJWT.getJWTClaimsSet().getClaim("active").toString();
            if (!name.equals("active")) throw new WebException(ErrorCode.TOKEN_INVALID);
            Accounts accounts = accountsRepository.findAccountsByUsername(username)
                    .orElseThrow(() -> new WebException(ErrorCode.USERNAME_NOT_FOUND));
            if (!mailSenderRepository.existsById(accounts.getId())) throw new WebException(ErrorCode.TOKEN_INVALID);
            accounts.setActive(true);
            accountsRepository.save(accounts);
            mailSenderRepository.deleteById(accounts.getId());
            return MailResponse.builder()
                    .message("Active successfully")
                    .success(true)
                    .build();
        } catch (Exception e) {
            throw new WebException(ErrorCode.TOKEN_INVALID);
        }

    }


    @Override
    public MailResponse resetPassword(String token, PasswordRequest passwordRequest) {
        try {
            SignedJWT signedJWT = verifyToken(token);
            String username = signedJWT.getJWTClaimsSet().getSubject();
            String name = signedJWT.getJWTClaimsSet().getClaim("reset").toString();
            if (!name.equals("reset")) throw new WebException(ErrorCode.TOKEN_INVALID);
            Accounts accounts = accountsRepository.findAccountsByUsername(username)
                    .orElseThrow(() -> new WebException(ErrorCode.USERNAME_NOT_FOUND));
            if (!passwordRequest.getPassword().equals(passwordRequest.getRepassword()))
                throw new WebException(ErrorCode.PASSWORD_NOT_MATCH);
            if (!mailSenderRepository.existsById(accounts.getId())) throw new WebException(ErrorCode.TOKEN_INVALID);
            accounts.setPassword(passwordEncoder.encode(passwordRequest.getPassword()));
            accountsRepository.save(accounts);
            mailSenderRepository.deleteById(accounts.getId());
            return MailResponse.builder()
                    .message("Reset successfully")
                    .success(true)
                    .build();
        } catch (Exception e) {
            throw new WebException(ErrorCode.TOKEN_INVALID);
        }

    }

    String generateTokenActive(Accounts account) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getUsername())
                .issuer("forum")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(5, java.time.temporal.ChronoUnit.MINUTES).toEpochMilli()
                ))
                .jwtID(java.util.UUID.randomUUID().toString())
                .claim("active", "active")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

    }

    String generateTokenReset(Accounts account) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getUsername())
                .issuer("forum")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(5, java.time.temporal.ChronoUnit.MINUTES).toEpochMilli()
                ))
                .jwtID(java.util.UUID.randomUUID().toString())
                .claim("reset", "reset")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

    }

    SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verify = signedJWT.verify(verifier);
        if (!(verify || expiration.before(new Date())))
            throw new WebException(ErrorCode.TOKEN_INVALID);
        if (tokenInvalidRepository.existsByIdToken(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new WebException(ErrorCode.TOKEN_INVALID);
        return signedJWT;
    }

}

package com.lephuocviet.forum.service.implement;

import com.lephuocviet.forum.configuration.VnPayConfig;
import com.lephuocviet.forum.dto.responses.VnPayResponse;
import com.lephuocviet.forum.enity.*;
import com.lephuocviet.forum.enums.ErrorCode;
import com.lephuocviet.forum.enums.TypeOrdersCode;
import com.lephuocviet.forum.exception.WebException;
import com.lephuocviet.forum.mapper.TransactionMapper;
import com.lephuocviet.forum.repository.*;
import com.lephuocviet.forum.service.IVnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VnPayService implements IVnPayService {

    VnPayConfig vnPayConfig;
    AccountsRepository accountsRepository;
    TransactionRepository transactionRepository;
    TransactionMapper transactionMapper;
    PostsRepository postsRepository;
    AdsRepository adsRepository;
    SalesPackageRepository salesPackageRepository;

    public VnPayResponse createOrder(HttpServletRequest request, String location, String type, String idHandler, String sales_package) {

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = UUID.randomUUID().toString();
        String vnp_IpAddr = VnPayConfig.getIpAddress(request);
        String vnp_TmnCode = vnPayConfig.getVnp_TmnCode();
        String orderType = "order-type";
        SalesPackage salesPackage = salesPackageRepository.findAdsPackageByName(sales_package)
                .orElseThrow(() -> new WebException(ErrorCode.PACKAGE_NOT_FOUND));
        Accounts accounts = accountsRepository.findAccountsByUsername(
                        SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new WebException(ErrorCode.ACCOUNT_NOT_FOUND));
        int amount;

        String orderInfor = accounts.getUsername().substring(0, 2) + idHandler;


        String vnp_CurrCode;
        String vnp_Locale;
        if (location.equals("vn")) {
            vnp_CurrCode = "VND";
            vnp_Locale = "vn";
            amount = salesPackage.getPrice_vnd();
        } else {
            vnp_CurrCode = "USD";
            vnp_Locale = "en";
            amount = salesPackage.getPrice_usd();
        }
        BigDecimal bigDecimal = BigDecimal.valueOf(amount);
        if (!type.equals(TypeOrdersCode.VIP.toString()) && !type.equals(TypeOrdersCode.ADS.toString()))
            throw new WebException(ErrorCode.TYPE_ORDER_IS_WRONG);


        Transaction transaction = Transaction.builder()
                .id(vnp_TxnRef)
                .date_created(LocalDate.now())
                .amount(bigDecimal)
                .content(orderInfor)
                .status(false)
                .accounts(accounts)
                .type(type)
                .salesPackage(salesPackage)
                .unit(vnp_CurrCode)
                .build();


        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfor);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        baseUrl += vnPayConfig.getVnp_Returnurl();
        vnp_Params.put("vnp_ReturnUrl", baseUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {

                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String salt = vnPayConfig.getVnp_HashSecret();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(salt, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        transactionRepository.save(transaction);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable deletedTransaction = () -> transactionRepository.delete(transaction);
        scheduledExecutorService.schedule(deletedTransaction, 16, TimeUnit.MINUTES);
        return VnPayResponse.builder()
                .success(true)
                .url(paymentUrl)
                .result(transactionMapper.toTransactionResponse(transaction))
                .build();
    }

    public VnPayResponse orderReturn(HttpServletRequest request) {
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_TxnRef = request.getParameter("vnp_TxnRef");
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        String vnp_OrderInfo = request.getParameter("vnp_OrderInfo");
        String idHandler = vnp_OrderInfo.substring(2);
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = VnPayConfig.hashAllFields(fields, vnPayConfig.getVnp_HashSecret());
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                Transaction transaction = transactionRepository.findTransactionById(vnp_TxnRef)
                        .orElseThrow(() -> new WebException(ErrorCode.TRANSACTION_NOT_FOUND));

                transaction.setStatus(true);
                transactionRepository.save(transaction);
                if (transaction.getType().equals(TypeOrdersCode.ADS.toString())) {
                    Posts posts = postsRepository.findPostsById(idHandler)
                            .orElseThrow(() -> new WebException(ErrorCode.POST_NOT_FOUND));

                    AdsPost ads = AdsPost.builder()
                            .post(posts)
                            .desired_views(transaction.getSalesPackage().getDescription())
                            .views(0)
                            .build();
                    adsRepository.save(ads);
                    return VnPayResponse.builder()
                            .result(AdsPost.builder()
                                    .desired_views(ads.getDesired_views())
                                    .views(ads.getViews())
                                    .build())
                            .success(true)
                            .build();
                }

            } else {
                return VnPayResponse.builder()
                        .success(false)
                        .build();
            }
        } else {
            return VnPayResponse.builder()
                    .success(false)
                    .build();
        }
        return VnPayResponse.builder()
                .success(false)
                .build();
    }


}


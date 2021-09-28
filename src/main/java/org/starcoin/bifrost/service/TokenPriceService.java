package org.starcoin.bifrost.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class TokenPriceService {

    @Value("${token-price-service.get-wei-to-nano-stc-exchange-rate-url}")
    private String getWeiToNanoStcExchangeRateUrl;

    @Autowired
    private RestTemplate restTemplate;

    public BigDecimal getWeiToNanoStcExchangeRate() {
        String priceStr = restTemplate.getForObject(getWeiToNanoStcExchangeRateUrl, String.class);
        if (priceStr.startsWith("\"") && priceStr.endsWith("\"")) {
            priceStr = priceStr.substring(1, priceStr.length() - 1);
        }
        return new BigDecimal(priceStr);
    }
}

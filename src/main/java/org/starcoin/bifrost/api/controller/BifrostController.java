package org.starcoin.bifrost.api.controller;


import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;
import org.starcoin.bifrost.data.model.AbstractEthereumTransaction;
import org.starcoin.bifrost.data.model.StarcoinEvent;
import org.starcoin.bifrost.data.repo.EthereumTransactionRepository;
import org.starcoin.bifrost.data.repo.StarcoinEventRepository;
import org.starcoin.bifrost.service.EthereumTransactionOnChainService;
import org.starcoin.bifrost.service.EthereumTransactionServiceFacade;
import org.starcoin.bifrost.service.StarcoinTransactionService;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@Api(tags = {"Bifrost Relay API"})
@RestController
@RequestMapping("v1/bifrost-relay")
public class BifrostController {

    @Resource
    private StarcoinEventRepository starcoinEventRepository;

    @Resource
    private StarcoinTransactionService starcoinTransactionService;

    @Resource
    private EthereumTransactionRepository ethereumTransactionRepository;

    @Resource
    private EthereumTransactionOnChainService ethereumTransactionOnChainService;

    @Resource
    private EthereumTransactionServiceFacade ethereumTransactionServiceFacade;

    @GetMapping("starcoinEvents")
    public List<StarcoinEvent> getStarcoinEvents(@RequestParam(value = "status", required = true) String status) {
        return starcoinEventRepository.findByStatusEquals(status);
    }

    @GetMapping("ethereumTransactions")
    public List<AbstractEthereumTransaction> getEthereumTransactions(@RequestParam(value = "status", required = true) String status) {
        return ethereumTransactionRepository.findByStatusEquals(status);
    }

    @GetMapping("eth/gasPrice")
    public @ResponseBody
    String getEthereumGasPrice() throws IOException {
        return ethereumTransactionOnChainService.getOnChainGasPrice().toString();
    }

    @GetMapping("eth/estimateDepositFromStarcoinGas")
    public @ResponseBody
    String estimateDepositFromStarcoinGas(@RequestParam(value = "to") String to,
                                          @RequestParam(value = "amount") BigInteger amount,
                                          @RequestParam(value = "gasPrice") BigInteger gasPrice
    ) throws IOException {
        return ethereumTransactionServiceFacade.estimateDepositFromStarcoinGas(to, amount, gasPrice).toString();
    }

    @GetMapping("exchangeRates/WEI_NANOSTC")
    public @ResponseBody
    String getWeiToNanoStcExchangeRate() {
        return ethereumTransactionServiceFacade.getWeiToNanoStcExchangeRate().toString();
    }

}

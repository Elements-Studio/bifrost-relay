# bifrost-relay

跨链后台任务及 API 服务。

## API 说明

### 获取 Starcoin 链上事件列表

目前主要是 STC -> EVM 兼容链的跨链事件的列表。

例子：

```url
http://localhost:8680/v1/bifrost-relay/starcoinEvents?status=CONFIRMED
```

输出结果示例：

```json
[
  {
    "eventId": "9db7599af8a980930ae3366f47ecc63e9538d64735da87bb62d226a56f1881f2",
    "blockHash": "0x50c84b1814e8fbc9d7f2736125161a95432874e6067022c7a0703240fba977ef",
    "blockNumber": 2819,
    "transactionHash": "0xae86af4fb271f505c3a63d9cf50b96f7e46e4d610722e5a1fdad799639d30b95",
    "transactionIndex": 1,
    "eventKey": "0x00000000000000000000000000000000000000000a550c18",
    "eventSequenceNumber": 10,
    "typeTag": "0x00000000000000000000000000000001::Account::WithdrawEvent",
    "data": "0x00ca9a3b00000000000000000000000000000000000000000000000000000001035354430353544300",
    "status": "CONFIRMED",
    "createdBy": "ADMIN",
    "updatedBy": "ADMIN",
    "createdAt": 1629262806927,
    "updatedAt": 1629262810803,
    "version": 1,
    "mintAmount": 2.000000001e+28,
    "mintAccount": "0xBf5A160865B70C2047f24eEE94cD04880Ddee1e8",
    "depositAmount": 2.000000001e+28,
    "gasPriceInWei": 20000000000,
    "estimatedGas": 62209,
    "weiToNanoStcExchangeRate": 0.000020387
  }
]
```

请求参数：

* status（必须）：
  * CREATED：事件已创建（等待一定的区块数量确认）。
  * CONFIRMED：事件已确认。
  * DROPPED：已丢弃（未得到链上的最终确认）。


### 获取 Ethereum 跨链交易列表

获取以太坊或 EVM 兼容链上的跨链交易列表。

例子：

```url
http://localhost:8680/v1/bifrost-relay/ethereumTransactions?status=CONFIRMED
```

输出结果示例：

```json
[
  {
    "transactionHash": "0x01ad91b0af275d7c8849962979243e4a45d78ca84fdc91ca62b0aa775ee6f5f1",
    "triggerEventId": "TEST:35df4f58-25df-446b-a283-67d1d57650c0",
    "recipient": "0x3d8c97DF6A0948f11273b0367579B1f0413DE0C4",
    "blockHash": "0x56103d662359ffbb443198b0800208a5d3eb784267ac3ce03cae5c095ad06a07",
    "blockNumber": 474,
    "gasPrice": 20000000000,
    "gasLimit": 1000000,
    "gasUsed": null,
    "transactionIndex": 0,
    "type": null,
    "input": "0x733e0ab9000000000000000000000000bf5a160865b70c2047f24eee94cd04880ddee1e80000000000000000000000000000000000000000813f3978f89409843fffffff",
    "value": 0,
    "v": null,
    "r": null,
    "s": null,
    "status": "CONFIRMED",
    "createdBy": "ADMIN",
    "updatedBy": "ADMIN",
    "createdAt": 1629381082106,
    "updatedAt": 1629381112277,
    "accountAddress": "0x414d6e7b82e9ce949d468d502057483195c69df9",
    "accountNonce": 472,
    "mintAccount": "0xBf5A160865B70C2047f24eEE94cD04880Ddee1e8",
    "mintAmount": 4e+28,
    "unexpectedTransactionHash": null,
    "version": 4,
    "rawTransaction": null,
    "credentials": null,
    "signedMessage": null
  }
]
```

请求参数：

* status（必须）：
  * CREATED：已创建（未发送）。
  * SENT：已发送（已向链上发送）。 
  * CONFIRMED：事件已确认。
  * DROPPED：已丢弃。链上存在已经确认的、使用同一个账号地址以及 nonce 的另外一个交易。将此交易移除到独立的历史记录表后，可以使用新的 nonce 重新创建交易。
  * CANCELED：已取消（未发送即取消）。
  * TOMBSTONED：异常的“死记录”。已被链上确认为丢弃，但仍然保存在交易表中以防止重复创建此交易。

### 获取 Token 价格

#### 获取以太坊 Wei 兑 NanoSTC 汇率

例子：

```url
http://localhost:8680/v1/bifrost-relay/exchangeRates/WEI_NANOSTC
```

### 计算 Ethereum 链的 Gas 费用

#### 获取 Ethereum 链上的当前 Gas 价格

例子：

```url
http://localhost:8680/v1/bifrost-relay/eth/gasPrice
```

#### 估算将 STC 存入 Ethereum 链需要的 Gas 数量 

例子：

```url
http://localhost:8680/v1/bifrost-relay/eth/estimateDepositFromStarcoinGas?to=0x71DFDD2BF49E8Af5226E0078efA31ecf258bC44E&amount=2333333333333&gasPrice=20000000
```

请求参数：

  * to：存入的地址。
  * amount：存入的数量（NanoSTC）。
  * gasPrice：每单位的 Gas 价格。


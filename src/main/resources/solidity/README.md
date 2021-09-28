
```shell
cd ~/Documents/starcoinorg/bifrost-ethernum/contracts
solc ./tokens/STC.sol --bin --abi --optimize --allow-paths ~/Documents/starcoinorg/bifrost-ethernum/contracts --overwrite -o ./bin
cp -r ./bin ~/Documents/starcoinorg/bifrost-relay/src/main/resources/solidity
cd ~/Documents/starcoinorg/bifrost-relay/src/main/resources/solidity/bin
#web3j generate solidity -a=SafeMath.abi -b=SafeMath.bin -o=../../../../generated/java -p=org.starcoin.bifrost.ethereum.model
#web3j generate solidity -a=Address.abi -b=Address.bin -o=../../../../generated/java -p=org.starcoin.bifrost.ethereum.model
#web3j generate solidity -a=Context.abi -b=Context.bin -o=../../../../generated/java -p=org.starcoin.bifrost.ethereum.model
#web3j generate solidity -a=ReentrancyGuard.abi -b=ReentrancyGuard.bin -o=../../../../generated/java -p=org.starcoin.bifrost.ethereum.model
#web3j generate solidity -a=Ownable.abi -b=Ownable.bin -o=../../../../generated/java -p=org.starcoin.bifrost.ethereum.model
#web3j generate solidity -a=IERC20.abi -b=IERC20.bin -o=../../../../generated/java -p=org.starcoin.bifrost.ethereum.model
#web3j generate solidity -a=SafeERC20.abi -b=SafeERC20.bin -o=../../../../generated/java -p=org.starcoin.bifrost.ethereum.model
#web3j generate solidity -a=ERC20.abi -b=ERC20.bin -o=../../../../generated/java -p=org.starcoin.bifrost.ethereum.model
#web3j generate solidity -a=ISTC.abi -b=ISTC.bin -o=../../../../generated/java -p=org.starcoin.bifrost.ethereum.model
web3j generate solidity -a=STC.abi -b=STC.bin -o=../../../../generated/java -p=org.starcoin.bifrost.ethereum.model
```

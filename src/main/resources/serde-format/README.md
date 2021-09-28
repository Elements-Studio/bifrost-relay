
Replace `{BIFROST_RELAY_REPO_DIR}` and `{STARCOIN_REPO_DIR}`, then run:

```shell
mvn exec:java -Dexec.mainClass="org.starcoin.serde.format.cli.SerdeGenJava" -X -Dexec.args="-w {BIFROST_RELAY_REPO_DIR} --onlyRetainDependenciesOfLast 1 --targetSourceDirectoryPath ./src/generated/java {STARCOI_REPO_DIR}/etc/starcoin_types.yml:org.starcoin.types {STARCOI_REPO_DIR}/etc/onchain_events.yml:org.starcoin.types.event {BIFROST_RELAY_REPO_DIR}/src/main/resources/serde-format/bifrost_types.yaml:org.starcoin.bifrost.types"
```

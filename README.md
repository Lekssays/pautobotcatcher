# PAutoBotCatcher: A Blockchain-based Privacy-preserving Botnet Detector for Internet of Things (under peer-review)

### Authors: Ahmed Lekssays, Luca Landa, Barbara Carminati, and Elena Ferrari

## Description

* This project uses a Fabric blockchain for botnet detection, based on PeerHunter algorithm.
* It contains a pre-built .jar of the detection algorithm, and will automatically download all dependencies with gradle when starting the network.
* The contract under "contract/" allows network flows insertions/query and the execution of the botnet detection algorithm.
* All the intermediate PeerHunter data, and the network flows data, will be compressed on the ledger, for performance sake in the read operations.
* The contract can execute reads from the ledger of up to 4MB, so the detection date is splitted in chunks before saving on the ledger.

## Project Structure

* `application/`

        * `e2e tests` for basic contract functionalities of network flows insertions

        * `invoke.js` script for main botnet detection functionalities, it contains two transaction calls (one of them commented) for retrieving current detection state or performing botnet detection

        * `upload_peerrush_dataset.js` for uploading peerrush dataset (from dataset/ subfolder)

* `bin/`: Hyperledger Fabric binaries.


* `chaincodes/detectioncc`: java contract for network flows update, botnet detection, and all related code

* `network/`

        * `crypto-config` and `configtx` standard metadata for defining the blockchain structure, and related docker-compose.yml

        * `start.sh`: creates a full instance of the network, installing the contract and testing that i works

        * `initialize.sh`: useful for crypto-config and/or configtx meta data update, it should not be used unless it is necessary for updating the network structure (e.g. for adding some peers)

## Getting Started

### Prerequisits
- Make sure `cryptogen` and `configtxgen` are added to your PATH.

- Hyperledger Fabric 2.2 requirements https://hyperledger-fabric.readthedocs.io/en/release-2.2/getting_started.html

- NodeJs > 10 and Java 8

- Install JavaScript dependencies with `npm install` or `yarn install` into `application/` folder.

- Unix based OS for scripts  (Tested on Ubuntu 18.04 64bits LTS)

### Run the system
- Run the command: `./system.sh up`
- To display other options: `./system.sh`

- Run: `node application/upload_peerrush_dataset.sh`

- Run: `node application/invoke.js`

- To easily kill the network, you can use `bin/util/clean_all_containers.sh`, but note that it will also remove all other docker containers


## Monitoring
Prometheus is enabled in the project as a monitoring framework. In addition, Grafana is added for better visualization. You can access Prometheus at: `http://0.0.0.0:9090` and Grafana at `http://0.0.0.0:3000`.

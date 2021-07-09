'use strict';

const fs = require('fs');
const yaml = require('js-yaml');
const { FileSystemWallet, Gateway } = require('fabric-network');
// const NetworkFlow = require('./lib/NetworkFlow.js');

const wallet = new FileSystemWallet('../identity/user/user1/wallet');

async function main() {

    const gateway = new Gateway();

    try {

        const userName = 'User1@org1.example.com';

        // Load connection profile; will be used to locate a gateway
        let connectionProfile = yaml.safeLoad(fs.readFileSync('../gateway/networkConnection.yaml', 'utf8'));

        // Set connection options; identity and wallet
        let connectionOptions = {
            identity: userName,
            wallet: wallet,
            discovery: { enabled:false, asLocalhost: true }
        };

        await gateway.connect(connectionProfile, connectionOptions);

        console.log('connected to mychannel');

        const network = await gateway.getNetwork('mychannel');
        const contract = await network.getContract('BotnetDetectionContract');

        // NOTE: with these timestamps it is performed on all network flows saved in the ledger
        // const issueResponse = await contract.submitTransaction('performBotnetDetection', '000', '99999999999999');
        const issueResponse = await contract.evaluateTransaction('getDetectionState');
        
        console.log('Issue transaction response:');
        const responseObj = JSON.parse(issueResponse.toString());
        console.log("bot ips:", responseObj.botIps);
    } catch (error) {

        console.log(`Error processing transaction. ${error}`);
        console.log(error.stack);

    } finally {
        gateway.disconnect();

    }
}

main().then(() => {
    console.log('DONE');

}).catch((e) => {
    console.log('script exception.');
    console.log(e);
    console.log(e.stack);
    process.exit(-1);

});
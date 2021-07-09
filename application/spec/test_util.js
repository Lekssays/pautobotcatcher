const fs = require('fs');
const yaml = require('js-yaml');
const { FileSystemWallet, Gateway } = require('fabric-network');

const wallet = new FileSystemWallet('../identity/user/user1/wallet');


const connectAndGetContract = async (contractName = 'BotnetDetectionContract', channelName = 'mychannel') => {
    const gateway = new Gateway();

    const userName = 'User1@org1.example.com';

    // Load connection profile; will be used to locate a gateway
    let connectionProfile = yaml.safeLoad(fs.readFileSync('../gateway/networkConnection.yaml', 'utf8'));

    // Set connection options; identity and wallet
    let connectionOptions = {
        identity: userName,
        wallet: wallet,
        discovery: { enabled: false, asLocalhost: true }
    };

    await gateway.connect(connectionProfile, connectionOptions);

    const network = await gateway.getNetwork(channelName);
    const contract = await network.getContract(contractName);

    return new Promise(resolve => resolve({ gateway, contract }));
};

module.exports = connectAndGetContract;

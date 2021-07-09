'use strict';

// Bring key classes into scope, most importantly Fabric SDK network class
const fs = require('fs');
const { FileSystemWallet, X509WalletMixin } = require('fabric-network');
const path = require('path');

const fixtures = path.resolve(__dirname, '../network');

// A wallet stores a collection of identities
const wallet = new FileSystemWallet('../identity/user/user1/wallet');

async function main() {

    // Main try/catch block
    try {

        // Identity to credentials to be stored in the wallet
        const credPath = path.join(fixtures, '/crypto-config/peerOrganizations/org1.example.com/users/User1@org1.example.com');
        const cert = fs.readFileSync(path.join(credPath, '/msp/signcerts/User1@org1.example.com-cert.pem')).toString();

        const privateKeyFileName = getPrivateKeyFileName(path.join(credPath, '/msp/keystore/'));
        const key = fs.readFileSync(path.join(credPath, `/msp/keystore/${privateKeyFileName}`)).toString();

        // Load credentials into wallet
        const identityLabel = 'User1@org1.example.com';
        const identity = X509WalletMixin.createIdentity('Org1MSP', cert, key);

        await wallet.import(identityLabel, identity);

    } catch (error) {
        console.log(`Error adding to wallet. ${error}`);
        console.log(error.stack);
    }
}

const getPrivateKeyFileName = path => {
    const files = fs.readdirSync(path);
    if (files.length > 1) {
        throw 'multiple keys found';
    } else if (files.length == 0) {
        throw 'no key found';
    }

    return files[0];
};

main().then(() => {
    console.log('done');
}).catch((e) => {
    console.log(e);
    console.log(e.stack);
    process.exit(-1);
});
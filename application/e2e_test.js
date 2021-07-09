'use strict';

const connectAndGetContract = require('./spec/test_util');
const upload = require('./util/upload_dataset');

const main = async () => {
    const { totalTimestamps } = await upload('./dataset/flows.csv', 50000, 4, 150000, 0);
    const { contract, gateway } = await connectAndGetContract('BotnetDetectionContract');

    const firstTimestamp = totalTimestamps[0];
    const secondTimestamp = totalTimestamps[parseInt(totalTimestamps.length / 2)];
    const lastTimestamp = totalTimestamps[totalTimestamps.length - 1];

    await assertBotsAre(contract, []);

    await contract.submitTransaction('performBotnetDetection', firstTimestamp.toString(), secondTimestamp.toString());
    await sleep(1000);
    
    await assertBotsAre(contract, []);

    await contract.submitTransaction('performBotnetDetection', secondTimestamp.toString(), lastTimestamp.toString());
    await sleep(1000);
    
    await assertBotsAre(contract, ['192.168.58.150', '192.168.58.136', '192.168.58.137']);

    await gateway.disconnect();
};

const assertBotsAre = async (contract, expectedBots) => {
    const response = await contract.evaluateTransaction('getDetectionState');
    const actualBots = JSON.parse(response.toString()).botIps;

    assertEquals(JSON.stringify(expectedBots), JSON.stringify(actualBots));
}

const assertEquals = (expected, actual) => {
    const _expected = JSON.stringify(expected);
    const _actual = JSON.stringify(actual);
    if (_expected !== _actual) throw new Error('expected:' + _expected + "\nbut found:" + _actual);
}

const sleep = (ms) => {
    return new Promise(resolve => {
        setTimeout(resolve, ms)
    })
}

main()
    .then(() => console.log('botnet detection is working properly'))
    .catch((e) => {
        console.log('script exception.');
        console.log(e);
        console.log(e.stack);
        process.exit(-1);
    });
const connectAndGetContract = require('./test_util');

let gateway = null
let contract = null;
let originalJasmineInterval = null;

beforeAll(async () => {
    let contractData = await connectAndGetContract('BotnetDetectionContract');
    gateway = contractData.gateway;
    contract = contractData.contract;
});

afterAll(async () => await gateway.disconnect());

beforeAll(() => {
    originalJasmineInterval = jasmine.DEFAULT_TIMEOUT_INTERVAL;
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 120000;
});

afterAll(() => {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalJasmineInterval;
});

describe('gateway connection', () => {
    it('execution time', async () => {
        await executeAndPrintTime(connectAndGetContract, 'Gateway connection and NetworkFlowContract retrieval');
    });
});

describe('insertion', () => {
    it('single insertion with many records, execution time', async () => {
        let recordsNumber = 10000;
        let csvData = generateCsvData(recordsNumber);

        const fn = async () => await contract.submitTransaction('addMultipleNetworkFlows', '00123', csvData);
        await executeAndPrintTime(fn, `"addMultipleNetworkFlows" transaction, 1 insertion with with ${recordsNumber} network flows`);
    });

    it('many insertions with few network flows each', async () => {
        const networkFlowsNumberPerRecord = 10;
        const transactionsNumber = 500;


        const fn = async () => {
            const promises = [];

            for (let i = 0; i < transactionsNumber; i++) {
                let csvData = generateCsvData(networkFlowsNumberPerRecord);
                promises.push(contract.submitTransaction('addMultipleNetworkFlows', i.toString().padStart(20, '0'), csvData));
            }

            await Promise.all(promises);
        }

        await executeAndPrintTime(fn, `"addMultipleNetworkFlows" transaction, ${transactionsNumber} insertions with ${networkFlowsNumberPerRecord} network flows each`);
    });
});

describe('query', () => {
    it('query of single record with many network flows, execution time', async () => {
        const networkFlowsNumber = 10000;
        let csvData = generateCsvData(networkFlowsNumber);
        await contract.submitTransaction('addMultipleNetworkFlows', '12345', csvData);

        const expectedKey = '12345'.padStart(20, '0') + "-001";
        let response = null;
        const fn = async () => {
            response = await contract.evaluateTransaction('getNetworkFlowsForKey', expectedKey);
        }

        await executeAndPrintTime(fn, `"getNetworkFlowsForKey" of a single record with ${networkFlowsNumber} network flows`);

        const responseObj = JSON.parse(response.toString());
        expect(responseObj.length).toBe(networkFlowsNumber);
    });

    it('query with many records with few network flows', async () => {
        const networkFlowsNumberPerRecord = 10;
        const transactionsNumber = 500;

        const promises = [];

        for (let i = 0; i < transactionsNumber; i++) {
            let csvData = generateCsvData(networkFlowsNumberPerRecord);
            promises.push(contract.submitTransaction('addMultipleNetworkFlows', i.toString().padStart(20, '0'), csvData));
        }
        await Promise.all(promises);

        let response = null;
        const fn = async () => {
            response = await contract.evaluateTransaction('getNetworkFlowsByRange', '0', transactionsNumber.toString());
        };

        await executeAndPrintTime(fn, `"getNetworkFlowsByRange" of ${transactionsNumber} records with ${networkFlowsNumberPerRecord} network flows each`);

        const responseObj = JSON.parse(response.toString());

        expect(responseObj.length).toBeGreaterThanOrEqual(transactionsNumber);
    });

    it('query with many records with many network flows', async () => {
        const networkFlowsNumberPerRecord = 500;
        const transactionsNumber = 50;

        const promises = [];

        for (let i = 0; i < transactionsNumber; i++) {
            let csvData = generateCsvData(networkFlowsNumberPerRecord);
            promises.push(contract.submitTransaction('addMultipleNetworkFlows', i.toString().padStart(20, '0'), csvData));
        }
        await Promise.all(promises);

        let response = null;
        const fn = async () => {
            response = await contract.evaluateTransaction('getNetworkFlowsByRange', '0', transactionsNumber.toString());
        };

        await executeAndPrintTime(fn, `"getNetworkFlowsByRange" of ${transactionsNumber} records with ${networkFlowsNumberPerRecord} network flows each`);

        const responseObj = JSON.parse(response.toString());

        expect(responseObj.length).toBeGreaterThanOrEqual(transactionsNumber);
    });

});

const executeAndPrintTime = async (fn, messageToPrint) => {
    let startTime = Date.now();

    await fn();

    let endTime = Date.now();

    console.log('-----', messageToPrint, 'execution time:', (endTime - startTime) / 1000, 'seconds')
}

const generateCsvData = (recordsNumber) => {
    let lines = [];
    const protocols = ['tcp', 'udp', 'man'];

    for (let i = 0; i < recordsNumber; i++) {
        const ipSrc = randomInt(200) + '.' + randomInt(200) + '.' + randomInt(200) + '.' + randomInt(200);
        const ipDst = randomInt(200) + '.' + randomInt(200) + '.' + randomInt(200) + '.' + randomInt(200);

        const protocol = protocols[randomInt(protocols.length)];

        const bppIn = randomInt(1000);
        const bppOut = randomInt(1000);

        lines.push([ipSrc, ipDst, protocol, bppIn, bppOut].join(','));
    }

    return lines.join("\n");
}

const randomInt = (limit) => Math.floor(Math.random() * limit);
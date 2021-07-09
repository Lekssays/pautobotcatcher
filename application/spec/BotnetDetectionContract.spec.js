// NOTE: make sure that network is up and running

const connectAndGetContract = require('./test_util');

let gateway = null;
let contract = null;

const validKeyParams = {
    timestamp: '987',
    orgId: '01'
};

const validValueParams = {
    ipSource: '123',
    ipDestination: '01',
    bytesPerPacketIn: 'bppIn',
    bytesPerPacketOut: 'bppOut',
    protocol: 'tcp'
};

const notSoValidCsvData = '123,456,0,0,tcp\n789,123,udp,1,2';


beforeAll(async () => {
    const data = await connectAndGetContract('BotnetDetectionContract');

    contract = data.contract;
    gateway = data.gateway;
});

afterAll(async () => await gateway.disconnect());

describe('addSingleNetworkFlow', () => {
    it('adds new network flow correctly', async () => {
        let response = await contract.submitTransaction('addSingleNetworkFlow', '1230002', 'ipSrc2', 'ipDst', 'tcp', '0', '0');

        let responseObj = JSON.parse(response.toString());

        expect(responseObj[0].ipSource).toEqual('ipSrc2');

        const networkFlowKey = '1230002'.padStart(20, '0') + '-' + '001';
        response = await contract.evaluateTransaction('getNetworkFlowsForKey', networkFlowKey);
        responseObj = JSON.parse(response.toString());

        expect(responseObj[0].ipSource).toEqual('ipSrc2');
    });

    it('throws error if timestamp is not an integer', async () => {
        const error = await getTransactionErrorWithArgs({ timestamp: '0a123' });
        expect(error.toString()).toContain(`timestamp "0a123" is not a positive integer`);
    });

    it('throws error if timestamp is too long', async () => {
        const timestamp = '0'.repeat(25);

        const error = await getTransactionErrorWithArgs({ timestamp });
        expect(error.toString()).toContain(`timestamp "${timestamp}" exceeds length limit of 20`);
    });
});

describe('.getNetworkFlowsForKey', () => {
    it('returns correct set of network flows', async () => {
        await contract.submitTransaction('addSingleNetworkFlow', '789', 'ipSrc2', 'ipDst', 'tcp', '0', '1');
        const expectedKey = '789'.padStart(20, '0') + '-' + '001';

        const actualResponse = await contract.evaluateTransaction('getNetworkFlowsForKey', expectedKey);
        const expectedResponse = '[{"ipSource":"ipSrc2","ipDestination":"ipDst","bytesPerPacketIn":0,"bytesPerPacketOut":1,"protocol":"tcp"}]';
        expect(JSON.parse(actualResponse.toString())).toEqual(JSON.parse(expectedResponse));
    });
});

describe('.getNetworkFlowsByRange', () => {
    it('returns correct set of network flows', async () => {
        const firstTimestamp = new Date('10/10/2020').getTime().toString();
        const secondTimestamp = new Date('11/10/2020').getTime().toString();
        await contract.submitTransaction('addSingleNetworkFlow', firstTimestamp, 'ipSrc2', 'ipDst', 'tcp', '0', '1');
        await contract.submitTransaction('addSingleNetworkFlow', secondTimestamp, 'ipSrc2', 'ipDst', 'tcp', '1', '2');

        const response = await contract.evaluateTransaction('getNetworkFlowsByRange', firstTimestamp, secondTimestamp);
        const result = JSON.parse(response);

        expect(result.length).toBe(2);

        const firstExpected = { "ipSource": "ipSrc2", "ipDestination": "ipDst", "bytesPerPacketIn": 0, "bytesPerPacketOut": 1, "protocol": "tcp" };

        const secondExpected = { "ipSource": "ipSrc2", "ipDestination": "ipDst", "bytesPerPacketIn": 1, "bytesPerPacketOut": 2, "protocol": "tcp" };

        expect(result).toContain(firstExpected);
        expect(result).toContain(secondExpected);
    });
});

describe('.addMultipleNetworkFlows', () => {
    it('adds multiple network flows at once', async () => {
        const validCsvData = '123,456,tcp,0,0\n789,987,udp,1,1';

        await contract.submitTransaction('addMultipleNetworkFlows', '00123', validCsvData);

        const expectedTimestamp = '00123'.padStart(20, '0');
        const expectedOrgId = '001';
        const expectedKey = expectedTimestamp + '-' + expectedOrgId;
        const response = await contract.evaluateTransaction('getNetworkFlowsForKey', expectedKey);

        const result = JSON.parse(response);

        expect(result.length).toBe(2);

        const expected = [
            { "ipSource": "123", "ipDestination": "456", "bytesPerPacketIn": 0, "bytesPerPacketOut": 0, "protocol": "tcp" },
            { "ipSource": "789", "ipDestination": "987", "bytesPerPacketIn": 1, "bytesPerPacketOut": 1, "protocol": "udp" }
        ];

        expect(result).toEqual(expected);
    });

    it('automatically rejects invalid rows', async () => {
        await contract.submitTransaction('addMultipleNetworkFlows', '99887', notSoValidCsvData);

        const expectedTimestamp = '99887'.padStart(20, '0');
        const expectedOrgId = '001';
        const expectedKey = expectedTimestamp + '-' + expectedOrgId;
        const response = await contract.evaluateTransaction('getNetworkFlowsForKey', expectedKey);

        const result = JSON.parse(response);

        expect(result.length).toBe(1);
    });

    it('throws error if timestamp is too long', async () => {
        let error = null;
        const invalidTimestamp = '2'.repeat(30);

        try {
            await contract.submitTransaction('addMultipleNetworkFlows', invalidTimestamp, notSoValidCsvData);
        } catch (e) {
            error = e;
        }

        expect(error).not.toBeNull();
    });
});

const getTransactionErrorWithArgs = async (keyParams = {}, valueParams = {}) => {
    const _keyParams = { ...validKeyParams, ...keyParams };
    const _valueParams = { ...validValueParams, ...valueParams };

    try {
        await contract.submitTransaction('addSingleNetworkFlow',
            _keyParams.timestamp,
            _valueParams.ipSource,
            _valueParams.ipDestination,
            _valueParams.protocol,
            _valueParams.bytesPerPacketIn,
            _valueParams.bytesPerPacketOut);
    } catch (error) {
        return error;
    }

    throw new Error('no exception was raised');
}

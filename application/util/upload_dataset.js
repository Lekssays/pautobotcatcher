const fs = require('fs');
const connectAndGetContract = require('../spec/test_util');

const NETWORK_FLOWS_PER_GROUP_NUMBER = 50000;
const MAX_LIMIT = 150000;
const PARALLEL_TRANSACTIONS_NUMBER = 4;
const START = 0;

const upload = async (
    filePath,
    networkFlowsPerGroupNumber = NETWORK_FLOWS_PER_GROUP_NUMBER,
    parallelTransactionsNumber = PARALLEL_TRANSACTIONS_NUMBER,
    maxLimitNumber = MAX_LIMIT,
    start = START
) => {
    const lines = fs.readFileSync(filePath).toString().split("\n");

    const { gateway, contract } = await connectAndGetContract();

    let totalRequests = 0;
    const totalTimestamps = [];

    for (let i = start; i < maxLimitNumber; i += networkFlowsPerGroupNumber) {
        const records = lines
            .slice(i, i + networkFlowsPerGroupNumber)
            .filter(validRecord);

        const promises = [];
        const networkFlowsSplits = splitUp(records, parallelTransactionsNumber);

        for (let i = 0; i < parallelTransactionsNumber; i++) {
            const partialCsvData = networkFlowsSplits[i].join("\n");
            const timestamp = Date.now().toString();
            totalRequests += 1;
            totalTimestamps.push(timestamp);
            promises.push(contract.submitTransaction('addMultipleNetworkFlows', timestamp, partialCsvData));
        }

        await Promise.all(promises);

        console.log((i + networkFlowsPerGroupNumber).toString() + '/' + maxLimitNumber.toString());
    }

    await gateway.disconnect();

    return { totalRequests, totalTimestamps };
};

const ipRegex = '^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$';
const validRecord = record => {
    const tokens = record.split(',');
    return (tokens[0].match(ipRegex) != null) && (tokens[1].match(ipRegex) != null);
}

function splitUp(arr, n) {
    var rest = arr.length % n, // how much to divide
        restUsed = rest, // to keep track of the division over the elements
        partLength = Math.floor(arr.length / n),
        result = [];

    for (var i = 0; i < arr.length; i += partLength) {
        var end = partLength + i,
            add = false;

        if (rest !== 0 && restUsed) { // should add one element for the division
            end++;
            restUsed--; // we've used one division element now
            add = true;
        }

        result.push(arr.slice(i, end)); // part of the array

        if (add) {
            i++; // also increment i in the case we added an extra element for division
        }
    }

    return result;
}

module.exports = upload;

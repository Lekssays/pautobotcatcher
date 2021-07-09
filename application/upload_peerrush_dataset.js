const upload = require('./util/upload_dataset');

const distinct = (value, index, self) => self.indexOf(value) === index;

upload('./dataset/flows.csv')
    .then(({ totalRequests, totalTimestamps }) => {
        console.log('totalRequests:', totalRequests);
        console.log('totalTimestamps (distinct):', totalTimestamps.filter(distinct).length);

        console.log("timestamps:");
        console.log(totalTimestamps.join("\n"));

        console.log('done')
    })
    .catch((error) => console.log('errors:', error.stack));

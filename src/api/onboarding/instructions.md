# On-Boarding guide

#### Steps for on-boarding your local environment

##### Node.js
Follow instructions from [link](https://phoenixnap.com/kb/install-node-js-npm-on-windows).
Verify installation with commands:
```sh
node -v
npm -v
```

##### Serverless
Install serverless with:
```sh
npm install -g serverless
```

Install serverless with:
```sh
npm install -g serverless
```
Creating AWS Access Keys, [instructions](https://www.youtube.com/watch?v=KngM5bfpttA).
```sh
serverless config credentials --provider aws --key <INSERT_KEY> --secret <INSERT_SECRET_KEY> -o
```

Verify installation with:
```sh
serverless -v
```
For more help checkout the steps from [link](https://www.serverless.com/framework/docs/getting-started) for getting stared with Serverless.

##### Dependencies

Install uuid library
```sh
npm install --save uuid
```

Install aws-sdk library
```sh
npm install --save aws-sdk
```

##### Deploying code
For deploying entire project:
```sh
serverless deploy --stage stage --region eu-central-1
```

For deploying single function:
```sh
serverless deploy function -f functionName

```

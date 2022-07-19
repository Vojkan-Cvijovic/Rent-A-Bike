# Bike renting application

> Andoid application that uses backend service deployed on AWS using serverless framework.
Backed service is composed for Lambda functions that use DynamoDb as database, CloudWatch for logging and are reachable over API gateway. For authentication and authorization AWS Cognito is used.

### Project overview

![AWS Overview](resources/AWS-Architecture-Overview.PNG?raw=true "Overview of AWS Architecture")

##### Backend service

Service uses NodeJS code which is deployed on AWS Lambda service using [Serverless](https://www.serverless.com/framework/docs) framework.
```
serverless deploy --stage stage --region eu-central-1
```
will generate stage CloudFormation template in eu-central region and provision all neccessarry resources. Full set of instructions is avaliable in project documentation.

##### Android application
- Requires atleast Android 11 to run.
- Update BikeREntApp with appropriate data from AWS
```Kotlin
auth = Auth(
            context = this,
            userPoolId = "<INSERT_POOL_ID>",
            identityPoolId = "<INSERT_IDENTITY_POOL_ID>",
            clientId = "<INSERT_CLIENT_ID>",
            clientSecret = "<INSERT_CLIENT_SECRET>"
        )
```


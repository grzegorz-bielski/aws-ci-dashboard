# aws-ci-dashboard
This is a full-stack Scala app which could be used as an alternative UI for AWS CodePipeline service.

It's not terribly feature rich, and has few bugs. It was mostly used as a playground for trying Scala 3 with ScalaJS, Laminar and ZIO.

## Setup
1. Install Scala & friends through [coursier](https://get-coursier.io/docs/cli-installation) with
   ```
   cs setup
   ```
2. Install Node & JS dependencies through [nvm](https://github.com/nvm-sh/nvm) and
   ```
   npm install
   ```
3. Create .env file in the root of the project with
   ```
   AWS_ACCESS_KEY_ID=<aws access key id for the Dashboard IAM user>
   AWS_SECRET_ACCESS_KEY=<aws secret access key for the Dashboard IAM user>
   APP_PORT=8090
   AWS_REGION=eu-central-1
   AWS_ACCOUNT=<your aws account number with given IAM user>
   ```

## Development

Open three terminal windows that will:
 - Compile Scala.js frontend files in watch mode 
    
    ```
    ./sbt.sh "~frontend/fastLinkJS"
    ``` 
 - Compile backend files in watch mode & restarts the server
    ```
    ./sbt.sh "~backend/reStart"
    ```
 - Run the snowpack bundler in watch mode for any other frontend resources
    ```
    npm run snowpack -- dev
    ```

Serving a prod built with sbt
   ```
   ./sbt.sh "~frontend/fullLinkJS"
   npm run snowpack -- build
   ./sbt.sh "backend/run"
   ```
## Prod
Build and push Docker image to the local repository:
```
./sbt.sh build
./sbt.sh backend/publishLocal
```

Build and deploy to AWS Fargate
```
./sbt.sh build
./deploy.sh
```
# aws-ci-dashboard

## Development

Open three terminal windows with:
 - Compile Scala.js frontend files in watch mode 
    
    ```
    sbt "~frontend/fastLinkJS"
    ``` 
 - Compiles backend files in watch mode & restarts the server
    ```
    sbt "~backend/reStart"
    ```
 - Runs the snowpack bundler in watch mode for any other frontend resources
    ```
    npm run snowpack -- dev
    ```


## Prod
```
 sbt "~frontend/fullLinkJS"
 npm run snowpack -- build
 sbt "backend/run"
```
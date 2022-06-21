const { ApolloGateway, RemoteGraphQLDataSource  } = require("@apollo/gateway");
const { ApolloServer } = require("apollo-server-express");
const express = require("express");
const expressjwt  = require("express-jwt");
require('dotenv').config()

const port = 8090;
const app = express();

app.use(
    expressjwt.expressjwt({
        secret: "f1BtnWgD3VKY",
        algorithms: ["HS256"],
        credentialsRequired: false,
        ignoreExpiration: true
    })
);

const gateway = new ApolloGateway({
    buildService({ name, url }) {
        return new RemoteGraphQLDataSource({
            url,
            willSendRequest({ request, context }) {
                request.http.headers.set(
                    "user",
                    context.user ? JSON.stringify(context.user.sub) : null
                );
            }
        });
    }
});

const server = new ApolloServer({
    gateway,
    subscriptions: false,
    context: ({ req }) => {
        const user = req.auth || null;
        return { user };
    }
});

server.start().then(res => {
    server.applyMiddleware({ app });
    app.listen({ port }, () =>
        console.log(`Gateway API running at port: ${port}`)
    );
});


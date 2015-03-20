# Database-container

[Docker](https://www.docker.io/) container, running a [PostgreSQL](http://www.postgresql.org/) database server for Grafterizer.

It exposes the default PostgreSQL port number (5432). The user is ```postgres``` and the password can be set when running the container.

## Building

```docker build -t pg_grafterizer .```

## Running

```docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=secretpassword pg_grafterizer```
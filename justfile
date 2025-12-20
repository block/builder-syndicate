# Builder Syndicate development commands

# Start local development environment (MySQL)
dev:
    docker-compose up -d
    @echo "Waiting for MySQL to be ready..."
    @until docker-compose exec -T mysql mysqladmin ping -uroot -proot --silent; do sleep 1; done
    @echo "MySQL is ready on port 3307"

# Stop local development environment
down:
    docker-compose down

# Run Flyway migrations
db-migrate:
    ./bin/gradle flywayMigrate

# Generate jOOQ code from database (requires `just dev` first)
codegen:
    ./bin/gradle jooqCodegen

# Build the frontend only
buildWeb:
    cd web && npm install && npm run build

# Build the project (includes frontend)
build:
    ./bin/gradle build -x test

# Run tests
test:
    ./bin/gradle test

# Run the application
run:
    ./bin/gradle run

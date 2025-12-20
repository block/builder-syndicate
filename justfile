# Builder Syndicate development commands
set shell := ["bash", "-c"]

# Start local development environment (MySQL)
dev:
    docker-compose up -d
    @echo "Waiting for MySQL to be ready..."
    @until docker-compose exec -T mysql mysqladmin ping -uroot -proot --silent; do sleep 1; done
    @echo "MySQL is ready on port 3307"

# Stop local development environment
down:
    docker-compose down

# Wipe database and restart fresh
db-reset:
    docker-compose down -v
    just dev
    @sleep 2
    just db-migrate

# Run Flyway migrations
db-migrate:
    source bin/activate-hermit && gradle flywayMigrate

# Generate jOOQ code from database (requires `just dev` first)
codegen:
    source bin/activate-hermit && gradle jooqCodegen

# Build the frontend only
buildWeb:
    source bin/activate-hermit && cd web && npm install && npm run build

# Build the project (includes frontend)
build:
    source bin/activate-hermit && gradle build

# Run tests
test:
    source bin/activate-hermit && gradle test

# Run the application
run:
    source bin/activate-hermit && gradle run

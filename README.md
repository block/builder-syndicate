# Builder Syndicate

**Builder Syndicate** is a knowledge-sharing platform designed for builders of all kinds. It helps 
contributors surface what matters, spark meaningful discussion, and learn from each other — with 
intelligent tooling that gets out of your way. Built for professionals who value signal over noise,
knowing one builder's noise is another's symphony.

## Getting Started

### Code Formatting

This project uses [kotlin-formatter](https://github.com/block/kotlin-formatter) to maintain consistent code style. Formatting is enforced in CI and via pre-commit hooks.

**Setup (one-time):**

```bash
lefthook install
```

This installs a pre-commit hook that automatically formats your Kotlin code before each commit.

**Manual formatting:**

```bash
# Format all Kotlin files
gradle applyFormatting

# Check formatting (same as CI)
gradle checkFormatting
```

**IDE Plugin (recommended):**

Install the [kotlin-formatter IntelliJ plugin](https://plugins.jetbrains.com/plugin/26482-kotlin-formatter) to format on save.

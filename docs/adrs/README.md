# Architecture Decision Record (ADR)

ADR is a way of capturing important architectural decisions, along with their context and consequences.

ADRs are proposed in the form of a pull request, and they commonly follow this format:

* **Title**: short present tense imperative phrase, less than 50 characters, like a git commit message.

* **Status**: proposed, accepted, rejected, deprecated, superseded, etc.

* **Context**: what is the issue that we're seeing that is motivating this decision or change.

* **Decision**: what is the change that we're actually proposing or doing.

* **Consequences**: what becomes easier or more difficult to do because of this change.

---

- More information about ADRs can be found [here](https://github.com/joelparkerhenderson/architecture_decision_record).
- Examples from GitHub's actions/runner can be found [here](https://github.com/actions/runner/tree/main/docs/adrs). 

---
### ADRs:
1. [Absolute URLs for attachments](001-absolute-urls-for-attachments.md)
1. [PostgreSQL as event (FHIR message) storage](002-postgres-as-eventstore.md)
1. [Terminology Server](003-terminology-server.md)
1. [Versioning](004-versioning.md)

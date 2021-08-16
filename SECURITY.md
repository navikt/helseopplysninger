# Security

## Dependency Vulnerability Scanning
Daily tasks:
- Vulnerability scanning by Snyk for validating dependencies 
- Dependabot for automatic dependency updates

## Security Disclosure Policy
Rais a [GitHub Issue](https://github.com/navikt/helseopplysninger/issues) and tag it as **Security**

## Security Update Policy
Best effort solve vulnerabilities found and only suppress false positives.

Try to only use active libraries that also updates its dependencies for faster vulnerability fixes. 
In other words; Avoid "dead" dependencies.

Try to merge dependabot pull-request daily if updates are found.

Exclude transitive dependencies we dont use that includes vulnerabilities.

## Security Configuration
We will follow [NAV Security Blueprints](https://security.labs.nais.io/) for different communication strategies.

## References
- https://securitylab.github.com/research/github-actions-preventing-pwn-requests/
- https://security.labs.nais.io/
- Security Playbook (kommer)

Slack: #sikkerhet #pig_sikkerhet #tokenx

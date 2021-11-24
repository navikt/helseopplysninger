# End-to-end (e2e) tests
This is our `end-to-end` or `e2e` tests that will test our services in `dev` the environment.

## Strategy
See our [Test Strategy](../../docs/test/test-strategy.md) for further information.

The following scopes will be tested

## End-to-end (e2e)
This microservice will test our value chain. 
We only have to write a few of these tests because they will only
verify our assumptions that are already tested extensively and thoroughly with 
`component tests` as Martin Fowler call them, or `integration` tests as Spotify call them.


## Integration test
As a result for testing the value chain, every integration in the chains will be tested.

## Smoke test
As a result for integration testing, e2e testing and that the tests require the microservices to
be running in the `development` environemnt, it will indirectly smoke test our configurations.

We will look at production environment as a mirror to the development environment,
everything that fails in dev, will fail in prod. 
A few times the environment will differ, e.g certificates, secrest etc. This will be cought 
by monitoring and observability and is not necessary to test.

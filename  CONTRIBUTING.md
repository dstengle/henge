# Contributing to Property Server
The following details the expectations and guidelines for incoming contributions to the Property Server repository. To better serve those who intend on contributing, we feel it is important that the acceptance criteria for acceptable code and issue submissions be defined for reference. In addition to the rules and guidelines below, we expect all contributors will adhere to the [contributor covenant]( http://contributor-covenant.org/version/1/2/0/). 
 
## Pull Requests
Submitting a PR should require that all fields in the template are filled out.
 
1. Your PR references an open issue (if one doesn't exist, it should be created). Is is important to keep talk about the issue separate from discussion about the code related to the issue. 
3. All PRs will be expected to pass a build process via CI
4. Any new dependencies added to a project should have a link to the project repo / documentation
5. Dependencies should always pin to a specific version (no wildcards or modifiers like *, ~, or ^) and organized alpha sort ASC
6. There are no merge conflicts
 
## Commit Messages
Commit message are important when looking at the commit history of a project. Please consider the following as you commit your work:
 
1. Use the present tense ("Add feature" not "Added feature")
2. Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
3. Limit the first line to 72 characters or less
4. Reference issues and pull requests liberally
 
 
## Continuous Integration
Projects are expected to enforce a number of best practices such as linting, static analysis, styleguide rules, and testing. Each PR will be run against a continuous integration server with the result getting marked in the PR as pass / fail. Failing builds will result in the pull request getting declined.
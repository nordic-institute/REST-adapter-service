## How to contribute to REST Adapter Service?

We are glad you want to contribute to development of REST Adapter Service!

Please read the following instructions for ease the work needed for the collaboration process.

## Submitting work

Please send a GitHub Pull Request to REST Adapter Service with a clear list of what you've done. When you send a pull request, it is strongly recommended to make sure all of your commits are atomic (one feature per commit).

Please write a clear log message for your commits. One-line messages are fine for small changes, but bigger changes should look like this:

```
$ git commit -m "A short summary of the commit
>
> A paragraph describing what changed and its impact."
```


The pull requests are reviewed by the REST Adapter core development team and possible other selected representatives related to development of REST Adapter Service. Additional reviewers can be added as necessary. The pull request is reviewed according to the acceptance criteria (see below sections).

##Accepting work and acceptance criteria

### 1. Feature analysis

Are the features OK to be accepted to the core of REST Adapter Service?

### 2. Source code

Is the source code for the software and its dependencies available?

### 3. CI build & tests

No merge conflicts?
Does the build and the test cases work?
Does the packaging work (Ubuntu & RHEL)?
Can the software be installed on a clean system (Ubuntu & RHEL)?
Can the software version be upgraded from the previous REST Adapter Service version?

### 4. [SonarQube](https://www.sonarqube.org/) static analysis

SonarQube shows no bugs or code smells of severity blocker or critical? The developer has a chance to comment the issues before accept/reject action.

### 5. [Checkstyle](http://checkstyle.sourceforge.net/) for coding rules

Checkstyle is used for checking the coding rules and it help programmers to write Java code that adheres to a coding standard.

Below coding style rules must be followed:

* use spaces, no tabs
* 4-space indents
    *  but a 8-space continuation after a wrapped line (to make the next statement stand out, e.g. the body in long if conditions).
* 120 characters line width (Java code tends to be wide, and so are screens nowadays)

### 6. [Jacoco](http://www.jacoco.org/jacoco/) static analysis Java code coverage

Is there enough test coverage? The test coverage should be equal or higher than in the previous version. Creator of a pull request should have added proper set of unit tests before sending the pull request. 

### 7. Changelogs

Have the changelogs been updated and include the changes made?
The changelog items contain reference to the backlog item where applicable?

### 8. Licensing

Is the code licensing OK?
All the code and possible external dependencies must follow MIT license.

### 9. Documentation

Has the documentation been updated if new features added or the old ones updated? Are the sources of pictures provided?

## Reviewing and acceptance order
Pull requests are generally reviewed and accepted on first-come, first-served (FCFS) basis.

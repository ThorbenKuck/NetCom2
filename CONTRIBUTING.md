# Branches

The main branch is the __master__-branch. This branch is meant only for new releases.

The main work-branch is __dev__. New features as well as bug-fixes are merged into this branch. This branch is branched from master.

The hot-spot-branch is the __NIGHTLY__ branch. Every new branch (except for urgent fixes) comes from/goes into this branch. This branch is branched from dev.

Every change in any of these branches requires a pull-request.

If you want, you may freely perform a __pull-down-update__. This means, merging the master into dev and merging dev into NIGHTLY. Please add to your pullrequest the comment "pull-down-update".

A push-up-update is done through the following pattern:

* __NIGHTLY__ into __dev__    
  Provided, your code works as intended, you may create a first level push-up-updated. This means, you will expose the current updates from __NIGHTLY__ into dev and therefor make them relevant for the next release.    
  Create a pull-request with base __dev__ and from __NIGHTLY__. Any other pull-request (except for Issues with the label "urgent") will be denied.    
  Remove the pull-request-template and insert "push-up-update".

* __dev__ into __master__    
  Such a pull-request is considered a release. Before requesting such a pull-request, run all tests. If any fails, this pull-request should not be created.  
  All Tests have to be run again, before accepting such a pull-request. If any fails, this pull-request will be denied.

### Your Branch

If The Issue, you are working on, contains the label "urgent", branch from __dev__.

Else, branch from NIGHTLY.

Once finished, open the pull-request only to the branch you originally branched from.

# Workflow

To correctly contribute on NetCom2, follow the guidelines below:

1) Design your changes
2) Implement your changes
3) Test your changes
4) Create a pull-request
5) Await the reviewer to finish
6) If your pull-request is denied, repeat at 1.

### Conventions

 * The code-conventions in this repository are the default Java conventions.
 * Intent your code with tabs rather than 4 spaces.
 * Branch from Nightly and name your Branch the following: #{ISSUE-NUMBER}_{ISSUE_DESCRIPTION}
 * For new features, the first Commit should be the interface of said feature.
 * Last Commit should be: "#{ISSUE-NUMBER} {ISSUE_DESCRIPTION} Finished."
 * The reviewer and the assignee of your pull-request should (if possible) be different
 * Force-Push is under all circumstances prohibited!
 
### Bug-fixes

Only change things, that are relevant to your issue!  
If possible, do not make major changes to the design!  
Modify/create the/a test, so that the test shows, that the fixed bug is really fixed!

### New features

__Currently, there is no board for new features. Once one is found, this part should be updated to include the used board.__

Import the feature request as an issue!  
Only change things, that are relevant to your issue!  
Design the new feature beforehand!  
If possible, do not make major changes to the design!  
Create a test, that thoroughly tests your new feature!

### Tests

Tests can be done in 2 ways:

1) Unit-Tests.  
   Create your test in <code>src/test/java</code> and then in the same package as the class you are testing  
   Name your Test in the following pattern: _"{Class-Name}Test"_  
   You may freely define the size of your unit. Make it as small as possible.  
   Mock external dependencies, but do not mock message-objects.  
2) Integration-Tests.  
   Create your test in <code>src/test/java/integration</code> and then in whatever package you feel comfortable with  
   Describe, what has to be done, so that your test can be run and succeeds.  
   
Annotate your Test with the Annotation <code>@Testing</code> and your code with <code>@Tested</code>. Those Annotations are for documentation-purposes only.  
Anything, that you need for a Unit-Test, should be exposed via the TestUtils class.

---

### External Contributions

If you want to provide a new feature or a bug-fix, please follow these steps:

0) Fork this repository
1) Create a branch named exactly as the issue you are working on. If the issue has the label "urgent", branch from dev, else branch from NIGHTLY.    
2) If you want to develop a new feature: Design your code. A simple class-diagram is enough, but if you go the extra mile to also provide a sequenz-diagramm, this wont hurt either.
3) Fix the bug/develop the new feature based on your design.
4) Write a test. Test any case you could come up with.
5) Open a pull-request based on the provided pull-request template onto the branch your branch branched from.
6) Add a direct contributor as an reviewer to your pull-request
7) Add a direct contributor as an assignee to your pull-request
8) If your pull-request get's denied, start over from 3.

**NOTE**: Only change thing relevant to your issue! If your pull-request changes anything, that is not important, your pull-request will be denied!

If you want to contribute a new feature to NetCom2, you should have extensive knowledge about NetCom2 and Client-Server-Communications.

In most cases, pull-request from external contributors about urgent Issues or new features will be denied.

### Direct Contributions

If you are a direct contributor (you have push rights to this repository), please follow the following guide

1) Create a branch named exactly as the issue you are working on. If the issue has the label "urgent", branch from dev, else branch from NIGHTLY.    
2) If you want to develop a new feature: Design your code. A simple class-diagram is enough, but if you go the extra mile to also provide a sequence-diagram, this wont hurt either.
3) Fix the bug/develop the new feature based on your design.
4) Write a test. Test any case you could come up with.
5) Open a pull-request based on the provided pull-request template onto the branch your branch branched from.
6) Add a direct contributor as an reviewer to your pull-request
7) Add a direct contributor as an assignee to your pull-request
6) If your pull-request gets denied, start over from 3.

### Releases

The repository owner reserves the right to provide a new release to maven-central.

### Becoming a direct Contributor

To become a direct contributor, you have to apply to the owner of this repository.

You should already have:

 * Extensive knowledge about software-design
 * Fluent in UML
 * Fluent in Java
 * Extensive knowledge about Client-Server-Communication
 * Optional: Done previous bug-fixes in this repository
 
You are granted:
 
 * The right to push.
 * The right to speak in behalf of NetCom2.
 * The right to alter the existing templates (requires the acknowledge and the okay from the repository owner)
 * Whatever you don't know, you will be taught. (The repository-owner does not have all the time in the world, so please resign to any direct contributor for help)
 
The repository owner keeps a local copy of the project. If you perform any malicious action or cause a major break, the repository owner reserves the right to remove you as a direct contributor.

**Note:** You do *NOT* have the right to release a new version! This right might however be granted temporarily by the repository owner.
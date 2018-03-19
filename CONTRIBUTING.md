# Branches

The main branch is the __master__-branch. This branch is meant only for new releases.

The main work-branch is __dev__. New features as well as bug-fixes are merged into this branch. This branch is branched from master.

The hot-spot-branch is the __NIGHTLY__ branch. Every new branch (except for urgent fixes) comes from/goes into this branch. This branch is branched from dev.

Every change in any of these branches requires an pull-request.

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

To correctly contribute on NetCom2, follow the following guidelines:


### Conventions

 * The code-conventions in this repository are the default Java conventions.
 * Intend your code with taps rather than 4 spaces.

### External Contributions

If you want to provide a new feature, a bug-fix, please follow these steps:

0) Fork this repository
1) Create a branch named exactly as the issue you are working on. If the issue has the label "urgent", branch from dev, else branch from NIGHTLY.    
2) If you want to develop a new feature: Design your code. A simple class-diagram is enough, but if you go the extra mile to also provide a sequenz-diagramm, this wont hurt either.
3) Fix the bug/develop the new feature based on your design.
4) Write a test. Test any case you could come up with.
5) Open a pull-request based on the provided pull-request template onto the branch your branch branched from.
6) If your pull-request get's denied, start over from 3.

**NOTE**: Only change thing relevant to your issue! If your pull-request changes anything, that is not important, your pull-request will be denied!

If you want to contribute a new feature to NetCom2, you should have extensive knowledge about NetCom2 and Client-Server-Communications.

In most cases, pull-request from external contributors about urgent Issues or new features will be denied.

### Direct Contributions

If you are a direct contributor (you have push rights to this repository), please follow the following guid

1) Create a branch named exactly as the issue you are working on. If the issue has the label "urgent", branch from dev, else branch from NIGHTLY.    
2) If you want to develop a new feature: Design your code. A simple class-diagram is enough, but if you go the extra mile to also provide a sequenz-diagramm, this wont hurt either.
3) Fix the bug/develop the new feature based on your design.
4) Write a test. Test any case you could come up with.
5) Open a pull-request based on the provided pull-request template onto the branch your branch branched from.
6) If your pull-request get's denied, start over from 3.

### Releases

The right to provide a new release lies by the repository owner.

### Becoming a direct Contributor

For becoming a direct contributor, you have to apply to the owner of this repository.

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
 
The repository owner holds a local copy of the project. If you do any malicious action or an major break, the repository owner holds his right to remove you as a direct contributor.

**Note:** You do *NOT* have the right to release a new version! This right might however be granted temporary by the repository owner.
# Workflow

To correctly contribute on NetCom2, follow the following guidlines:

### Branches

The main branch is the __master__-branch. This branch is meant only for new releases.

The main work-branch is __dev__. New features as well as bug-fixes are merged into this branch. This branch is branched from master.

The hot-spot-branch is the __NIGHTLY__ branch. Every new branch (except for urgend fixes) go into this branch. This branch is branched from dev.

Every change in any of these branches requires an pull-request.

If you want, you may freely perform a __pull-down-update__. This means, merging the master into dev and merging dev into NIGHTLY. Please add to your pullrequest the comment "pull-down-update".

If you want to provide a new feature, a bug-fix, please follow these steps:

0) Fork this repository
1) Create a branch named exactly as the issue you are working on. If the issue has the label "urgent", branch from dev, else branch from NIGHTLY.
2.1) If you want to develop a new feature: Design your code. A simple class-diagram is enough, but if you go the extra mile to also provide a sequenz-diagramm, this wont hurt either.
2.2) Fix the bug/develop the new feature based on your design.
2.3) Write a test. Test any case you could come up with.
3) Open a pull-request based on the provided pull-request template onto the branch your branch branched from.
4) If your pull-request get's denied, rins repeat.

**NOTE**: Only change thing relevant to your issue! If your pull-request changes anything, that is not important, your pull-request will be denied!

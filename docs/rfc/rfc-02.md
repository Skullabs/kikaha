# `RFC-2` - Low overhead principle
Any feature maybe included in the framework as long as it has (close to) no overhead has been introduced.

## Tracking issues
- **Pull Request**: [#261](https://github.com/Skullabs/kikaha/pull/261)
- **Issue**: -

## Motivation
It should be clear to the community that new RFC should not affect the framework efficiency, footprint
or usability. Once Kikaha is responsible to abstract repetitive lines of code from the developer's daily routine, people
trust on this framework as a backbone to their codebase, making it sensible to drastic changes.

A more strict judgement about new RFCs should be valuable as long as it keep the framework easy to use and leverage
high performance to the users.

## Solution Overview
Every RFC, from now on, should be assessed taking into account the following points:
1. It **should not** introduce performance issues into the framework.
2. It **should not** make harder for users to use or understand the new feature.
3. It **shuold not** overlap other RFC in a way Kikaha would have more than one way to achieve the expected result.

## Drawbacks
This would introduce hours of discussion within the team, making the development pace a bit slower.

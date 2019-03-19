# `RFC-1` - Requests for Comments
A transparent and colaborative model to include and define new features or expected behavior for Kikaha.

## Tracking issues
- **Pull Request**: [#260](https://github.com/Skullabs/kikaha/pull/260)
- **Issue**: -

## Motivation
Kikaha was conceived as a tiny layer over the almight [Undertow](http://undertow.io/), wrapping it in a way developers of all
levels would take advantage from its impressive performance without imposing significant runtime overhead. Since its debut,
a lot of effort was put into Kikaha's development to include useful of features, although accourding with feedback we've
received, only a few of them have actually being widely used by our code base.

To avoid waste time, and money in some cases, put unnecessary development effort into something that people don't actually
need, Kikaha will be guided by a RFC model, in a similar approach to what the Rust maintainers has been doing on its
development process.

## Solution Overview
Once this RFC-1 is approved, every new feature, or change of a previously approved behavior, will be required to follow the
following steps in order to be incorporated into the Kikaha's version 3.X.X or later.

1. Every new feature should have a Pull Request with a grasp of the idea about it.
2. The pull request should be accompanied by a new file placed in the `/docs/rfc/`. This file should:
   - Have the name starting with `rfc-` followed by a number identifing the RFC.
   - It should follow the same structure as described in the `Template Structure` below.
   - It should cover at least the three first items of the `Template Structure`.
3. The Pull Request will be used to reach agreement.
4. Once agreement is reached the development can be proceed, although the Pull Request will be still opened for further discussions.
5. After the development is finished, and no amendment is required, a documentation page should be included in the `/docs/` folder.
6. Once finished, the branch can be merged and the Pull Request can be successfully closed.

## Template Structure
The following snippet should be used as template for further RFCs.
```md
# `RFC-#` - Title of the proposal
A brief introduction to the subject - usually in one or two phrases.

## Tracking issues
- **Pull Request**: - #12345678
- **Issue**: - #12345678 (if any issue has motivated the Pull Request)

## Motivation
Here should be given enough context for the audience to proper understand why this feature/enhancement should be
included into the framework. 

## Solution Overview
Describe clearly what should be done and how it should be done. This is specially important to highlight the important
architecture aspects of this solution and give users enough resource to understand how to take advantage of this new
important feature/enhancement.

## Drawbacks
Well, nothing is perfect:
1. It completely acceptable to introduce a temporary feature and have it improved in a further attempt.
1. It may also happen to introduce something new that can mean a huge compatibility impact with previous versions.
1. Any other concerns that may be seen as a concern to the future of the Framework.

Whenever we know upfront that may be a problem, it should be described here.
```

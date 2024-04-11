# Tags #

(Last updated 1 April 2024.)

A Communiqué tag is an item such as `nation:imperium_anglorum` or `region:europe`. Tags are in defined by the
combination of two classes: `CommuniqueRecipientType` and `CommuniqueFilterType`. Those classes' enumerated items (and
the relevant `toString` methods) give the most up-to-date tag definitions. They are parsed
by `CommuniqueRecipient.parseRecipient`.

For the most part a tag designates a nation or a collection thereof. The prefix `+`, `-`, or no prefix determines how it
should be used.

## Tags ##

Tag names were initially assigned to be consistent with NationStates but over time have become more complex.

### Simple tags ###

The simplest tag is a mere name such as `imperium_anglorum`. This is technically not a valid tag but is handled sui
generis; this is turned into `nation:imperium_anglorum` on save. Other simpler tags include:

* `region:europe`
* `tag:wa` and `tag:delegates`, `tag:new` (`tag:all` is not supported; `tag:new####` is not supported; `tag:new` with no
  numbers returns the newest 50 nations)
* `endorsers_of:imperium_anglorum` lists all the endorsers of the nation `imperium_anglorum`

### Complex tags ###

Some tags take parameters. These are enforced at runtime and if they have a specific number of parameters, parameter
counts are enforced by `CommuniqueSplitter`.

All _stateful_ tags are prefixed with an underscore. Eg `_approvals`. They are stateful because generating recipients
from them can take time (movement into a region is not something that can be assessed at a single point in time but
rather must be observed between two updates) or alternatively because they can exhaust. A tag exhausts when the
conditions that went into its making are no longer met. Eg `_approvals:given_to;PROPOSAL_ID` no longer has effect when
the proposal expires or goes to vote.

* `_movement` takes two parameters, a direction (`out_of` or `into`) and a region. Eg `_movement:out_of;europe`.
* `_approvals` takes two parameters, an action (`given_to` or `removed_from`) and a proposal ID.
  Eg `_approvals:given_to;imperium_anglorum_1711797410`.
* `_voting` takes two parameters, a chamber (`ga` or `sc`) and a position (`for` or `against`)

`_voting` is taken to exhaust because it is no good to send the same telegram once the resolution at vote has changed.

Some tags break the normal rules because they do not actually decompose to lists of nations. This is the case
with `-regex:REGEX` and `+regex:REGEX`. The prefixes are covered in the next section, so read ahead, but the effect of
these two tags is to filter nation names by whether they match the regular expression. This can force a matching
nation's exclusion or inclusion, respectively.

## Prefixes ##

There are three prefixes that can be given. They are processed in order given.

The first, which is no prefix at all, merely adds the nations into which the
tag decomposes to the ending recipient list. In the simplest case, `nation:imperium_anglorum` merely sets
up `imperium_anlgorum` to be telegrammed.

The second prefix is `-`. It asks for the nations identified by that tag _not_ to be included. A query such
as `tag:delegates // -region:europe` first adds all the delegates. It then removes from that list every nation that is
present in Europe. If the delegate of Europe is `imperium_anglorum`, this means that he is excluded.

THe third prefix is `+`. It asks requires that all nations so far enumerated appear in the list. Consider for
example `tag:delegates // +region:europe`; this lists all the delegates. It then requires all the delegates be present
in Europe. The combinations of the two commands therefore leaves only the Delegate for Europe.

## Combination ##

Communiqué's tags in combination can be very powerful. For example, if you want a list of WA members in The North
Pacific, you can write two lines `region:the_north_pacific // +tag:wa`. Multiple tags can also be used at once.

Thus, if you want to telegram the newest 50 nations which have names that do not end in a number that are present in the
Pacifics:

```
tag:new                    # new nations
+region:the_pacific        # who are in the pacific
+region:the_north_pacific  # who are in TNP
+region:the_south_pacific  # who are in TSP
+region:the_east_pacific   # who are in TEP
+region:the_west_pacific   # who are in TWP
-regex:.*[0-9]$            # with names that do not end with a number
```

This can also be used with stateful tags as well. Something such as all nations who moved out of Lazarus, Balder, and
Osiris, who voted in favour of the Security Council resolution, are not delegates, and have a nation name starting
with `a` can be expressed as:

```
_movement:out_of;lazarus  # people who left lazarus
_movement:out_of;balder   # people who left balder
_movement:out_of;osiris   # people who left osiris
+_voting:sc;for           # who are voting for the SC resolution
-tag:delegates            # are not delegates
+regex:^a.*               # and have names that begin with an A
```

A perhaps more useful use of this would be in a proposal counter-countercampaigns. The following would send telegrams to
every nation that once approved a proposal and stopped doing so that is also, at the time of parsing, a delegate (
excluding players who gave approvals and withdrew them because they lost their delegate offices).

```
_approvals:removed_from;PROPOSAL_ID  # people who removed their approval
+tag:delegates                       # who are delegates
```

Another useful syntax is a supplementary telegram campaign to active delegates seeking approvals to stave off a
proposal expiry in the queue:

```
_happenings:active                # active players
+tag:delegates                    # who are delegates
-_approvals:given_to;PROPOSAL_ID  # who have not approved this proposal
```

Combination of tags can also be used to get a list of WA nations who have not endorsed someone in a region (a topic not
directly supported by the `endorser_of` tag) by chaining the following commands (remember that `SOME_NATION` cannot
endorse _itself_):

```
region:REGION              # residents of REGION
+tag:wa                    # who are WA members
-endorsers_of:SOME_NATION  # who did not endorse SOME_NATION
-nation:SOME_NATION        # who are not SOME_NATION
```

## Tag naming conventions ##

Tags should be named in a way which either is clear in conveying their meaning or approximate of a nominative phrase.
Thus, `region:europe` is all the nations in Europe (remember that every tag must be decomposable to a list of nations)
and `_approvals:given_to;PROPOSAL_ID` similarly is a nominative phrase describing the nations approving a proposal.

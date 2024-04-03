# Repeat #

The repeat system relies on the `repeatInterval` parameter rather strongly. You can think of the repeat system as
basically:

* Launch old Communique normally
* Parse the configuration to get the list of recipients
* Wait the repeat interval
* Interrupt Communique
* Reparse the configuration
* Continue doing this until parsing the configuration no longer makes any sense

## State ##

Tags prefixed with `_` are stateful. That means that either they cannot be generated without waiting or the results
thereof are themselves self-invalidating. This therefore includes tags such as `_movement`, and `_approvals`. Recipients
_cannot_ be given except over time by `_movement`. For `_approvals`, `_approvals:given_to; PROPOSAL_ID` lists all
approvals currently given which can exhaust if the proposal stops existing (going to vote or expiring); on the other
hand, `_approvals:removed_from; PROPOSAL_ID` gives the list of all persons who ever approved it and are not approving any
more.

All of these tags still work with the non-stateful tags. Thus, one can set up a counter-counter-campaign by writing:

```
_approvals:removed_from; MY_FRIENDS_PROPOSAL
+tag:delegates
```

Setting this to repeat every 15 minutes then will take the list of all people who removed their approvals, remove all on
that list who are not delegates, and then send telegrams to them.

## Timing ##

When Communiqué repeats, it will call for reparse 10 seconds before the next telegram can be sent. For this example,
which assumes that a telegram parse takes one second after Communiqué start:

* T+0s: Start recruitment
* T+1s: Parse recruitment
* T+1s: Send first recruitment telegram and wait 180 seconds (T+181)
* T+171s: Reparse recruitment
* T+172s: Finish reparse recruitment and save the new list of recipients
* T+181s: Send using the new recipient list

If parsing takes more than 10 seconds, which should probably never happen, then Communiqué will block until parsing is
complete and send when recipients are available.

## API rate limits ##

Communiqué does not check for _other programs_ which use the NationStates API. It will always remain within the API
limitations. In fact, the implementation at `NSConnection` rate limits Communiqué to one call every 750 milliseconds to
allow for other programs possibly to make calls.

However, because of this, when repeat mode is active, a complex parsing request will take a majority of the API call
budget. This means that Communiqué along with some other program running at the same time will cause the API rate limit
to trigger.

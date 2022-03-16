# JavaCard Secret Storage

* Based on JavaCard `3.0.5`

## Design assumptions

* somehow verify PIN
* split branches DURESS_PIN
* authenticated ECDH for communication

### Supported APDU calls

* set(key, value)
* get(key)
* del(key)
* list()

## Encountered problems

```
What can be a key?
How long can a value be?

Communication begins with authenticated ECDH, secure channel is established. 

Keys for the AES encrypted secure channel (from APDU applet to the applet on the card) are established via authenticated EDCH.

Each of the stored secrets is encrypted via AES-256, used media key is derived from the PIN supplied/sent by the user from the APDU via secure channel.
```

## Current state

Meditative implementation only.

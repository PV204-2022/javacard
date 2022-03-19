# JavaCard Secret Storage

### Supported APDU calls

All instructions/calls begin with the "instruction selection process", data received after is handled in the following manner.

* set(key, value)
  * check key has appropriate length
    * exception/trim
  * check data has appropriate length
    * exception/trim
  * encrypt data with media key
  * allocate memmory in persistent storage
  * store data
  * *return OK*
* get(key)
  * check key has appropriate length
    * exception/trim
  * get data
  * decrypt data with media key
  * *return data*
* list()
  * **If keys aren't encrypted we need to authenticate this somehow.**
  * *return list of all keys*

### Communication initialization

* Communication begins with authenticated ECDH, secure channel is established. Keys used for this channel are ephermeral.
* PIN is sent via this secure channel and verified.
  * If `DURESS_PIN` is entered than the media key is deleted. 
  * Before attempted decryption of media key, counter of attempts is decreased (counter initialized to 3, if 0, remove media key). 
    * We probably could use `mod X` to mask the counter/prevent manipulation.
  * Media key is decrypted and put into memory.
* *Card waits for ADPU calls.*

## Algorithms

* *work-in-progress*

## Encountered problems / Questions

* **We need to test this on a physical card, which versions are supported by the cards in the lab?**
* Do we encrypt keys as well? (probably not)
* Will all keys have constant length padded by zeros, or will we use just-enough?
* Are using key-derivation or hashes for `PIN`/`DURESS_PIN`?
* Are we supposed to attempt protection against side-channel attacks?
* How long the stored value/key can be?
* Do we assume that all values/keys are strings (or binary)?

## Proposed APDU demo

Proposed demo order of APDU calls. 

* *enter PIN*
  * add key1:value1 with appropriate length
  * add key2:value2 with appropriate length
  * add key3:value3 with appropriate length
  * (try to) add key:value with too long value
  * (try to) add key:value with too long key
  * list all keys
    * returned values depend on how we handle errors
* *disconnect* (artificially using JcardSIM)
* *enter DURESS_PIN* (all following calls should fail)
  * add key1:value1 with appropriate length
  * add key2:value2 with appropriate length
  * add key3:value3 with appropriate length
  * (try to) add key:value with too long value
  * (try to) add key:value with too long key
  * list all keys
    * returned values depend on how we handle errors

## Current state

Meditative implementation only.

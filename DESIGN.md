# JavaCard Secret Storage

### Supported APDU calls

All instructions/calls begin with the "instruction selection process", data received after is handled in the following manner.

* *instruction* set(key, value)
  * check key has appropriate length
    * exception/trim
  * check data has appropriate length
    * exception/trim
  * encrypt data with media key
  * allocate memmory in persistent storage
  * store data
  * *return OK*
* *instruction* get(key)
  * check key has appropriate length
    * exception/trim
  * get data
  * decrypt data with media key
  * *return data*
* *instruction* list()
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

Picked fitting algorithms from [the provided documentation](https://docs.oracle.com/javacard/3.0.5/api/index.html).

* establishment of the secure channel
  * ellyptical diffie-hellman
* encryption of the secure channel
  * some kind of aes
* media key derivation
  * pdbkf (based on hash?)
* media encryption 
  * some kind of aes again

## Encountered problems / Questions

During the design phase (as well as the little implementation we have done), we have encountered the following issues/decisions. The answeres/solutions provided in this document might be subject to change during the Phase 3.

* **We need to test this on a physical card, which versions are supported by the cards in the lab?**
* Do we encrypt keys as well? (probably not)
  * No, arbitrary decision.
* Will all keys have constant length padded by zeros, or will we use just-enough?
* Are using key-derivation or hashes for `PIN`/`DURESS_PIN`?
* Are we supposed to attempt protection against side-channel attacks?
* How long the stored value/key can be?
  * Changeable, in `Configuration.java`
* Do we assume that all values/keys are strings (or binary)?
* Aren't we supposed to implement "delete" instruction as well?
* Describe the authentication process in detail
  * How does it prevent listing of keys? (since they're not encrypted)

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
* *enter DURESS_PIN* 
  * use the same calls, but should fail

## Current state

Due to slight planning oversight on our part, we currently do not have a working prototype. However, ...

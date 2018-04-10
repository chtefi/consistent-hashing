# Consistent Hashing

A simple implementation of the consistent hashing technique.

Tests are using scalacheck to test tons of different setups and compute the standard deviation of the nodes and vnodes keys distributions.

Two hashes are being used:
- Murmur3 hash
- Jump hash (constraint: it needs the bucket size)


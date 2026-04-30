# Rendezvous

A simple rendezvous server for NAT hole-punching. Peers post their connection
info to a shared mailbox code; the server returns all entries in that mailbox
so peers can attempt a direct connection.

## Requirements

- [Clojure CLI](https://clojure.org/guides/install_clojure) 1.11+

## Running

```
clojure -M:run -i <interface>
```

For example, to listen on loopback:

```
clojure -M:run -i 127.0.0.1
```

Or on a specific LAN interface:

```
clojure -M:run -i 192.168.1.10
```

The server listens on port 8080.

## API

### POST `/rendezvous/<code>`

Register your presence in a mailbox. `<code>` must be exactly 6 alphanumeric
characters. The server records your observed IP automatically.

**Request body:**
```json
{ "id": "some-peer-identifier", "port": 1337 }
```

**Response:** JSON array of all current entries in the mailbox.

```json
[
  { "id": "peer-a", "ip": "1.2.3.4", "port": 51234 },
  { "id": "peer-b", "ip": "5.6.7.8", "port": 49012 }
]
```

### GET `/rendezvous/<code>`

Read the current entries in a mailbox without adding one.

**Response:** Same format as POST.

---

Entries are de-duplicated by `id` (re-posting with the same id updates the
entry) and expire automatically after 90 seconds.

## Running tests

```
clojure -X:test
```

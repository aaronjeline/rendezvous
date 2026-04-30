## API

### POST `/rendezvous/<code>`

Register your presence in a mailbox. `<code>` must be exactly 6 alphanumeric
characters. The server records your observed IP automatically.

**Request body:**
```json
{ "id": "some-peer-identifier", "port" : 1337 }
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



I think:

TemporaryNode START - Works
TemporaryNode GET - Works
TemporaryNode Store - Maybe?

FullNode GET - Works
FullNode START - Works
FullNode ECHO? - Works
FullNode END - Works
FullNode NOTIFY? - Works
FullNode NEAREST? - Not implemented, made it so other nodes are notified if they try or send wrong format
FullNode PUT? - Not implemented, made it so other nodes are notified if they try or send wrong format

Forgot to prepare Wireshark recording, when I realised it was too late, the VM reset and I didn't have time to find
an unzipper (we didn't get one by default) and do everything

But my application seemed to always send the right stuff, it just didn't receive the positive answers I expected :(

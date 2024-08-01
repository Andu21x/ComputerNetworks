Feedback from the teacher:

**General Comments**
The use of interfaces enhances modularity.
Incomplete Functionality. (Made sure what doesn't work won't hinder the network)
Code is generally well-commented.

**Automated Marks**
[FN 2:1] TCP connection accepted.
[FN 2:2] Accepted connection to the 2D#4 network.
[FN 2:3] Accepted connection to the 2D#4 network.
[FN 3:1] Correct response to ECHO.
[FN 5:1] GET correctly fails when the key has not been PUT.
[FN 6:1] Responded to a NOTIFY request.

**Review Comments (RB)**
[RB 3] Test failures are rare.
[RB 7] Hangs are rare.

**Test Notes (TN)**
[TN 1:1] TCP connection initiated.
[TN 1:2] START message sent.
[TN 1:3] Connected to the 2D#4 network.
[TN 3:1] Made a PUT request.
[TN 3:2] Stored a single-line (key, value) pair.
[TN 3:3] Stored a multi-line (key, value) pair.
[TN 4:1] Made a GET request.
[TN 4:2] Found value when present.

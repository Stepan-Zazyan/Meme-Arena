# Rate limiting

The backend includes a compact in-memory fixed-window limiter isolated behind `RateLimiterService`.

Current limits:

* guest creation: 5 per hashed IP per hour;
* voting: 120 per session per minute;
* image upload: 10 per user per hour and 30 per user per day;
* meme submission: 20 per user per day.

Exceeded limits return HTTP 429, `RATE_LIMIT_EXCEEDED`, and `Retry-After`.

This limiter is correct only for a single backend instance. Before horizontal scaling, replace the implementation with a distributed backend such as Redis or a database-backed/token-bucket implementation while keeping the interface.

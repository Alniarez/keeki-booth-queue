# Keeki Booth Queue

Queue management system for the Keekihime booth. Attendants register visitors into time blocks.


## Running with Docker

```bash
docker build -t keeki-booth .
docker run -p 7070:7070 -v $(pwd)/data:/app/data keeki-booth
```

The `-v` flag mounts a local `data/` directory for H2 database persistence across restarts.

## Pages

- `/` — queue view for the current day; tap a block to book a slot
- `/admin` — admin view; browse any date, see all bookings, delete entries

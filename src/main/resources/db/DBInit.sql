PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS app_config (
                                          key TEXT PRIMARY KEY,
                                          value TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS sessions (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        start_ts INTEGER NOT NULL,
                                        end_ts INTEGER,
                                        type TEXT NOT NULL,
                                        completed INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS window_usage (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                                            start_ts INTEGER NOT NULL,
                                            end_ts INTEGER NOT NULL,
                                            process_name TEXT,
                                            window_title TEXT
);

CREATE TABLE IF NOT EXISTS events (
                                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                                      ts INTEGER NOT NULL,
                                      type TEXT NOT NULL,
                                      description TEXT
);

CREATE TABLE IF NOT EXISTS reminders (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         day TEXT NOT NULL,          -- yyyy-MM-dd
                                         text TEXT NOT NULL,
                                         created_ts INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sessions_start ON sessions(start_ts);
CREATE INDEX IF NOT EXISTS idx_window_start ON window_usage(start_ts);
CREATE INDEX IF NOT EXISTS idx_reminders_day ON reminders(day);

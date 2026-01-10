package com.zenflow.dao;

import com.zenflow.db.DBHelper;
import com.zenflow.model.Session;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionDAOTest {

    @BeforeAll
    static void setup() {
        // Shared in-memory SQLite database
        DBHelper.setDbUrl("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
        DBHelper.initDatabase();
    }

    @Test
    void testInsertAndQuery() {
        SessionDAO dao = new SessionDAO();

        Session s = new Session();
        s.setStartTs(System.currentTimeMillis());
        s.setType("FOCUS");
        s.setCompleted(0);

        Long id = dao.addSession(s);
        assertNotNull(id);

        long now = System.currentTimeMillis();
        List<Session> items =
                dao.getSessionsByDateRange(now - 60_000, now + 60_000);

        assertFalse(items.isEmpty());
        assertEquals("FOCUS", items.get(0).getType());
    }
}

package ru.sber.df.epmp.netty_postgres.utils.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlHintsTest {

    @Test
    void containsHint() {
        assertFalse(SqlHints.containsHint("/* */ select 2"));
        assertFalse(SqlHints.containsHint("/* datagate */ select 2"));
        assertFalse(SqlHints.containsHint("/* datagate raw mode */ select 2"));

        assertTrue(SqlHints.containsHint("/* datagate_raw_mode */"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=0 */"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=1 */"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=true */"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=TRUE */"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=false */"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=FALSE */"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=aaaaaaaa */"));

        assertTrue(SqlHints.containsHint("/* datagate_raw_mode */select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=0 */select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=1 */select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=true */select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=TRUE */select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=false */select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=FALSE */select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=aaaaaaaa */select 2"));

        assertTrue(SqlHints.containsHint("/* datagate_raw_mode */ select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=0 */ select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=1 */ select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=true */ select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=TRUE */ select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=false */ select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=FALSE */ select 2"));
        assertTrue(SqlHints.containsHint("/* datagate_raw_mode=aaaaaaaa */ select 2"));

        assertTrue(SqlHints.containsHint("select/*datagate_raw_mode=true*/ 2"));
        assertTrue(SqlHints.containsHint("select/*datagate_raw_mode=true*/ 2"));

        assertTrue(SqlHints.containsHint("select /* datagate_raw_mode=true */ 2"));
        assertTrue(SqlHints.containsHint("select /* datagate_raw_mode=true */ 2"));

        assertTrue(SqlHints.containsHint("select 2/* datagate_raw_mode=true */"));
        assertTrue(SqlHints.containsHint("select 2/* datagate_raw_mode=true */"));
    }

    @Test
    void isDatagateRawModeEnabled() {
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* */ select 2"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate */ select 2"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate raw mode */ select 2"));

        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode */"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=0 */"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=1 */"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=true */"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=TRUE */"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=false */"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=FALSE */"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=aaaaaaaa */"));

        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode */select 2"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=0 */select 2"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=1 */select 2"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=true */select 2"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=TRUE */select 2"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=false */select 2"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=FALSE */select 2"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=aaaaaaaa */select 2"));

        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode */ select 2"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=0 */ select 2"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=1 */ select 2"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=true */ select 2"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=TRUE */ select 2"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=false */ select 2"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=FALSE */ select 2"));
        assertFalse(SqlHints.isDatagateRawModeEnabled("/* datagate_raw_mode=aaaaaaaa */ select 2"));

        assertTrue(SqlHints.isDatagateRawModeEnabled("select/*datagate_raw_mode=true*/ 2"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("select/*datagate_raw_mode=true*/ 2"));

        assertTrue(SqlHints.isDatagateRawModeEnabled("select /* datagate_raw_mode=true */ 2"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("select /* datagate_raw_mode=true */ 2"));

        assertTrue(SqlHints.isDatagateRawModeEnabled("select 2/* datagate_raw_mode=true */"));
        assertTrue(SqlHints.isDatagateRawModeEnabled("select 2/* datagate_raw_mode=true */"));
    }
}
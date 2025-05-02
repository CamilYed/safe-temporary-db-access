package pl.pw.cyber.dbaccess.testing.dsl.assertions

trait DatabaseResultAssertion {

    static DatabaseRowsAssertion assertThatRows(List<Map<String, Object>> rows) {
        return new DatabaseRowsAssertion(rows)
    }

    static class DatabaseRowsAssertion {
        private final List<Map<String, Object>> rows

        DatabaseRowsAssertion(List<Map<String, Object>> rows) {
            this.rows = rows
        }

        DatabaseRowsAssertion hasRowWithId(Number id, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DatabaseRowAssertion) Closure closure = {}) {
            def row = rows.find { it.id == id }
            assert row != null : "No row with id $id found"
            def assertion = new DatabaseRowAssertion(row)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.delegate = assertion
            closure.call()
            return this
        }

        DatabaseRowsAssertion hasNumberOfRows(int expected) {
            assert rows.size() == expected : "Expected $expected rows but found ${rows.size()}"
            return this
        }
    }

    static class DatabaseRowAssertion {
        private final Map<String, Object> row

        DatabaseRowAssertion(Map<String, Object> row) {
            this.row = row
        }

        void hasAmount(BigDecimal expected) {
            assert row.amount == expected : "Expected amount $expected but was ${row.amount}"
        }

        void hasColumnValue(String column, Object expectedValue) {
            assert row[column] == expectedValue : "Expected $column to be $expectedValue but was ${row[column]}"
        }
    }
}

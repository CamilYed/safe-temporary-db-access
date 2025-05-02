package pl.pw.cyber.dbaccess.testing.dsl.builders

import groovy.transform.builder.Builder

@Builder
class TableDefinitionBuilder {

    String tableName
    List<String> columns = []
    List<Map<String, Object>> rows = []

    static TableDefinitionBuilder aSimpleOrdersTable() {
        new TableDefinitionBuilder()
                .withTableName("orders")
                .withColumns(["id SERIAL PRIMARY KEY", "amount DECIMAL(10,2)"])
                .withRow([amount: 100.50])
                .withRow([amount: 200.75])
    }

    TableDefinitionBuilder withTableName(String name) {
        this.tableName = name
        return this
    }

    TableDefinitionBuilder withColumns(List<String> columns) {
        this.columns = columns
        return this
    }

    TableDefinitionBuilder withRow(Map<String, Object> row) {
        this.rows << row
        return this
    }

    TableDefinitionBuilder withColumn(String column) {
        this.columns << column
        return this
    }

    static TableDefinitionBuilder table(String tableName, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = TableDefinitionBuilder) Closure closure) {
        def builder = new TableDefinitionBuilder()
        builder.tableName = tableName
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
        return builder
    }
}

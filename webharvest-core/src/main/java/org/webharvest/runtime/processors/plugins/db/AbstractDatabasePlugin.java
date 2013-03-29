/*
 Copyright (c) 2006-2012 the original author or authors.

 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above
   copyright notice, this list of conditions and the
   following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

 * The name of Web-Harvest may not be used to endorse or promote
   products derived from this software without specific prior
   written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
*/

package org.webharvest.runtime.processors.plugins.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.webharvest.exception.DatabaseException;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.Variable;

/*
 * This code has been extracted in unchanged form from the DatabasePlugin class.
 */
public abstract class AbstractDatabasePlugin extends WebHarvestPlugin {

    /**
     * Returns configured, ready for use database {@link Connection}
     *
     * @param scraper
     *            {@link DynamicScopeContext} instance of executed scraper
     * @return ready for use database {@link Connection}
     */
    protected abstract Connection obtainConnection(DynamicScopeContext context);

    private class DbParamInfo {
        private Variable value;
        private String type;

        private DbParamInfo(final Variable value, final String type) {
            this.value = value;
            this.type = type;
        }
    }

    private List<DbParamInfo> dbParams = null;

    public String getName() {
        return "database";
    }

    public Variable executePlugin(final DynamicScopeContext context)
            throws InterruptedException {

        final Connection conn = obtainConnection(context);

        Variable body = executeBody(context);
        String sql = body.toString();

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            int index = 1;
            if (dbParams != null) {
                for (DbParamInfo paramInfo : dbParams) {
                    if ("int".equalsIgnoreCase(paramInfo.type)) {
                        try {
                            int intValue = Integer.parseInt(paramInfo.value
                                    .toString());
                            statement.setInt(index, intValue);
                        } catch (NumberFormatException e) {
                            throw new PluginException(
                                    "Error in SQL statement - invalid integer!",
                                    e);
                        }
                    } else if ("long".equalsIgnoreCase(paramInfo.type)) {
                        try {
                            long longValue = Long.parseLong(paramInfo.value
                                    .toString());
                            statement.setLong(index, longValue);
                        } catch (NumberFormatException e) {
                            throw new PluginException(
                                    "Error in SQL statement - invalid long!", e);
                        }
                    } else if ("double".equalsIgnoreCase(paramInfo.type)) {
                        try {
                            double doubleValue = Double
                                    .parseDouble(paramInfo.value.toString());
                            statement.setDouble(index, doubleValue);
                        } catch (NumberFormatException e) {
                            throw new PluginException(
                                    "Error in SQL statement - invalid long!", e);
                        }
                    } else if ("binary".equalsIgnoreCase(paramInfo.type)) {
                        statement.setBytes(index, paramInfo.value.toBinary());
                    } else {
                        statement.setString(index, paramInfo.value.toString());
                    }
                    index++;
                }
            }

            statement.execute();
            resultSet = statement.getResultSet();

            if (resultSet != null) {
                ResultSetMetaData metadata = resultSet.getMetaData();
                ListVariable queryResult = new ListVariable();
                int columnCount = metadata.getColumnCount();
                DbColumnDescription[] colDescs =
                    new DbColumnDescription[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    String colName = metadata.getColumnLabel(i);
                    int colType = metadata.getColumnType(i);
                    colDescs[i - 1] = new DbColumnDescription(colName, colType);
                }

                int rowCount = 0;
                while (resultSet.next()) {
                    Object[] rowData = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        switch (colDescs[i].getType()) {
                        case Types.TIME:
                            rowData[i] = resultSet.getTime(i + 1);
                            break;
                        case Types.TIMESTAMP:
                            rowData[i] = resultSet.getTimestamp(i + 1);
                            break;
                        case Types.DATE:
                            rowData[i] = resultSet.getDate(i + 1);
                            break;
                        case Types.FLOAT:
                            rowData[i] = resultSet.getFloat(i + 1);
                            break;
                        case Types.DOUBLE:
                        case Types.DECIMAL:
                        case Types.NUMERIC:
                        case Types.REAL:
                            rowData[i] = resultSet.getDouble(i + 1);
                            break;
                        case Types.SMALLINT:
                        case Types.INTEGER:
                        case Types.TINYINT:
                            rowData[i] = resultSet.getInt(i + 1);
                            break;
                        case Types.BLOB:
                        case Types.BINARY:
                        case Types.VARBINARY:
                        case Types.LONGVARBINARY:
                            rowData[i] = resultSet.getBytes(i + 1);
                            break;
                        default:
                            rowData[i] = resultSet.getString(i + 1);
                            break;
                        }
                    }

                    queryResult
                            .addVariable(new DbRowVariable(colDescs, rowData));
                    rowCount++;
                }
                return queryResult;
            } else {
                return EmptyVariable.INSTANCE;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
        }

    }

    void addDbParam(final Variable value, final String type) {
        if (dbParams == null) {
            dbParams = new ArrayList<DbParamInfo>();
        }
        dbParams.add(new DbParamInfo(value, type));
    }
}

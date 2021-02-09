package koral.guildsaddons.database.statements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static koral.guildsaddons.database.DatabaseConnection.hikari;

abstract class Statements {
    interface StatementConsumer {
        void accept(PreparedStatement statement) throws SQLException;
    }
    interface StatementFunction<T> {
        T apply(ResultSet resultSet) throws SQLException;
    }

    static void setter(String sql, StatementConsumer preparedExecute) {
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            connection = hikari.getConnection();

            statement = connection.prepareStatement(sql);

            preparedExecute.accept(statement);

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(connection, statement);
        }
    }
    static void stringSetter(String sql, String... args) {
        setter(sql, statement -> {
            for (int i=0; i < args.length; i++)
                statement.setString(i + 1, args[i]);
        });
    }


    static <T> T getter(String sql, StatementConsumer preparedExecuteQuarry, StatementFunction<T> func) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        T result = null;

        try{
            connection = hikari.getConnection();
            statement = connection.prepareStatement(sql);

            preparedExecuteQuarry.accept(statement);

            resultSet = statement.executeQuery();

            result = func.apply(resultSet);
        }catch (SQLException e){
            e.printStackTrace();
        } finally {
            closeAll(connection, statement, resultSet);
        }

        return result;
    }
    static <T> T stringGetter(String sql, StatementFunction<T> func, String... args) {
        return getter(sql, statement -> {
            for (int i=0; i < args.length; i++)
                statement.setString(i + 1, args[i]);
        }, func);
    }


    public static void closeAll(AutoCloseable... toClose) {
        for (AutoCloseable closeable : toClose)
            if (closeable != null)
                try {
                    closeable.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }
}

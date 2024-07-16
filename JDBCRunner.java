import java.sql.*;

public class JDBCRunner {
    private static final String PROTOCOL = "jdbc:postgresql://";
    private static final String DRIVER = "org.postgresql.Driver";
    private static final String URL_LOCALE_NAME = "localhost/";
    private static final String DATABASE_NAME = "OptStore";
    public static final String DATABASE_URL = PROTOCOL + URL_LOCALE_NAME + DATABASE_NAME;
    public static final String USER_NAME = "postgres";
    public static final String DATABASE_PASS = "postgres";

    public static void main(String[] args) {

        checkDriver();
        checkDB();
        System.out.println("Подключение к базе данных | " + DATABASE_URL + "\n");

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS)) {
            //TODO show all tables
            getOrders(connection); System.out.println();
            getClientele(connection); System.out.println();
            getProduct(connection); System.out.println();
            getSuppliers(connection); System.out.println();

            // TODO show with parametr
            getOnTheWayOrder(connection, "В пути"); System.out.println();
            getProductParam(connection, 1, 1000); System.out.println();
            getMolviniProduct(connection, 1); System.out.println();

            // TODO correction
            addOrder(connection, 3500, "Советская ул., 2А, микрорайон Пироговский, Мытищи", "Оформлен", 15, 30); System.out.println();
            correctStatusOrder(connection, 14, "В пути"); System.out.println();
            addProduct(connection, "Штаны 2yk", 500, 2000, 1); System.out.println();
            removeProduct(connection, "Штаны 2yk"); System.out.println();
            correctProductPrice(connection,26,  110); System.out.println();
            correctProductQuantity(connection, 22, 235); System.out.println();
            addSuppliers(connection, "Balensi", "89456473822", "1-я Хотьковская ул., 24, Хотьково", "balensi@yandex.ru"); System.out.println();
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")){
                System.out.println("Произошло дублирование данных");
            } else throw new RuntimeException(e);
        }
    }


    public static void checkDriver () {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Нет JDBC-драйвера! Подключите JDBC-драйвер к проекту согласно инструкции.");
            throw new RuntimeException(e);
        }
    }

    public static void checkDB () {
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        } catch (SQLException e) {
            System.out.println("Нет базы данных! Проверьте имя базы, путь к базе или разверните локально резервную копию согласно инструкции");
            throw new RuntimeException(e);
        }
    }


    private static void getOrders(Connection connection) throws SQLException{
        String columnName0 = "order_id", columnName1 = "order_date", columnName2 = "full_price", columnName3 = "delivery_address",
                columnName4 = "order_status", columnName5 = "product_id", columnName6 = "client_id";
        int param0 = -1, param2 = - 1, param5 = -1, param6 = -1;
        String param1 = null, param3 = null, param4 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM \"Orders\";");

        while (rs.next()) { 
            param6 = rs.getInt(columnName6);
            param5 = rs.getInt(columnName5);
            param4 = rs.getString(columnName4);
            param3 = rs.getString(columnName3);
            param2 = rs.getInt(columnName2); 
            param1 = rs.getString(columnName1);
            param0 = rs.getInt(columnName0);   
            System.out.println(param0 + " | " + param1 + " | " + param2 + " | " + param3 + " | " + param4 + " | " + param5 + " | " + param6);
        }
    }

    static void getProduct (Connection connection) throws SQLException {
        String columnName0 = "product_id", columnName1 = "Name", columnName2 = "price", columnName3 = "Quantity in stock",
                columnName4 = "suppliers_id";
        int param0 = -1, param2 = -1, param3 = -1, param4 = -1;
        String param1 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM product;");

        while (rs.next()) { 
            param0 = rs.getInt(columnName0); 
            param1 = rs.getString(columnName1);
            param2 = rs.getInt(columnName2);
            param3 = rs.getInt(columnName3);
            param4 = rs.getInt(columnName4);
            System.out.println(param0 + " | " + param1 + " | " + param2 + " | " + param3 + " | " + param4);
        }
    }

    static void getClientele (Connection connection) throws SQLException {
        String columnName0 = "client_id", columnName1 = "first_name", columnName2 = "last_name", columnName3 = "phone_number",
                columnName4 = "email";
        int param0 = -1;
        String param1 = null, param2 = null, param3 = null, param4 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM clientele;");

        while (rs.next()) {
            param0 = rs.getInt(columnName0);
            param1 = rs.getString(columnName1);
            param2 = rs.getString(columnName2);
            param3 = rs.getString(columnName3);
            param4 = rs.getString(columnName4);
            System.out.println(param0 + " | " + param1 + " | " + param2 + " | " + param3 + " | " + param4);
        }
    }

    static void getSuppliers (Connection connection) throws SQLException {
        // имена столбцов
        String columnName0 = "suppliers_id", columnName1 = "Name", columnName2 = "phone_number", columnName3 = "suppliers_address",
                columnName4 = "email";
        // значения ячеек
        int param0 = -1;
        String param1 = null, param2 = null, param3 = null, param4 = null;

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM suppliers;");

        while (rs.next()) {
            param0 = rs.getInt(columnName0);
            param1 = rs.getString(columnName1);
            param2 = rs.getString(columnName2);
            param3 = rs.getString(columnName3);
            param4 = rs.getString(columnName4);
            System.out.println(param0 + " | " + param1 + " | " + param2 + " | " + param3 + " | " + param4);
        }
    }


    private static void getOnTheWayOrder(Connection connection, String order_status) throws SQLException {
        if (order_status == null || order_status.isBlank()) return;
        long time = System.currentTimeMillis();
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * "+
                        "FROM \"Orders\"" +
                        "WHERE order_status LIKE ?;");
        statement.setString(1, order_status);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) { 
            System.out.println(rs.getString(1) + " | " + rs.getString(2) + " | " + rs.getInt(3) + " | " + rs.getString(4)
                    + " | " + rs.getString(5) + " | " + rs.getInt(6) + " | " + rs.getInt(7));
        }
        System.out.println("SELECT with WHERE (" + (System.currentTimeMillis() - time) + " мс.)");
    }
    private static void getProductParam(Connection connection, int value1, int value2) throws SQLException {
        long time = System.currentTimeMillis();
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * "+
                        "FROM product " +
                        "WHERE \"Quantity in stock\" BETWEEN ? AND ?;");
        statement.setInt(1, value1);
        statement.setInt(2, value2);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) { 
            System.out.println(rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getInt(3) + " | " + rs.getInt(4)
                    + " | " + rs.getInt(5));
        }
    }

    private static void getMolviniProduct(Connection connection, int suppliers_id) throws SQLException {
        if (suppliers_id < 0) return;

        PreparedStatement statement = connection.prepareStatement(
                "SELECT *" +
                        "FROM product " +
                        "JOIN suppliers ON product.suppliers_id = suppliers.suppliers_id " +
                        "WHERE suppliers.suppliers_id = ?;");
        statement.setInt(1, suppliers_id);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            System.out.println(rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getInt(3) +
                    " | " + rs.getInt(4) + " | " + rs.getInt(5));
        }
    }


    private static void addOrder(Connection connection, int full_price, String delivery_address, String order_status,
                                  int product_id, int client_id)  throws SQLException {
        if (delivery_address == null || delivery_address.isBlank() || client_id < 0 || full_price < 0 || product_id < 0) return;

        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO \"Orders\"(full_price, delivery_address, order_status, product_id, client_id) VALUES (?, ?, ?, ?, ?) returning order_id;", Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, full_price);
        statement.setString(2, delivery_address);
        statement.setString(3, order_status);
        statement.setInt(4, product_id);
        statement.setInt(5, client_id);

        int count =
                statement.executeUpdate();

        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            System.out.println("Идентификатор заказа " + rs.getInt(1));
        }

        System.out.println("INSERTed " + count + " orders");
        getOrders(connection);
    }

     private static void correctStatusOrder (Connection connection, int order_id, String order_status) throws SQLException {
        if (order_status == null || order_status.isBlank()) return;

        PreparedStatement statement = connection.prepareStatement("UPDATE \"Orders\" SET order_status=? WHERE order_id=?;");
        statement.setString(1, order_status);
        statement.setInt(2, order_id);

        int count = statement.executeUpdate();

        System.out.println("UPDATEd " + count + " orders");
        getOrders(connection);
    }
    private static void correctProductPrice (Connection connection, int product_id, int price) throws SQLException {
         if (product_id < 0 || price < 0) return;

         PreparedStatement statement = connection.prepareStatement("UPDATE product SET price=? WHERE product_id=?;");
         statement.setInt(1, price);
         statement.setInt(2, product_id);

         int count = statement.executeUpdate();

         System.out.println("UPDATEd " + count + " product");
         getProduct(connection);
     }
    private static void correctProductQuantity (Connection connection, int product_id, int Quantity) throws SQLException {
        if (product_id < 0 || Quantity < 0) return;

        PreparedStatement statement = connection.prepareStatement("UPDATE product SET \"Quantity in stock\"=? WHERE product_id=?;");
        statement.setInt(1, Quantity);
        statement.setInt(2, product_id);

        int count = statement.executeUpdate();

        System.out.println("UPDATEd " + count + " product");
        getProduct(connection);
    }
    private static void addProduct(Connection connection, String name, int price, int Quantity, int suppliers_id)  throws SQLException {
        if (name == null || name.isBlank() || price < 0 || Quantity < 0 || suppliers_id < 0) return;

        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO product(\"Name\", price, \"Quantity in stock\", suppliers_id) VALUES (?, ?, ?, ?) returning product_id;", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, name);
        statement.setInt(2, price);
        statement.setInt(3, Quantity);
        statement.setInt(4, suppliers_id);

        int count =
                statement.executeUpdate();

        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            System.out.println("Идентификатор товара " + rs.getInt(1));
        }

        System.out.println("INSERTed " + count + " products");
        getProduct(connection);
    }

    private static void removeProduct(Connection connection, String name) throws SQLException {
        if (name == null || name.isBlank()) return;

        PreparedStatement statement = connection.prepareStatement("DELETE from product WHERE \"Name\"=?;");
        statement.setString(1, name);

        int count = statement.executeUpdate();
        System.out.println("DELETEd " + count + " products");
        getProduct(connection);
    }
    private static void addSuppliers(Connection connection, String name, String number, String address, String email)  throws SQLException {
        if (name == null || name.isBlank() || number == null || number.isBlank() || address == null || address.isBlank() || email == null || email.isBlank()) return;

        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO suppliers(\"Name\", phone_number, suppliers_address, email) VALUES (?, ?, ?, ?) returning suppliers_id;", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, name);
        statement.setString(2, number);
        statement.setString(3, address);
        statement.setString(4, email);

        int count =
                statement.executeUpdate();

        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            System.out.println("Идентификатор поставщика " + rs.getInt(1));
        }

        System.out.println("INSERTed " + count + " products");
        getSuppliers(connection);
    }

}

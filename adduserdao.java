public HashMap<String, Object> getUserImages(String userId) {
        HashMap<String, Object> result = new HashMap<>();
        String query = "SELECT rejected_imgpath, remark as remark FROM profile_image_status WHERE user_id=?";

        jdbcTemplate.query(query, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, userId);
            }
        }, rs -> {
            while (rs.next()) {
                String userImage = rs.getString("rejected_imgpath");
                result.put("userImage", ConstantKeys.S3_PATH + userImage);

                String remark = rs.getString("remark");
                result.put("remark", remark);
            }
        });

        return result;
    }
}
In this refactored code, we've created a UserImages class with a JdbcTemplate instance. In the getUserImages method, the query and the preparation of the parameters remain the same. The execution of the query is now handled by the jdbcTemplate.query() method, which takes the SQL query string, a PreparedStatementSetter for setting the query parameters, and a ResultSetExtractor lambda function to process the ResultSet.

This way, the connection and statement handling is taken care of by the Spring framework, and you don't need to manage it manually.







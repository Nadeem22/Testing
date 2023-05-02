public HashMap<String, Object> getUserImages(String userId) {
        String query = "SELECT rejected_imgpath, remark as remark FROM profile_image_status WHERE user_id=?";

        return jdbcTemplate.query(query, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, userId);
            }
        }, new ResultSetExtractor<HashMap<String, Object>>() {
            @Override
            public HashMap<String, Object> extractData(ResultSet rs) throws SQLException {
                HashMap<String, Object> result = new HashMap<>();
                while (rs.next()) {
                    String userImage = rs.getString("rejected_imgpath");
                    result.put("userImage", ConstantKeys.S3_PATH + userImage);

                    String remark = rs.getString("remark");
                    result.put("remark", remark);
                }
                return result;
            }
        });
    }
@Autowired
private JdbcTemplate jdbcTemplate;

public List<User> getUser(String userName, String userType, String city, String team, String page,
        UserForm loginUser, String value, String param) {
    System.out.println("page " + page);
    List<User> userList = new ArrayList<>();

    // ... (the same query building logic as in the original method) ...

    StringBuilder queryBuilder = new StringBuilder(query);

    int start = ContextUtil.getReportProperties("report_start");
    int limitValue = ContextUtil.getReportProperties(param);
    if (!value.equals("0")) {
        int values = Integer.parseInt(value);
        queryBuilder.append("  limit ").append(values * limitValue).append(",").append(limitValue + 1);
    } else {
        queryBuilder.append(" limit ").append(start).append(",").append(limitValue + 1);
    }

    String finalQuery = queryBuilder.toString();

    userList = jdbcTemplate.query(finalQuery, preparedStatementSetter(userName, userType, city, team, page), (ResultSetExtractor<List<User>>) rs -> {
        List<User> users = new ArrayList<>();

        while (rs.next()) {
            User user = new User();
            // ... (the same user object population logic as in the original method) ...
            users.add(user);
        }
        return users;
    });

    return userList;
}

private PreparedStatementSetter preparedStatementSetter(String userName, String userType, String city, String team, String page) {
    return preparedStatement -> {
        if (page.equals("searchPage")) {
            int index = 0;
            if (!(userName == null || userName.equals("-1"))) {
                preparedStatement.setString(++index, "%" + userName + "%");
            }
            if (!(userType == null || userType.equals("-1"))) {
                preparedStatement.setString(++index, userType);
            }
            if (!(city == null || city.equals("-1"))) {
                preparedStatement.setString(++index, city);
            }
            if (!(team == null || team.equals("-1"))) {
                preparedStatement.setString(++index, team);
            }
        }
    };
}

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

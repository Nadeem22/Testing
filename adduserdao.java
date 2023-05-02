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

        // Prepare query and other variables as in the original code
        String query = "";
        
        if (!loginUser.getUserType().equals("1") && !loginUser.getUserType().equals("9")) {
			query = " select um.user_id,um.user_name,um.user_type,um.user_password,um.disabled,um.address,c.city_name,um.first_name,um.last_name,"
					+ "(select team_name from h2h_teams where team_id=um.parent_user_id AND sup_user_id !=0) as team_name,um.isLoggedIn ,"
					+ " imei, dName, dModel, apkVer, insAppReq, name,age,dob,work_exp,doj,/*Image_path,*/kyc_path"
					+ " from USER_MASTER um INNER JOIN city_master c ON um.city=c.city_id Where  user_type != 1 AND user_type != 9"
					+ " And um.agencyId=" + loginUser.getUserId();

		} else if (loginUser.getUserType().equals("9")) {
			query = " select um.user_id,um.user_name,um.user_type,um.user_password,um.disabled,um.address,c.city_name,um.first_name,um.last_name,"
					+ "(select team_name from h2h_teams where team_id=um.parent_user_id AND sup_user_id !=0) as team_name,um.isLoggedIn ,"
					+ " imei, dName, dModel, apkVer, insAppReq, name,age,dob,work_exp,doj,(select rejected_imgpath from profile_image_status "
					+ " where user_id=um.user_id) as Image_path,kyc_path,(select isRejected from profile_image_status "
					+ " where user_id=um.user_id)as Rejected,(select blocked from user_login_details where userid=um.user_id) as block "
					+ " from USER_MASTER um INNER JOIN city_master c ON um.city=c.city_id Where (um.parent_user_id is null or um.parent_user_id not in (select op_team_id from op_team)) and user_type != 1 AND user_type != 9"
					+ " And um.agencyId=" + loginUser.getUserId();
		} else {

			query = " select um.user_id,um.user_name,um.user_type,um.user_password,um.disabled,um.address,c.city_name,um.first_name,um.last_name,"
					+ "(select team_name from h2h_teams where team_id=um.parent_user_id AND sup_user_id !=0) as team_name,um.isLoggedIn "
					+ " from USER_MASTER um INNER JOIN city_master c ON um.city=c.city_id Where user_type in(9,15,17,20,22) ";

		}

        List<Object> params = new ArrayList<>();
        if (page.equals("searchPage")) {
            if (!(userName == null || userName.equals("-1"))) {
                query += " AND um.user_name like ? ";
                params.add("%" + userName + "%");
            }
            if (!(userType == null || userType.equals("-1"))) {
                query += " AND um.user_type =?";
                params.add(userType);
            }
            if (!(city == null || city.equals("-1"))) {
                query += " AND um.city in (?)";
                params.add(city);
            }
            if (!(team == null || team.equals("-1"))) {
                query += " AND um.parent_user_id in (?)";
                params.add(team);
            }
        }

        // Add limit to the query
        int start = ContextUtil.getReportProperties("report_start");
        int limitValue = ContextUtil.getReportProperties(param);
        if (!value.equals("0")) {
            int values = Integer.parseInt(value);
            query = query + "  limit " + (values * limitValue) + "," + (limitValue + 1);
        } else
            query = query + " limit " + start + "," + (limitValue + 1);

        List<User> userList = jdbcTemplate.query(query, params.toArray(), (ResultSetExtractor<List<User>>) resultSet -> {
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                User user = new User();
                // Set user properties from resultSet like in the original code
                user.setId(resultSet.getInt(1));
                user.setUserId(resultSet.getInt(1));
                user.setUserName(resultSet.getString(2));
                int UserType = resultSet.getInt(3);
                user.setUserType(UserType);
                user.setUserTypeStr(modelMap.get(UserType));
                if (resultSet.getString(5).equals("L") && !param.equals("smsExcel_limit")) {
                    user.setDisabled("L");
                } else {
                    user.setDisabled(resultSet.getString(5));
                }
                user.setAddress(resultSet.getString(6));
                user.setCity(resultSet.getString(7));
                user.setFirstName((resultSet.getString(8)));
                user.setLastName(resultSet.getString(9));
                user.setTeamName(resultSet.getString(10));
                user.setEdit("Edit");
                user.setDelete("Delete");
                if ((resultSet.getInt(11) == 0)) {

                    user.setIsActive("InActive");
                } else {
                    user.setIsActive("Active");
                }
            	if (!loginUser.getUserType().equals("1")) {
					user.setImei(res.getString(12));
					user.setdName(res.getString(13));
					user.setdModel(res.getString(14));
					user.setApkVer(res.getString(15));
					if (user.getUserType() >= 2 && user.getUserType() <= 6) {
						user.setAppListReq("" + ((res.getInt(16) == 1) ? "Active" : "InActive") + "");

						user.setAppListRep("Export");
					}
					user.setName(res.getString(17));
					user.setAge(res.getInt(18));
					user.setDob(res.getString(19));
					user.setWorkExp(res.getString(20));
					user.setDoj(res.getString(21));
					if (loginUser.getUserType().equals("9")) {
						String promoImgPath = res.getString(22);
						String kycImagePath = res.getString(23);
						String Isrejected = res.getString(24);

						if (null != res.getString(24) && res.getString(24).equals("Y")) {

							Gson gson = new GsonBuilder().disableHtmlEscaping().create();
							if (null != promoImgPath) {
								String path = "View Image";
								user.setUserImage(path);
							}

						} else if (null != res.getString(24) && res.getString(24).equals("N")) {
							user.setUserImage("N/A");
						}
					}
					if (user.getUserType() == 4 || user.getUserType() == 6) {
						if (res.getInt("block") == 2 || res.getInt("block") == 1) {
							user.setBlocked("" + ((res.getInt("block") != 0) ? "unlock" : "lock") + "");
						}

						else {
							user.setBlocked(""+ ((res.getInt("block") != 0) ? "unlock" : "lock") + "");
						}
						if (res.getInt("block") == 1)
							user.setLockedstatus("EXPIRED");
						else if (res.getInt("block") == 2)
							user.setLockedstatus("ADMIN BLOCKED");
						else
							user.setLockedstatus("UNLOCKED");
					}

				}

                users.add(user);
            }
            return users;
        });

        return userList;
    }


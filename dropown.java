@Autowired
    private JdbcTemplate jdbcTemplate;

    public List<City> getCityDropdown() {
        String sql = "SELECT CITY_NAME, CITY_ID FROM CITY_MASTER";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            City city = new City();
            city.setCityName(rs.getString("CITY_NAME"));
            city.setCityId(rs.getString("CITY_ID"));
            return city;
        });
    }
	
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeamDropdown {
    private JdbcTemplate jdbcTemplate;

    public TeamDropdown() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/mydb");
        dataSource.setUsername("username");
        dataSource.setPassword("password");

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public ArrayList<Team> getTeamDropdown(String city, UserForm userLogin) {
        String sql = "";
        List<Object> params = new ArrayList<>();

        if (city != null && !city.equals("-1")) {
            sql = "SELECT team_name, team_id FROM h2h_teams WHERE team_id NOT IN (SELECT op_team_id FROM op_team) AND city_id=?";
            params.add(city);

            if (!userLogin.getUserType().equals("1") && !userLogin.getUserType().equals("17")) {
                sql += " AND agencyId=?";
                params.add(userLogin.getUserId());
            }
        } else {
            sql = "SELECT team_name, team_id FROM h2h_teams";

            if (userLogin.getUserType() != null && !userLogin.getUserType().equals("1") && !userLogin.getUserType().equals("17")) {
                sql += " WHERE agencyId=? AND team_id NOT IN (SELECT op_team_id FROM op_team)";
                params.add(userLogin.getUserId());
            }
        }
        sql += " ORDER BY team_name ASC";

        ArrayList<Team> teamList = (ArrayList<Team>) jdbcTemplate.query(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setObject(i + 1, params.get(i));
                }
            }
        }, (rs, rowNum) -> {
            Team team = new Team();
            team.setTeamName(rs.getString("team_name"));
            team.setTeamId(rs.getString("team_id"));
            return team;
        });

        return teamList;
    }
}



public String generateDropdowns(HttpSession session, String agencyId) {
    try {
        String userType = (String) session.getAttribute("UserType");

        String sql = "SELECT CITY_NAME, CITY_ID FROM CITY_MASTER WHERE 1=1 ";
        if(userType.equals("25")){
            sql += " AND ZONE = 3 ";
        }
        if(userType.equals("26")){
            sql += " AND ZONE IN (0,1,2) ";
        }
        sql += "ORDER BY CITY_NAME ASC";
        List<City> cityList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(City.class));
        session.setAttribute("cityList", cityList);

        sql = "SELECT st.STATE_NAME, st.STATE_ID FROM STATE_MASTER st ";
        if(userType.equals("25")){
            sql += " inner join city_master ct on ct.state_id = st.state_id where zone = 3";
        }
        if(userType.equals("26")){
            sql += " inner join city_master ct on ct.state_id = st.state_id where zone in (0,1,2)";
        }
        List<State> stateList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(State.class));
        session.setAttribute("stateList", stateList);

        sql = "SELECT USER_NAME FROM USER_MASTER";
        if (!userType.equals("1")) {
            sql += " Where agencyId=" + agencyId;
        }
        List<String> userList = jdbcTemplate.queryForList(sql, String.class);
        session.setAttribute("userList", new Gson().toJson(userList));

        if (userType.equals("10")) {
            String city = (String) session.getAttribute("usercity");
            int zone = DropDownListDao.getZoneByCity(city);
            sql = "SELECT TEAM_ID, TEAM_NAME FROM h2h_TEAMS where city_id in (select city_id from city_master where zone=" + zone + ") AND agencyId=" + agencyId + " Order by TEAM_NAME asc ";
        } else if(userType.equals("25")){
            sql = "SELECT TEAM_ID, TEAM_NAME FROM h2h_TEAMS where city_id in (select city_id from city_master where zone= 3) AND agencyId=" + agencyId + " Order by TEAM_NAME asc ";
        } else if(userType.equals("26")){
            sql = "SELECT TEAM_ID, TEAM_NAME FROM h2h_TEAMS where city_id in (select city_id from city_master where zone in (0,1,2)) AND agencyId=" + agencyId + " Order by TEAM_NAME asc ";
        } else {
            if(userType.equals("1")){
                sql = "SELECT TEAM_ID, TEAM_NAME FROM h2h_TEAMS where 1=1"; 
            }else{
                sql = "SELECT TEAM_ID, TEAM_NAME FROM h2h_TEAMS where agencyId not in (select user_id from dummy_agencies)";
            }
            if (!userType.equals("1") && !userType.equals("17")&& !userType.equals("15") && agencyId != null) {
                sql += "and agencyId=" + agencyId;
            }
        }
        sql += "  Order by TEAM_NAME asc ";
        List<Team> teamNameList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Team.class));
        session.setAttribute("teamNameList", teamNameList);

        if(!userType.equals(18)){
            sql = "SELECT Field_Data_ID,Field_Data_Name,Field_Data_Details FROM BRANDS Where ParentId is null and disabled='N' order by Field_Data_Name";
            List<Brands> brandList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Brands.class));
            session.setAttribute("brandList", brandList);
        }

        return "success";
    } catch (DataAccessException e) {
        e.printStackTrace();
        return null;
    }
}
public int getZoneByCity(String cityId) {
    String sql = "SELECT zone FROM city_master where city_id = ?";
    try {
        return jdbcTemplate.queryForObject(sql, new Object[]{cityId}, Integer.class);
    } catch (DataAccessException e) {
        e.printStackTrace();
        return 0;
    }
}
public ArrayList<User> getAllActiveAgenciesString() {
        String sql = "SELECT user_name, user_id FROM user_master WHERE user_type=9 and user_id not in (select user_id from dummy_agencies) ORDER BY user_name ASC";

        ArrayList<User> userList = jdbcTemplate.query(sql, resultSet -> {
            ArrayList<User> list = new ArrayList<>();
            while (resultSet.next()) {
                User user = new User();
                user.setUserName(resultSet.getString("user_name"));
                user.setUserIdInString(resultSet.getString("user_id"));
                list.add(user);
            }
            return list;
        });

        System.out.println("dummy : " + sql);
        return userList;
    }

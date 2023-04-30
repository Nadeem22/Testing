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

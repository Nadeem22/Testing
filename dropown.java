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
	
public List<Team> getTeamDropdown(String city, UserForm userLogin) {
        List<Object> params = new ArrayList<>();
        String sql = "";

        if (city != null && !city.equals("-1")) {
            sql = "SELECT team_name, team_id FROM h2h_teams where team_id not in (select op_team_id from op_team) and city_id=? ";
            params.add(city);

            if (!userLogin.getUserType().equals("1") && !userLogin.getUserType().equals("17")) {
                sql += " AND agencyId=?";
                params.add(userLogin.getUserId());
            }
        } else {
            sql = "SELECT team_name, team_id FROM h2h_teams ";

            if (userLogin.getUserType() != null && !userLogin.getUserType().equals("1") && !userLogin.getUserType().equals("17")) {
                sql += " where agencyId=? and team_id not in (select op_team_id from op_team)";
                params.add(userLogin.getUserId());
            }
        }

        sql += " order by team_name asc";
        System.out.println(sql);

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            Team team = new Team();
            team.setTeamName(rs.getString("team_name"));
            team.setTeamId(rs.getString("team_id"));
            return team;
        });
    }
package kr.co.admonster.dispatcher.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import kr.co.admonster.common.constants.enums.BiddingType;
import kr.co.admonster.common.constants.enums.CampaignType;
import kr.co.admonster.common.constants.enums.DeviceType;
import kr.co.admonster.common.dto.PidGains;
import kr.co.admonster.dispatcher.domain.dto.BiddingTaskDto;

@Repository
public class BiddingTaskDao {
	
	private final JdbcTemplate jdbcTemplate;
	
	public BiddingTaskDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public List<BiddingTaskDto> findAll() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT\n)");
		query.append("    t1.keyword_id,\n)");
		query.append("    t1.keyword,\n)");
		query.append("    t1.display_url,\n)");
		
		query.append("    t1.device_type,\n)");
		query.append("    t1.campaign_type,\n)");
		query.append("    t1.bidding_type,\n)");
		query.append("    70 AS min_bid,\n)");
		query.append("    t1.max_bid,\n)");
		
		query.append("    t1.target_rank,\n)");
		query.append("    t1.current_bid,\n)");
		query.append("    t1.preset_bid,\n)");
		
		query.append("    t1.previous_error,\n)");
		query.append("    t1.integral_error,\n)");
		query.append("    t1.pid_cluster_id,\n)");
		
		query.append("    t2.customer_id,\n)");
		query.append("    t2.access_license,\n)");
		query.append("    t2.secret_key,\n)");
		
		query.append("    t3.kp AS kp,\n)");
		query.append("    t3.ki AS ki,\n)");
		query.append("    t3.kd AS kd,\n)");
		query.append("    t3.derivative_smoothing AS derivative_smoothing\n)");
		query.append("FROM\n)");
		query.append("    v_bidding_task AS t1\n)");
		query.append("JOIN\n)");
		query.append("    tb_media_license AS t2 ON t1.api_seq = t2.api_seq\n)");
		query.append("JOIN\n)");
		query.append("    tb_pid_cluster AS t3 ON t1.pid_cluster_id = t3.pid_cluster_id\n)");
		query.append("WHERE\n)");
		query.append("    t1.next_bidding_time <= NOW()\n)");
		
		return this.jdbcTemplate.query(query.toString(), new BiddingTaskDtoMapper());
	}
	
	public void updateNextTm(List<String> keywordIds, LocalDateTime nextTm) {
		String sql = "UPDATE tb_bidding SET NEXT_TM = ? WHERE KEYWORD_ID IN (?)";
		// IN 절을 동적으로 생성
		String inSql = String.join(",", Collections.nCopies(keywordIds.size(), "?"));
		String updateQuery = sql.replace("(?)", "(" + inSql + ")");
		
		this.jdbcTemplate.update(updateQuery, new Object[]{ nextTm, keywordIds.toArray() });
	}
	
	private static final class BiddingTaskDtoMapper implements RowMapper<BiddingTaskDto> {
		@Override
		public BiddingTaskDto mapRow(ResultSet rs, int rowNum) throws SQLException {
			BiddingTaskDto dto = new BiddingTaskDto();
			dto.setKeywordId(rs.getString("keyword_id"));
			dto.setKeyword(rs.getString("keyword"));
			dto.setDisplayUrl(rs.getString("display_url"));
			
			dto.setDeviceType(DeviceType.valueOf(rs.getString("device_type")));
			dto.setCampaignType(CampaignType.valueOf(rs.getString("campaign_type")));
			dto.setBiddingType(BiddingType.valueOf(rs.getString("bidding_type")));
			dto.setMinBid(rs.getDouble("min_bid"));
			dto.setMaxBid(rs.getDouble("max_bid"));
			
			dto.setTargetRank(rs.getInt("target_rank"));
			dto.setCurrentBid(rs.getDouble("current_bid"));
			dto.setPresetBid(rs.getDouble("preset_bid"));
			
			dto.setPreviousError(rs.getDouble("previous_error"));
			dto.setIntegralError(rs.getDouble("integral_error"));
			
			dto.setPidClusterId(rs.getString("pid_cluster_id"));
			
			dto.setCustomerId(rs.getString("customer_id"));
			dto.setAccessLicense(rs.getString("access_license"));
			dto.setSecretKey(rs.getString("secret_key"));
			
			dto.setPidGains(new PidGains(rs.getDouble("kp"), rs.getDouble("ki"), rs.getDouble("kd"), rs.getDouble("derivative_smoothing")));
			return dto;
		}
	}
	
}

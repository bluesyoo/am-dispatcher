package kr.co.admonster.dispatcher.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import kr.co.admonster.common.constants.Constants;
import kr.co.admonster.common.constants.enums.BiddingType;
import kr.co.admonster.common.constants.enums.CampaignType;
import kr.co.admonster.common.constants.enums.DeviceType;
import kr.co.admonster.common.dto.PidGains;
import kr.co.admonster.dispatcher.domain.dto.BiddingTaskDto;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class BiddingTaskDao {
	
	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	public BiddingTaskDao(
			JdbcTemplate jdbcTemplate,
			NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}
	
	private final AtomicLong lastTm = new AtomicLong(0);
	
	public List<BiddingTaskDto> findAll() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT\n");
		query.append("    t1.keyword_id,\n");
		query.append("    t1.keyword,\n");
		query.append("    t1.display_url,\n");
		
		query.append("    t1.device_type,\n");
		query.append("    t1.campaign_type,\n");
		query.append("    t1.bidding_type,\n");
		
		query.append("    t1.target_rank,\n");
		query.append("    t1.current_bid,\n");
		query.append("    t1.minimum_bid,\n");
		query.append("    t1.maximum_bid,\n");
		
		query.append("    t1.previous_error,\n");
		query.append("    t1.integral_error,\n");
		
		query.append("    IFNULL(t1.account_no,'') AS account_no,\n");
		query.append("    IFNULL(t1.pid_cluster_id,1) AS pid_cluster_id,\n");
		
		query.append("    IFNULL(JSON_UNQUOTE(JSON_EXTRACT(t2.license_option,\"$.access_license\")),'') AS access_license,\n");
		query.append("    IFNULL(JSON_UNQUOTE(JSON_EXTRACT(t2.license_option,\"$.secret_key\")),'') AS secret_key,\n");
		
		query.append("    IFNULL(t3.kp,0) AS kp,\n");
		query.append("    IFNULL(t3.ki,0) AS ki,\n");
		query.append("    IFNULL(t3.kd,0) AS kd,\n");
		query.append("    IFNULL(t3.derivative_smoothing,1) AS derivative_smoothing\n");
		query.append("FROM\n");
		query.append("    tb_bidding_task AS t1\n");
		query.append("LEFT JOIN\n");
		query.append("    tb_media_license AS t2 ON t1.license_seq = t2.seq\n");
		query.append("LEFT JOIN\n");
		query.append("    tb_pid_cluster AS t3 ON t1.pid_cluster_id = t3.id\n");
		query.append("WHERE\n");
		query.append("    t1.next_tm BETWEEN 0 AND UNIX_TIMESTAMP()\n");
		query.append("ORDER BY\n");
		query.append("    t1.next_tm ASC, RAND()\n");
		query.append("LIMIT 10\n");
		
		long now = System.currentTimeMillis();
		if (now - this.lastTm.get() > Constants.MILLISECOND_FOR_HOUR) {
			log.info("Query: {}", query);
			this.lastTm.set(now);
		}
		
		try {
			log.info("Fetch bidding_task. limit=10");
			List<BiddingTaskDto> results = this.jdbcTemplate.query(query.toString(), new BiddingTaskDtoMapper());
			log.info("Bidding_task fetched. size={}", results.size());
			
			if (results.isEmpty()) {
				log.warn("No bidding_task available.");
			}
			return results;
		} catch (Exception e) {
			log.error("Failed to fetch bidding_task.", e);
			throw e;
		}
	}
	
	public void updateNextTm(List<String> keywordIds) {
		String query = """
				UPDATE
					tb_bidding_task
				SET
					next_tm = -1
				WHERE
					keyword_id IN (:ids)
				""";
		
		try {
			log.info("Update next_tm for bidding_task. keyword_ids_count={}", keywordIds.size());
			int updated = this.namedParameterJdbcTemplate.update(query, Map.of("ids", keywordIds));
			log.info("Updated bidding_task successfully. keyword_ids={}, updatedRows={}", keywordIds, updated);
		} catch (Exception e) {
			log.error("Failed to update bidding_task. keyword_ids={}", keywordIds, e);
			throw e;
		}
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
			
			dto.setTargetRank(rs.getInt("target_rank"));
			dto.setCurrentBid(rs.getDouble("current_bid"));
			dto.setMinimumBid(rs.getDouble("minimum_bid"));
			dto.setMaximumBid(rs.getDouble("maximum_bid"));
			
			dto.setPreviousError(rs.getDouble("previous_error"));
			dto.setIntegralError(rs.getDouble("integral_error"));
			
			dto.setAccountNo(rs.getString("account_no"));
			dto.setAccessLicense(rs.getString("access_license"));
			dto.setSecretKey(rs.getString("secret_key"));
			
			dto.setPidClusterId(rs.getInt("pid_cluster_id"));
			dto.setPidGains(new PidGains(rs.getDouble("kp"), rs.getDouble("ki"), rs.getDouble("kd"), rs.getDouble("derivative_smoothing")));
			return dto;
		}
	}
	
}

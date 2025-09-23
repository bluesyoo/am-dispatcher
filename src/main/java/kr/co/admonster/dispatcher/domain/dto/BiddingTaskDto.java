package kr.co.admonster.dispatcher.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import kr.co.admonster.common.constants.enums.BiddingType;
import kr.co.admonster.common.constants.enums.CampaignType;
import kr.co.admonster.common.constants.enums.DeviceType;
import kr.co.admonster.common.dto.PidGains;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
@Setter
@ToString
public class BiddingTaskDto {
	
	private String keywordId;
	private String keyword;
	private String displayUrl;
	
	// 입찰 상품 정보
	private DeviceType deviceType;
	private CampaignType campaignType;
	private BiddingType biddingType;
	private Double minBid;
	private Double maxBid;
	
	// 입찰 실행 정보
	private Integer targetRank;         // 목표 순위
	private Double currentBid;          // 현재 입찰가
	private Double presetBid;           // 설정 입찰가
	
	private Double previousError;
	private Double integralError;
	
	private Integer pidClusterId;
	private PidGains pidGains;
	
	// api 정보
	private Long accountNo;
	private String accessLicense;
	private String secretKey;
	
}

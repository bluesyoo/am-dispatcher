package kr.co.admonster.dispatcher.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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
public class BiddingTask {
	
	private String keywordId;
	private String keyword;
	private String deviceType;
	private String displayUrl;
	
	private String biddingType;
	private Double maxBid;
	
	private Integer targetRank;
	private Double currentBid;
	private Double presetBid;
	
	private Double previousError;
	private Double integralError;
	
	private LocalDateTime nextTm;
	private LocalDateTime lastTm;
	
	private Integer agencySeq;
	private Long accountNo;
	private String mediaTp;
	
	private Integer apiSeq;
	private String pidClusterId;
	
}

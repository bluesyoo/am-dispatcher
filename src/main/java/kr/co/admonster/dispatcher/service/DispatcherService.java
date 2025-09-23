package kr.co.admonster.dispatcher.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.admonster.dispatcher.db.dao.BiddingTaskDao;
import kr.co.admonster.dispatcher.domain.dto.BiddingTaskDto;
import kr.co.admonster.kafka.domain.BiddingTaskMessage;
import kr.co.admonster.kafka.naming.MessageType;
import kr.co.admonster.kafka.producer.MessageProducer;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DispatcherService {
	
	private final MessageProducer messageProducer;
	
	private final BiddingTaskDao biddingTaskDao;
	
	public DispatcherService(
			MessageProducer messageProducer,
			BiddingTaskDao biddingTaskDao) {
			this.messageProducer = messageProducer;
			
			this.biddingTaskDao = biddingTaskDao;
	}
	
	@Transactional
	public void run() {
		log.info("### DispatcherService started to find bidding tasks. ###");
		
		// 1. 입찰이 필요한 키워드 조회
		List<BiddingTaskDto> tasks = this.biddingTaskDao.findAll();
		log.info("Found {} keywords to dispatch.", tasks.size());
		
		if (!tasks.isEmpty()) {
			List<String> keywordIds = new ArrayList<>();
			
			for (BiddingTaskDto task : tasks) {
				BiddingTaskMessage taskMessage = BiddingTaskMessage.builder()
						.messageType(MessageType.BIDDING)
						.keywordId(task.getKeywordId())
						.keyword(task.getKeyword())
						.displayUrl(task.getDisplayUrl())
						
						.deviceType(task.getDeviceType())
						.campaignType(task.getCampaignType())
						.biddingType(task.getBiddingType())
						.maxBid(task.getMaxBid())
						
						.targetRank(task.getTargetRank())
						.currentBid(task.getCurrentBid())
						.presetBid(task.getPresetBid())
						
						.previousError(task.getPreviousError()) // 초기화
						.integralError(task.getIntegralError()) // 초기화
						
						.pidGains(task.getPidGains())
						
						.customerId(task.getCustomerId())
						.accessLicense(task.getAccessLicense())
						.secretKey(task.getSecretKey())
						.build();
				
				// 3. Kafka로 메시지 전송
				this.messageProducer.send(taskMessage);
			}
			
			// 4. 다음 입찰 시간 업데이트 로직 추가
			this.biddingTaskDao.updateNextTm(keywordIds, LocalDateTime.now());
			
			log.info("### DispatcherService finished. ###");
		}
		
	}
	
}

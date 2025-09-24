package kr.co.admonster.dispatcher.service;

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
	public void execute() {
		log.info("Start dispatcher job to find bidding_tasks.");
		
		try {
			// 1. 입찰이 필요한 키워드 조회
			List<BiddingTaskDto> tasks = this.biddingTaskDao.findAll();
			log.info("Found {} bidding_tasks to dispatch.", tasks.size());
			
			if (tasks.isEmpty()) {
				log.warn("No bidding_task to dispatch.");
				return;
			}
			
			List<String> keywordIds = new ArrayList<>();
			
			for (BiddingTaskDto task : tasks) {
				keywordIds.add(task.getKeywordId());
				
				BiddingTaskMessage taskMessage = BiddingTaskMessage.builder()
						.messageType(MessageType.BIDDING)
						
						.keywordId(task.getKeywordId())
						.keyword(task.getKeyword())
						.displayUrl(task.getDisplayUrl())
						
						.deviceType(task.getDeviceType())
						.campaignType(task.getCampaignType())
						.biddingType(task.getBiddingType())
						
						.targetRank(task.getTargetRank())
						.currentBid(task.getCurrentBid())
						.minimumBid(task.getMinimumBid())
						.maximumBid(task.getMaximumBid())
						
						.previousError(task.getPreviousError()) // 초기화
						.integralError(task.getIntegralError()) // 초기화
						
						.pidClusterId(task.getPidClusterId())
						.pidGains(task.getPidGains())
						
						.accountNo(task.getAccountNo())
						.accessLicense(task.getAccessLicense())
						.secretKey(task.getSecretKey())
						.build();
				
				// 3. Kafka로 메시지 전송
				this.messageProducer.send(taskMessage);
			}
				
			// 4. 다음 입찰 시간 업데이트 로직 추가
			this.biddingTaskDao.updateNextTm(keywordIds);
			
			log.info("Completed dispatcher job successfully. dispatched_count={} keyword_ids={}", keywordIds.size(), keywordIds);
		} catch (Exception e) {
			log.error("Failed to run dispatcher job.", e);
			throw e;
		}
	}
	
}

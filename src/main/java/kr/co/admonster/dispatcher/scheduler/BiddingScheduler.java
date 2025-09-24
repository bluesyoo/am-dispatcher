package kr.co.admonster.dispatcher.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.co.admonster.common.constants.Constants;
import kr.co.admonster.dispatcher.service.DispatcherService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BiddingScheduler {
	
	private final DispatcherService dispatcherService;
	
	public BiddingScheduler(DispatcherService dispatcherService) {
		this.dispatcherService = dispatcherService;
	}
	
	@Scheduled(cron = "${app.batch.job.aggregate-settle:*/20 * * * * ?}", zone = Constants.ZONE)
	public void run() {
		log.info("Start dispatch job.");
		
		try {
			this.dispatcherService.execute();
			log.info("Dispatch job completed successfully.");
		} catch (Exception e) {
			log.error("Failed to execute dispatch job.", e);
		}
	}
	
}
